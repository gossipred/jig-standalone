package com.jj.jig.ui.uninstall;

import com.jj.jig.auth.AuthService;
import com.jj.jig.uninstall.UninstallService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UninstallDialogController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordBtn;
    @FXML private Label errorLabel;

    private boolean passwordShown = false;

    private final AuthService authService;
    private final UninstallService uninstallService;

    public UninstallDialogController(AuthService authService, UninstallService uninstallService) {
        this.authService = authService;
        this.uninstallService = uninstallService;
    }

    @FXML
    public void togglePasswordVisibility() {
        if (passwordShown) {
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            togglePasswordBtn.setText("👁");
            passwordShown = false;
        } else {
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisibleField.requestFocus();
            passwordVisibleField.positionCaret(passwordVisibleField.getText().length());
            togglePasswordBtn.setText("🚫👁");
            passwordShown = true;
        }
    }

    @FXML
    public void handleCancel() {
        getStage().close();
    }

    @FXML
    public void handleConfirm() {
        String username = usernameField.getText().trim();
        String password = passwordShown ? passwordVisibleField.getText() : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter admin credentials. / 請輸入管理者帳號密碼。");
            return;
        }

        if (!authService.verifyAdmin(username, password)) {
            showError("Invalid admin credentials. / 管理者帳號密碼錯誤。");
            passwordField.clear();
            passwordVisibleField.clear();
            usernameField.requestFocus();
            return;
        }

        uninstallService.markForUninstall();
        getStage().close();
        Platform.exit();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private Stage getStage() {
        return (Stage) usernameField.getScene().getWindow();
    }
}
