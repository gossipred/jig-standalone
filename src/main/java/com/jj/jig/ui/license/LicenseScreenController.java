package com.jj.jig.ui.license;

import com.jj.jig.license.AuthStatus;
import com.jj.jig.license.LicenseInfo;
import com.jj.jig.license.LicenseService;
import com.jj.jig.license.LicenseValidator;
import com.jj.jig.ui.SpringFxmlLoader;
import com.jj.jig.ui.StageHolder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class LicenseScreenController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField machineIdField;
    @FXML private Label statusLabel;

    private final LicenseService licenseService;
    private final LicenseValidator licenseValidator;
    private final StageHolder stageHolder;
    private final SpringFxmlLoader fxmlLoader;

    public LicenseScreenController(LicenseService licenseService,
                                   LicenseValidator licenseValidator,
                                   StageHolder stageHolder,
                                   SpringFxmlLoader fxmlLoader) {
        this.licenseService = licenseService;
        this.licenseValidator = licenseValidator;
        this.stageHolder = stageHolder;
        this.fxmlLoader = fxmlLoader;
    }

    @FXML
    public void initialize() {
        machineIdField.setText(licenseService.getMachineId());

        AuthStatus status = licenseService.checkAuthorization();
        if (status.state() == com.jj.jig.license.AuthState.EXPIRED) {
            titleLabel.setText("Trial Expired / 試用期已結束");
            subtitleLabel.setText(
                "Your 30-day free trial has expired. Please purchase a license.\n" +
                "30 天免費試用已到期，請聯絡我們取得授權。");
        } else {
            titleLabel.setText("License Required / 需要授權");
            subtitleLabel.setText(
                "Please load a valid license file to continue.\n" +
                "請載入有效的 .lic 授權檔以繼續使用。");
        }
    }

    @FXML
    public void handleCopyMachineId() {
        ClipboardContent content = new ClipboardContent();
        content.putString(machineIdField.getText());
        Clipboard.getSystemClipboard().setContent(content);
        showStatus("Copied! / 已複製到剪貼簿", false);
    }

    @FXML
    public void handleLoadLicense() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select License File / 選擇授權檔");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("License Files", "*.lic"));
        File file = chooser.showOpenDialog(getStage());
        if (file == null) return;

        Optional<LicenseInfo> info = licenseValidator.loadFrom(file.toPath());
        if (info.isEmpty()) {
            showStatus("Invalid license file / 授權檔無效或已損壞。", true);
            return;
        }

        // Copy to the standard location
        try {
            Path dest = LicenseValidator.LICENSE_FILE;
            Files.createDirectories(dest.getParent());
            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            showStatus("Failed to install license: " + e.getMessage(), true);
            return;
        }

        // Invalidate cache and re-check
        licenseService.invalidateCache();
        AuthStatus status = licenseService.checkAuthorization();

        if (status.isAllowed()) {
            openLoginScreen();
        } else {
            showStatus("License valid but does not match this machine / 授權不符合本機。", true);
        }
    }

    private void openLoginScreen() {
        try {
            Parent loginRoot = fxmlLoader.load("/fxml/login.fxml");
            Stage stage = getStage();
            Scene scene = new Scene(loginRoot, 960, 600);
            scene.getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            showStatus("Failed to open login screen.", true);
        }
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
        statusLabel.getStyleClass().add(isError ? "error-label" : "success-label");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private Stage getStage() {
        return stageHolder.getPrimaryStage();
    }
}
