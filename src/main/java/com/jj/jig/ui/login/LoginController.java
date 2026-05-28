package com.jj.jig.ui.login;

import com.jj.jig.auth.AuthService;
import com.jj.jig.auth.UserSession;
import com.jj.jig.ui.SpringFxmlLoader;
import com.jj.jig.ui.StageHolder;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordBtn;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private boolean passwordShown = false;

    private final AuthService authService;
    private final UserSession userSession;
    private final StageHolder stageHolder;
    private final SpringFxmlLoader fxmlLoader;

    public LoginController(AuthService authService, UserSession userSession,
                           StageHolder stageHolder, SpringFxmlLoader fxmlLoader) {
        this.authService = authService;
        this.userSession = userSession;
        this.stageHolder = stageHolder;
        this.fxmlLoader = fxmlLoader;
    }

    @FXML
    public void focusPassword() {
        if (passwordShown) {
            passwordVisibleField.requestFocus();
        } else {
            passwordField.requestFocus();
        }
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
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordShown ? passwordVisibleField.getText() : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }

        loginButton.setDisable(true);

        if (authService.login(username, password)) {
            openMainWindow();
        } else {
            showError("Invalid username or password.");
            passwordField.clear();
            passwordVisibleField.clear();
            usernameField.requestFocus();
            loginButton.setDisable(false);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void openMainWindow() {
        try {
            Parent mainRoot = fxmlLoader.load("/fxml/main.fxml");
            Stage stage = stageHolder.getPrimaryStage();
            Scene mainScene = new Scene(mainRoot, 1200, 800);
            mainScene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(mainScene);
            stage.setResizable(true);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Failed to open main window.");
            loginButton.setDisable(false);
        }
    }
}
