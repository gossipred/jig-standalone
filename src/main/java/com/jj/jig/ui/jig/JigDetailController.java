package com.jj.jig.ui.jig;

import com.jj.jig.auth.UserSession;
import com.jj.jig.jig.Jig;
import com.jj.jig.jig.JigFile;
import com.jj.jig.jig.JigService;
import com.jj.jig.jig.JigStatus;
import com.jj.jig.log.JigLog;
import com.jj.jig.log.JigLogActionType;
import com.jj.jig.ui.SpringFxmlLoader;
import com.jj.jig.user.UserRole;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class JigDetailController {

    // Header
    @FXML private Label  detailJigNoLabel;
    @FXML private Label  detailJigNameLabel;
    @FXML private Label  detailStatusBadge;
    @FXML private Button detailEditBtn;
    @FXML private Button detailStatusBtn;

    // Info Grid
    @FXML private GridPane infoGrid;

    // Files section
    @FXML private Button                          uploadFilesBtn;
    @FXML private Label                           fileSummaryLabel;
    @FXML private TableView<JigFile>              filesTable;
    @FXML private TableColumn<JigFile, String>    colFileName;
    @FXML private TableColumn<JigFile, String>    colFileSize;
    @FXML private TableColumn<JigFile, String>    colUploadedAt;
    @FXML private TableColumn<JigFile, Void>      colFileActions;

    // Logs section
    @FXML private TableView<JigLog>               logsTable;
    @FXML private TableColumn<JigLog, String>     colLogDate;
    @FXML private TableColumn<JigLog, String>     colLogAction;
    @FXML private TableColumn<JigLog, String>     colLogDetails;
    @FXML private TableColumn<JigLog, String>     colLogOperator;

    private Long jigId;

    private static final DateTimeFormatter DATE_FMT        = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT    = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
    private static final DateTimeFormatter FULL_DT_FMT     = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JigService      jigService;
    private final UserSession     userSession;
    private final SpringFxmlLoader fxmlLoader;

    public JigDetailController(JigService jigService, UserSession userSession,
                               SpringFxmlLoader fxmlLoader) {
        this.jigService  = jigService;
        this.userSession = userSession;
        this.fxmlLoader  = fxmlLoader;
    }

    @FXML
    public void initialize() {
        setupLogColumns();
        setupFilesColumns();
    }

    public void initDetail(Long jigId) {
        this.jigId = jigId;
        setupRoleAccess();
        reload();
    }

    // ===== Role Access =====

    private void setupRoleAccess() {
        UserRole role = userSession.getCurrentUser().getRole();
        boolean canAct = role == UserRole.ADMIN || role == UserRole.SUPERVISOR || role == UserRole.ENGINEER;
        detailEditBtn.setVisible(canAct);
        detailEditBtn.setManaged(canAct);
        detailStatusBtn.setVisible(canAct);
        detailStatusBtn.setManaged(canAct);
        uploadFilesBtn.setVisible(canAct);
        uploadFilesBtn.setManaged(canAct);
        if (!canAct) {
            colFileActions.setVisible(false);
        }
    }

    // ===== Reload =====

    private void reload() {
        Jig jig = jigService.findById(jigId);
        populateHeader(jig);
        populateInfoGrid(jig);
        loadFiles();
        loadLogs();
    }

    // ===== Header =====

    private void populateHeader(Jig jig) {
        detailJigNoLabel.setText(jig.getJigNo());
        detailJigNameLabel.setText(jig.getJigName());
        JigStatus status = jig.getStatus() != null ? jig.getStatus() : JigStatus.Normal;
        detailStatusBadge.setText(status.getLabel() + " / " + status.getLabelZh());
        detailStatusBadge.getStyleClass().removeIf(c -> c.startsWith("jig-status-"));
        detailStatusBadge.getStyleClass().add("jig-status-" + status.getCssClass());
    }

    // ===== Info Grid =====

    private void populateInfoGrid(Jig jig) {
        infoGrid.getChildren().clear();
        int row = 0;
        addInfoRow(row++, "JT No.", jig.getJigNo(), "Model Name / 型號", jig.getModelName());
        addInfoRow(row++, "JT Name / 治模具名稱", jig.getJigName(), "Classification / 分類", jig.getClassification());
        addInfoRow(row++, "Customer / 客戶", jig.getCustomer(), "Assembly Line / 線別", jig.getAssemblyLine());
        addInfoRow(row++,
                "Quantity / 數量", jig.getQuantity() != null ? String.valueOf(jig.getQuantity()) : "—",
                "DRI / 負責人", jig.getDri());
        addInfoRow(row++,
                "Start Date / 啟用日", jig.getStartDate() != null ? jig.getStartDate().format(DATE_FMT) : "—",
                "Due Date / 到期日",   jig.getDueDate()   != null ? jig.getDueDate().format(DATE_FMT)   : "—");
        addInfoRow(row++, "MRO No.", jig.getMroNo(), "PR No.", jig.getPrNo());
        addInfoRow(row++, "Note / 備註", jig.getNote(), "", null);
        addInfoRow(row++,
                "Created By / 建立者",    jig.getCreatedBy()  != null ? jig.getCreatedBy().getUsername()  : "—",
                "Created At / 建立時間", jig.getCreatedAt()  != null ? jig.getCreatedAt().format(FULL_DT_FMT)  : "—");
        addInfoRow(row,
                "Updated By / 最後修改者", jig.getUpdatedBy()  != null ? jig.getUpdatedBy().getUsername()  : "—",
                "Updated At / 最後修改", jig.getUpdatedAt()  != null ? jig.getUpdatedAt().format(FULL_DT_FMT)  : "—");
    }

    private void addInfoRow(int row, String l1, String v1, String l2, String v2) {
        Label label1 = new Label(l1);
        label1.getStyleClass().add("jig-detail-field-label");

        Label value1 = new Label(v1 != null && !v1.isBlank() ? v1 : "—");
        value1.getStyleClass().add("jig-detail-field-value");
        value1.setWrapText(true);

        Label label2 = new Label(l2);
        label2.getStyleClass().add("jig-detail-field-label");

        Label value2 = new Label(v2 != null && !v2.isBlank() ? v2 : "—");
        value2.getStyleClass().add("jig-detail-field-value");
        value2.setWrapText(true);

        infoGrid.add(label1, 0, row);
        infoGrid.add(value1, 1, row);
        infoGrid.add(label2, 2, row);
        infoGrid.add(value2, 3, row);
    }

    // ===== Files =====

    private void setupFilesColumns() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        colFileName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getOriginalFilename()));

        colFileSize.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFileSize() != null
                        ? formatFileSize(d.getValue().getFileSize()) : "—"));

        colUploadedAt.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getUploadedAt() != null
                        ? d.getValue().getUploadedAt().format(dtf) : "—"));

        colFileActions.setCellFactory(col -> new TableCell<>() {
            private final Button openBtn   = new Button("Open / 開啟");
            private final Button deleteBtn = new Button("✕ Del");
            private final HBox   box       = new HBox(6, openBtn, deleteBtn);
            {
                openBtn.getStyleClass().add("jig-btn-outline-sm");
                deleteBtn.getStyleClass().add("jig-btn-danger-sm");
                box.setAlignment(Pos.CENTER_LEFT);
                openBtn.setOnAction(e -> {
                    JigFile f = getTableRow().getItem();
                    if (f != null) openFile(f);
                });
                deleteBtn.setOnAction(e -> {
                    JigFile f = getTableRow().getItem();
                    if (f != null) confirmDeleteFile(f);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadFiles() {
        List<JigFile> files = jigService.findFiles(jigId);
        filesTable.setItems(FXCollections.observableArrayList(files));
        long totalSize = files.stream()
                .mapToLong(f -> f.getFileSize() != null ? f.getFileSize() : 0L)
                .sum();
        fileSummaryLabel.setText(files.size() + " / 11 files  ·  " + formatFileSize(totalSize) + " / 100 MB");
    }

    @FXML
    public void handleUploadFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Upload / 選擇要上傳的附件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files / 所有檔案", "*.*"),
                new FileChooser.ExtensionFilter("Images / 圖片", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Documents / 文件", "*.pdf", "*.dwg", "*.dxf", "*.doc", "*.docx", "*.xls", "*.xlsx")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(getStage());
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return;
        }

        List<Path> filePaths = selectedFiles.stream().map(File::toPath).toList();
        try {
            jigService.uploadFiles(jigId, filePaths, userSession.getCurrentUser().getUsername());
            loadFiles();
            loadLogs();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Upload Failed / 上傳失敗",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    private void openFile(JigFile jigFile) {
        Path basePath = Path.of(System.getProperty("user.home"), ".jig-standalone");
        Path filePath = basePath.resolve(jigFile.getStoredPath()).normalize();

        if (!Files.exists(filePath)) {
            showAlert(AlertType.WARNING, "File Not Found / 找不到檔案",
                    "The file no longer exists on disk.\n該檔案已不存在，可能已被移動或刪除。");
            return;
        }

        // Desktop.open() must run on a non-JavaFX thread to avoid AppKit conflicts on macOS
        java.io.File fileToOpen = filePath.toFile();
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(fileToOpen);
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        showAlert(AlertType.ERROR, "Error / 錯誤", "Failed to open file: " + e.getMessage()));
            }
        }, "file-open-thread").start();
    }

    private void confirmDeleteFile(JigFile jigFile) {
        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Delete \"" + jigFile.getOriginalFilename() + "\"?\n"
                + "刪除附件 \"" + jigFile.getOriginalFilename() + "\"？",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm Delete / 確認刪除");
        confirm.setHeaderText(null);
        confirm.initOwner(getStage());
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    jigService.deleteFile(jigFile.getId(), userSession.getCurrentUser().getUsername());
                    loadFiles();
                    loadLogs();
                } catch (Exception e) {
                    showAlert(AlertType.ERROR, "Delete Failed / 刪除失敗",
                            e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                }
            }
        });
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    // ===== Logs =====

    private void setupLogColumns() {
        colLogDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().format(DATETIME_FMT) : ""));

        colLogAction.setCellValueFactory(d -> new SimpleStringProperty(
                formatActionType(d.getValue().getActionType())));

        colLogDetails.setCellValueFactory(d -> new SimpleStringProperty(
                formatLogDetails(d.getValue())));
        colLogDetails.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setWrapText(true);
            }
        });

        colLogOperator.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getUser() != null ? d.getValue().getUser().getUsername() : "—"));
    }

    private void loadLogs() {
        List<JigLog> logs = jigService.findLogs(jigId);
        logsTable.setItems(FXCollections.observableArrayList(logs));
    }

    private String formatActionType(JigLogActionType type) {
        if (type == null) return "";
        return switch (type) {
            case CREATE        -> "Created / 新增";
            case UPDATE        -> "Updated / 修改";
            case DELETE        -> "Deleted / 刪除";
            case FILE_UPLOAD   -> "File Uploaded / 上傳附件";
            case FILE_REPLACE  -> "File Replaced / 替換附件";
            case FILE_DELETE   -> "File Deleted / 刪除附件";
            case STATUS_CHANGE -> "Status Change / 狀態變更";
            case DUE_DATE_CHANGE -> "Due Date / 到期日變更";
            case NOTE          -> "Note / 備註";
        };
    }

    private String formatLogDetails(JigLog log) {
        if (log.getActionType() == null) return "";
        StringBuilder sb = new StringBuilder();

        if (log.getActionType() == JigLogActionType.STATUS_CHANGE) {
            String oldS = log.getOldStatus() != null ? log.getOldStatus().getLabel() : "—";
            String newS = log.getNewStatus() != null ? log.getNewStatus().getLabel() : "—";
            sb.append(oldS).append(" → ").append(newS);
        } else if (log.getActionType() == JigLogActionType.DUE_DATE_CHANGE) {
            String oldD = log.getOldDueDate() != null ? log.getOldDueDate().format(DATE_FMT) : "—";
            String newD = log.getNewDueDate() != null ? log.getNewDueDate().format(DATE_FMT) : "—";
            sb.append(oldD).append(" → ").append(newD);
        }

        if (log.getNote() != null && !log.getNote().isBlank()) {
            if (!sb.isEmpty()) sb.append("  ");
            sb.append(log.getNote());
        }
        return sb.toString();
    }

    // ===== Sub-dialogs =====

    @FXML
    public void handleEdit() {
        try {
            FXMLLoader loader = fxmlLoader.createLoader("/fxml/jig-form-dialog.fxml");
            Parent root = loader.load();
            JigFormDialogController formController = loader.getController();
            formController.initForEdit(jigId);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit JT / 編輯治模具");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(getStage());
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            reload();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error / 錯誤", "Failed to open edit form.");
        }
    }

    @FXML
    public void handleChangeStatus() {
        try {
            FXMLLoader loader = fxmlLoader.createLoader("/fxml/jig-status-dialog.fxml");
            Parent root = loader.load();
            JigStatusDialogController statusController = loader.getController();
            statusController.initDialog(jigId);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Change Status / 變更狀態");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(getStage());
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            reload();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error / 錯誤", "Failed to open status dialog.");
        }
    }

    @FXML
    public void handleRefreshLogs() {
        loadLogs();
    }

    @FXML
    public void handleClose() {
        getStage().close();
    }

    // ===== Helpers =====

    private Stage getStage() {
        return (Stage) detailJigNoLabel.getScene().getWindow();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.initOwner(getStage());
        alert.showAndWait();
    }
}
