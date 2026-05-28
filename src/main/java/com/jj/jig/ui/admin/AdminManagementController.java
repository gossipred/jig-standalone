package com.jj.jig.ui.admin;

import com.jj.jig.auth.UserSession;
import com.jj.jig.backup.AutoBackupService;
import com.jj.jig.backup.AutoBackupSettings;
import com.jj.jig.backup.BackupRestoreService;
import com.jj.jig.ui.SpringFxmlLoader;
import com.jj.jig.ui.StageHolder;
import com.jj.jig.update.UpdateCheckResult;
import com.jj.jig.update.UpdateService;
import com.jj.jig.user.User;
import com.jj.jig.user.UserForm;
import com.jj.jig.user.UserLog;
import com.jj.jig.user.UserRole;
import com.jj.jig.user.UserService;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class AdminManagementController {

    // ---- Users tab ----
    @FXML private TableView<User>    usersTable;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, String> colCreated;
    @FXML private Button editUserBtn;
    @FXML private Button resetPasswordBtn;
    @FXML private Button deleteUserBtn;
    @FXML private TableView<UserLog>    logsTable;
    @FXML private TableColumn<UserLog, String> colLogActor;
    @FXML private TableColumn<UserLog, String> colLogTarget;
    @FXML private TableColumn<UserLog, String> colLogAction;
    @FXML private TableColumn<UserLog, String> colLogDate;

    // ---- Update tab ----
    @FXML private Label  currentVersionLabel;
    @FXML private Label  updateStatusLabel;
    @FXML private VBox   updateDetailsBox;
    @FXML private Label  updateVersionLabel;
    @FXML private Label  updateNotesLabel;
    @FXML private Label  updateNotesZhLabel;
    @FXML private Button openDownloadBtn;

    // ---- Backup & Restore tab ----
    @FXML private TextField  backupLocationField;
    @FXML private Label      lastAutoBackupLabel;
    @FXML private CheckBox   autoBackupCheckBox;
    @FXML private ComboBox<String> frequencyComboBox;
    @FXML private Label      dataStatusLabel;
    @FXML private TextField  restoreFolderField;
    @FXML private ListView<String> backupListView;
    @FXML private Button     scheduleRestoreBtn;

    private List<Path> availableBackupPaths = List.of();
    private String pendingDownloadUrl = null;
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserService userService;
    private final UserSession userSession;
    private final BackupRestoreService backupRestoreService;
    private final AutoBackupService autoBackupService;
    private final UpdateService updateService;
    private final SpringFxmlLoader fxmlLoader;
    private final StageHolder stageHolder;

    public AdminManagementController(UserService userService, UserSession userSession,
                                     BackupRestoreService backupRestoreService,
                                     AutoBackupService autoBackupService,
                                     UpdateService updateService,
                                     SpringFxmlLoader fxmlLoader, StageHolder stageHolder) {
        this.userService = userService;
        this.userSession = userSession;
        this.backupRestoreService = backupRestoreService;
        this.autoBackupService = autoBackupService;
        this.updateService = updateService;
        this.fxmlLoader = fxmlLoader;
        this.stageHolder = stageHolder;
    }

    @FXML
    public void initialize() {
        initUsersTab();
        initUpdateTab();
        initBackupRestoreTab();
    }

    // ========== Users Tab ==========

    private void initUsersTab() {
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole().name()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isEnabled() ? "✓ Active / 啟用" : "✗ Disabled / 停用"));
        colCreated.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null
                        ? d.getValue().getCreatedAt().format(DT_FMT) : ""));

        colLogActor.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getActorUser() != null ? d.getValue().getActorUser().getUsername() : "-"));
        colLogTarget.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTargetUser() != null ? d.getValue().getTargetUser().getUsername() : "-"));
        colLogAction.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getActionType().name()));
        colLogDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null
                        ? d.getValue().getCreatedAt().format(DT_FMT) : ""));

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean hasSelection = sel != null;
            editUserBtn.setDisable(!hasSelection);
            resetPasswordBtn.setDisable(!hasSelection);
            deleteUserBtn.setDisable(!hasSelection);
        });

        refreshUserTable();
    }

    private void refreshUserTable() {
        usersTable.setItems(FXCollections.observableArrayList(userService.findAll()));
        logsTable.setItems(FXCollections.observableArrayList(userService.findRecentLogs()));
    }

    @FXML
    public void handleRefresh() {
        refreshUserTable();
    }

    @FXML
    public void handleAddUser() {
        showUserFormDialog(null).ifPresent(form -> {
            try {
                userService.create(form, userSession.getCurrentUser().getUsername());
                refreshUserTable();
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Error / 錯誤", e.getMessage());
            }
        });
    }

    @FXML
    public void handleEditUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        showUserFormDialog(selected).ifPresent(form -> {
            try {
                userService.update(selected.getId(), form,
                        userSession.getCurrentUser().getUsername());
                refreshUserTable();
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Error / 錯誤", e.getMessage());
            }
        });
    }

    @FXML
    public void handleResetPassword() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        showPasswordResetDialog(selected).ifPresent(newPassword -> {
            try {
                UserForm form = UserForm.from(selected);
                form.setPassword(newPassword);
                userService.update(selected.getId(), form,
                        userSession.getCurrentUser().getUsername());
                refreshUserTable();
                showAlert(AlertType.INFORMATION, "Done / 完成",
                        "Password reset for: " + selected.getUsername());
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Error / 錯誤", e.getMessage());
            }
        });
    }

    @FXML
    public void handleDeleteUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(AlertType.WARNING,
                "Delete account: " + selected.getUsername() + "\n刪除帳號後無法復原。\n\nConfirm?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Delete User / 刪除帳號");
        confirm.initOwner(getStage());
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            try {
                userService.delete(selected.getId(),
                        userSession.getCurrentUser().getUsername());
                refreshUserTable();
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Error / 錯誤", e.getMessage());
            }
        });
    }

    private Optional<UserForm> showUserFormDialog(User existing) {
        boolean isNew = (existing == null);
        Dialog<UserForm> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Add User / 新增帳號" : "Edit User / 編輯帳號");
        dialog.initOwner(getStage());
        dialog.initModality(Modality.WINDOW_MODAL);

        VBox content = new VBox(8);
        content.setPadding(new Insets(16));
        content.setPrefWidth(340);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username / 帳號");
        if (!isNew) {
            usernameField.setText(existing.getUsername());
            usernameField.setEditable(false);
            usernameField.setStyle("-fx-background-color: #f3f4f6;");
        }

        TextField passwordField = new TextField();
        passwordField.setPromptText(isNew
                ? "Password (min 6 chars) / 密碼（至少 6 字元）"
                : "New password (blank = no change) / 留空表示不更改");

        ComboBox<UserRole> roleBox = new ComboBox<>(
                FXCollections.observableArrayList(UserRole.values()));
        roleBox.setValue(isNew ? UserRole.OPERATOR : existing.getRole());
        roleBox.setPrefWidth(300);

        CheckBox enabledBox = new CheckBox("Enabled / 啟用");
        enabledBox.setSelected(isNew || existing.isEnabled());

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");

        content.getChildren().addAll(
                new Label("Username / 帳號"), usernameField,
                new Label(isNew ? "Password / 密碼（必填）" : "Password / 密碼（選填）"), passwordField,
                new Label("Role / 角色"), roleBox,
                enabledBox, errorLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (isNew && username.isEmpty()) {
                errorLabel.setText("Username is required. / 帳號不可為空。"); ev.consume(); return;
            }
            if (isNew && password.isEmpty()) {
                errorLabel.setText("Password is required. / 密碼不可為空。"); ev.consume(); return;
            }
            if (!password.isEmpty() && password.length() < 6) {
                errorLabel.setText("Password must be at least 6 characters. / 密碼至少 6 字元。");
                ev.consume(); return;
            }
            if (roleBox.getValue() == null) {
                errorLabel.setText("Please select a role. / 請選擇角色。"); ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            UserForm form = new UserForm();
            form.setUsername(usernameField.getText().trim());
            form.setPassword(passwordField.getText());
            form.setRole(roleBox.getValue());
            form.setEnabled(enabledBox.isSelected());
            return form;
        });

        return dialog.showAndWait();
    }

    private Optional<String> showPasswordResetDialog(User target) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password / 重設密碼：" + target.getUsername());
        dialog.initOwner(getStage());
        dialog.initModality(Modality.WINDOW_MODAL);

        VBox content = new VBox(8);
        content.setPadding(new Insets(16));
        content.setPrefWidth(320);

        PasswordField newPwd   = new PasswordField();
        newPwd.setPromptText("New password / 新密碼（至少 6 字元）");
        PasswordField confirmPwd = new PasswordField();
        confirmPwd.setPromptText("Confirm password / 確認新密碼");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");

        content.getChildren().addAll(
                new Label("New Password / 新密碼"), newPwd,
                new Label("Confirm / 確認"), confirmPwd,
                errorLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            if (newPwd.getText().length() < 6) {
                errorLabel.setText("At least 6 characters. / 至少 6 字元。"); ev.consume(); return;
            }
            if (!newPwd.getText().equals(confirmPwd.getText())) {
                errorLabel.setText("Passwords do not match. / 兩次密碼不一致。"); ev.consume();
            }
        });

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? newPwd.getText() : null);
        return dialog.showAndWait();
    }

    // ========== Update Tab ==========

    private void initUpdateTab() {
        currentVersionLabel.setText(updateService.getCurrentVersion());
        updateDetailsBox.setVisible(false);
        updateDetailsBox.setManaged(false);
    }

    @FXML
    public void handleCheckUpdate() {
        updateStatusLabel.setText("Checking... / 檢查中...");
        updateStatusLabel.setStyle("-fx-text-fill: #6b7280;");
        updateDetailsBox.setVisible(false);
        updateDetailsBox.setManaged(false);
        new Thread(() -> {
            try {
                UpdateCheckResult result = updateService.checkForUpdate();
                Platform.runLater(() -> showUpdateResult(result));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateStatusLabel.setText("Connection failed / 連線失敗：" + e.getMessage());
                    updateStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                });
            }
        }).start();
    }

    private void showUpdateResult(UpdateCheckResult result) {
        if (result.hasUpdate()) {
            updateStatusLabel.setText("New version available! / 有新版本可用！");
            updateStatusLabel.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
            updateVersionLabel.setText(result.getLatestVersion() + "  (" + result.getReleaseDate() + ")");
            updateNotesLabel.setText(result.getReleaseNotes());
            updateNotesZhLabel.setText(result.getReleaseNotesZh());
            pendingDownloadUrl = result.getDownloadUrl();
            updateDetailsBox.setVisible(true);
            updateDetailsBox.setManaged(true);
        } else {
            updateStatusLabel.setText("Already up to date. / 已是最新版本。");
            updateStatusLabel.setStyle("-fx-text-fill: #2563eb;");
        }
    }

    @FXML
    public void handleOpenDownload() {
        if (pendingDownloadUrl == null || pendingDownloadUrl.isBlank()) return;
        try {
            Desktop.getDesktop().browse(URI.create(pendingDownloadUrl));
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Cannot open browser: " + e.getMessage());
        }
    }

    // ========== Backup & Restore Tab ==========

    private void initBackupRestoreTab() {
        frequencyComboBox.setItems(FXCollections.observableArrayList("daily", "weekly", "monthly"));
        AutoBackupSettings settings = autoBackupService.loadSettings();
        autoBackupCheckBox.setSelected(settings.isEnabled());
        frequencyComboBox.setValue(settings.getFrequency());
        backupLocationField.setText(settings.getLocation());
        lastAutoBackupLabel.setText(settings.getLastRun().isBlank()
                ? "Never / 尚未執行" : settings.getLastRun());

        Path dataDir = Path.of(System.getProperty("user.home"), ".jig-standalone", "data");
        if (Files.exists(dataDir)) {
            dataStatusLabel.setText("✓  " + dataDir + "  (data found / 資料已存在)");
            dataStatusLabel.setStyle("-fx-text-fill: #15803d;");
        } else {
            dataStatusLabel.setText("✗  " + dataDir + "  (no data / 無資料)");
            dataStatusLabel.setStyle("-fx-text-fill: #dc2626;");
        }

        if (!settings.getLocation().isBlank()) {
            restoreFolderField.setText(settings.getLocation());
            refreshBackupList(Path.of(settings.getLocation()));
        }

        scheduleRestoreBtn.setDisable(true);
        backupListView.getSelectionModel().selectedIndexProperty().addListener(
                (obs, o, n) -> scheduleRestoreBtn.setDisable(n.intValue() < 0));
    }

    @FXML public void handleBrowseBackupLocation() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Backup Destination / 選擇備份目的地");
        java.io.File dir = dc.showDialog(getStage());
        if (dir != null) backupLocationField.setText(dir.getAbsolutePath());
    }

    @FXML public void handleBackupNow() {
        String loc = backupLocationField.getText().trim();
        if (loc.isBlank()) {
            showAlert(AlertType.WARNING, "No Location / 未選擇位置",
                    "Please select a backup destination first.\n請先選擇備份目的地。");
            return;
        }
        try {
            Path result = backupRestoreService.createBackup(Path.of(loc));
            showAlert(AlertType.INFORMATION, "Backup Complete / 備份完成",
                    "Backup created:\n備份已建立：\n\n" + result.toAbsolutePath());
            if (restoreFolderField.getText().equals(loc)) refreshBackupList(Path.of(loc));
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Backup Failed / 備份失敗", e.getMessage());
        }
    }

    @FXML public void handleSaveAutoBackupSettings() {
        AutoBackupSettings settings = autoBackupService.loadSettings();
        settings.setEnabled(autoBackupCheckBox.isSelected());
        settings.setFrequency(frequencyComboBox.getValue());
        settings.setLocation(backupLocationField.getText().trim());
        try {
            autoBackupService.saveSettings(settings);
            showAlert(AlertType.INFORMATION, "Saved / 已儲存",
                    "Auto-backup settings saved.\n自動備份設定已儲存。");
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Save Failed / 儲存失敗", e.getMessage());
        }
    }

    @FXML public void handleBrowseRestoreFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Backup Folder / 選擇備份資料夾");
        java.io.File dir = dc.showDialog(getStage());
        if (dir != null) {
            restoreFolderField.setText(dir.getAbsolutePath());
            refreshBackupList(dir.toPath());
        }
    }

    @FXML public void handleScheduleRestore() {
        int idx = backupListView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= availableBackupPaths.size()) return;
        Path selected = availableBackupPaths.get(idx);
        Alert confirm = new Alert(AlertType.WARNING,
                "Restore: " + selected.getFileName() + "\n\n" +
                "This will REPLACE all current data. App will exit and restore on next launch.\n" +
                "此操作將覆蓋現有資料。程式結束後下次啟動時自動還原。\n\nContinue? / 繼續？",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm Restore / 確認還原");
        confirm.initOwner(getStage());
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            try {
                backupRestoreService.markPendingRestore(selected);
                showAlert(AlertType.INFORMATION, "Scheduled / 已排程",
                        "App will now exit. Data restored on next launch.\n程式即將結束，下次啟動時將自動還原資料。");
                Platform.exit();
            } catch (IOException e) {
                showAlert(AlertType.ERROR, "Error / 錯誤", e.getMessage());
            }
        });
    }

    private void refreshBackupList(Path folder) {
        availableBackupPaths = backupRestoreService.listAvailableBackups(folder);
        backupListView.setItems(FXCollections.observableArrayList(
                availableBackupPaths.isEmpty()
                        ? List.of("No backups found / 找不到備份")
                        : availableBackupPaths.stream()
                                .map(p -> p.getFileName().toString()).toList()));
        scheduleRestoreBtn.setDisable(true);
    }

    // ========== Uninstall ==========

    @FXML public void handleOpenUninstallDialog() {
        try {
            Parent dialogRoot = fxmlLoader.load("/fxml/uninstall-dialog.fxml");
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Uninstall Application / 卸載應用程式");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(getStage());
            dialogStage.setResizable(false);
            Scene scene = new Scene(dialogRoot);
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error / 錯誤", "Failed to open uninstall dialog.");
        }
    }

    // ========== Helpers ==========

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.initOwner(getStage());
        alert.showAndWait();
    }

    private Stage getStage() {
        return (Stage) usersTable.getScene().getWindow();
    }
}
