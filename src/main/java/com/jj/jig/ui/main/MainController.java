package com.jj.jig.ui.main;

import com.jj.jig.auth.UserSession;
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
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class MainController {

    @FXML private Label welcomeLabel;
    @FXML private Button adminBtn;

    private final UserSession userSession;
    private final SpringFxmlLoader fxmlLoader;
    private final StageHolder stageHolder;

    public MainController(UserSession userSession, SpringFxmlLoader fxmlLoader,
                          StageHolder stageHolder) {
        this.userSession = userSession;
        this.fxmlLoader = fxmlLoader;
        this.stageHolder = stageHolder;
    }

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + userSession.getCurrentUser().getUsername() + "!");
        boolean isAdmin = userSession.getCurrentUser().getRole() == UserRole.ADMIN;
        adminBtn.setVisible(isAdmin);
        adminBtn.setManaged(isAdmin);
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
            Alert alert = new Alert(AlertType.ERROR, "Failed to open admin panel.");
            alert.initOwner(stageHolder.getPrimaryStage());
            alert.showAndWait();
        }
    }
}
