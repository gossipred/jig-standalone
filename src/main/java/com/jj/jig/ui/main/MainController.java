package com.jj.jig.ui.main;

import com.jj.jig.auth.UserSession;
import com.jj.jig.license.AuthState;
import com.jj.jig.license.AuthStatus;
import com.jj.jig.license.LicenseService;
import com.jj.jig.ui.SpringFxmlLoader;
import com.jj.jig.ui.StageHolder;
import com.jj.jig.user.UserRole;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class MainController {

    @FXML private BorderPane mainRoot;
    @FXML private Label welcomeLabel;
    @FXML private Button adminBtn;
    @FXML private Button navJigsBtn;
    @FXML private Button navLogsBtn;
    @FXML private Button navStatsBtn;
    @FXML private javafx.scene.layout.HBox trialBanner;
    @FXML private Label trialBannerLabel;

    private Button activeNavBtn;

    private final UserSession userSession;
    private final SpringFxmlLoader fxmlLoader;
    private final StageHolder stageHolder;
    private final LicenseService licenseService;

    public MainController(UserSession userSession, SpringFxmlLoader fxmlLoader,
                          StageHolder stageHolder, LicenseService licenseService) {
        this.userSession = userSession;
        this.fxmlLoader = fxmlLoader;
        this.stageHolder = stageHolder;
        this.licenseService = licenseService;
    }

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + userSession.getCurrentUser().getUsername() + "!");
        boolean isAdmin = userSession.getCurrentUser().getRole() == UserRole.ADMIN;
        adminBtn.setVisible(isAdmin);
        adminBtn.setManaged(isAdmin);

        AuthStatus status = licenseService.checkAuthorization();
        if (status.state() == AuthState.TRIAL) {
            trialBannerLabel.setText(
                "Trial Mode / 試用期：剩餘 " + status.trialDaysLeft() + " 天 (" + status.trialDaysLeft() + " days remaining)");
            trialBanner.setVisible(true);
            trialBanner.setManaged(true);
        }

        handleNavJigs();
    }

    @FXML
    public void handleNavJigs() {
        switchView("/fxml/jig-list.fxml", navJigsBtn);
    }

    @FXML
    public void handleNavLogs() {
        switchView("/fxml/log-view.fxml", navLogsBtn);
    }

    @FXML
    public void handleNavStats() {
        switchView("/fxml/stats-view.fxml", navStatsBtn);
    }

    private void switchView(String fxmlPath, Button navBtn) {
        try {
            Parent view = fxmlLoader.load(fxmlPath);
            mainRoot.setCenter(view);
            setActiveNav(navBtn);
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR, "Failed to load view: " + e.getMessage(), ButtonType.OK);
            alert.initOwner(stageHolder.getPrimaryStage());
            alert.showAndWait();
        }
    }

    private void setActiveNav(Button btn) {
        if (activeNavBtn != null) {
            activeNavBtn.getStyleClass().remove("main-nav-btn-active");
        }
        activeNavBtn = btn;
        if (btn != null && !btn.getStyleClass().contains("main-nav-btn-active")) {
            btn.getStyleClass().add("main-nav-btn-active");
        }
    }

    @FXML
    public void handleOpenAdminManagement() {
        try {
            Parent adminRoot = fxmlLoader.load("/fxml/admin-management.fxml");
            Stage adminStage = new Stage();
            adminStage.setTitle("Admin Management / 管理員操作設定");
            adminStage.initModality(Modality.WINDOW_MODAL);
            adminStage.initOwner(stageHolder.getPrimaryStage());
            adminStage.setResizable(false);
            Scene scene = new Scene(adminRoot);
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm());
            adminStage.setScene(scene);
            adminStage.show();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR, "Failed to open admin panel.", ButtonType.OK);
            alert.initOwner(stageHolder.getPrimaryStage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleLogout() {
        userSession.logout();
        try {
            Parent loginRoot = fxmlLoader.load("/fxml/login.fxml");
            Stage stage = stageHolder.getPrimaryStage();
            Scene loginScene = new Scene(loginRoot, 960, 600);
            loginScene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(loginScene);
            stage.setResizable(false);
            stage.setWidth(960);
            stage.setHeight(600);
            stage.centerOnScreen();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR, "Failed to return to login screen.", ButtonType.OK);
            alert.initOwner(stageHolder.getPrimaryStage());
            alert.showAndWait();
        }
    }
}
