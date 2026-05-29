package com.jj.jig.ui.jig;

import com.jj.jig.auth.UserSession;
import com.jj.jig.jig.Jig;
import com.jj.jig.jig.JigFile;
import com.jj.jig.jig.JigForm;
import com.jj.jig.jig.JigService;
import com.jj.jig.jig.JigStatus;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class JigFormDialogController {

    @FXML private Label    dialogTitleLabel;
    @FXML private Label    dialogTitleZhLabel;
    @FXML private HBox     jigNoAutoRow;
    @FXML private TextField jigNoPrefixField;
    @FXML private TextField jigNoField;
    @FXML private TextField modelNameField;
    @FXML private TextField jigNameField;
    @FXML private TextField classificationField;
    @FXML private TextField customerField;
    @FXML private TextField assemblyLineField;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private ComboBox<JigStatus> statusCombo;
    @FXML private TextField driField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker dueDatePicker;
    @FXML private TextField mroNoField;
    @FXML private TextField prNoField;
    @FXML private TextArea  noteField;
    // Files section
    @FXML private VBox      filesSection;
    @FXML private Label     filesSectionLabel;
    @FXML private VBox      existingFilesBox;
    @FXML private Label     selectedFilesCountLabel;
    @FXML private VBox      selectedFilesBox;
    // Error bar
    @FXML private HBox      errorBar;
    @FXML private Label     errorLabel;

    private Long        editingJigId;
    private final List<Path> selectedFiles = new ArrayList<>();

    private final JigService  jigService;
    private final UserSession userSession;

    public JigFormDialogController(JigService jigService, UserSession userSession) {
        this.jigService  = jigService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        quantitySpinner.setEditable(true);

        statusCombo.getItems().addAll(JigStatus.values());
        statusCombo.setConverter(new StringConverter<>() {
            @Override public String toString(JigStatus s) {
                return s == null ? "" : s.getLabel() + " / " + s.getLabelZh();
            }
            @Override public JigStatus fromString(String s) { return null; }
        });
        statusCombo.getSelectionModel().select(JigStatus.Normal);
    }

    // ===== Init =====

    public void initForCreate() {
        editingJigId = null;
        dialogTitleLabel.setText("New JT");
        dialogTitleZhLabel.setText("新增治模具");
        jigNoAutoRow.setVisible(true);
        jigNoAutoRow.setManaged(true);
        jigNoPrefixField.setText("AM-ME");
        jigNoField.setEditable(true);
        jigNoField.getStyleClass().remove("jig-form-field-readonly");
        driField.setText(userSession.getCurrentUser().getUsername());
        selectedFiles.clear();
        filesSectionLabel.setText("Files / 附件（選填，最多 11 個 · 100MB）");
        existingFilesBox.getChildren().clear();
        filesSection.setVisible(true);
        filesSection.setManaged(true);
        refreshSelectedFilesDisplay();
    }

    public void initForEdit(Long jigId) {
        editingJigId = jigId;
        Jig jig = jigService.findById(jigId);
        dialogTitleLabel.setText("Edit JT");
        dialogTitleZhLabel.setText("編輯治模具");
        jigNoAutoRow.setVisible(false);
        jigNoAutoRow.setManaged(false);
        jigNoField.setText(jig.getJigNo());
        jigNoField.setEditable(false);
        if (!jigNoField.getStyleClass().contains("jig-form-field-readonly")) {
            jigNoField.getStyleClass().add("jig-form-field-readonly");
        }
        modelNameField.setText(jig.getModelName());
        jigNameField.setText(jig.getJigName());
        classificationField.setText(nullToEmpty(jig.getClassification()));
        customerField.setText(nullToEmpty(jig.getCustomer()));
        assemblyLineField.setText(nullToEmpty(jig.getAssemblyLine()));
        quantitySpinner.getValueFactory().setValue(jig.getQuantity() != null ? jig.getQuantity() : 1);
        statusCombo.getSelectionModel().select(jig.getStatus() != null ? jig.getStatus() : JigStatus.Normal);
        driField.setText(nullToEmpty(jig.getDri()));
        noteField.setText(nullToEmpty(jig.getNote()));
        startDatePicker.setValue(jig.getStartDate());
        dueDatePicker.setValue(jig.getDueDate());
        mroNoField.setText(nullToEmpty(jig.getMroNo()));
        prNoField.setText(nullToEmpty(jig.getPrNo()));
        selectedFiles.clear();
        filesSectionLabel.setText("Files / 附件管理（最多 11 個 · 100MB）");
        filesSection.setVisible(true);
        filesSection.setManaged(true);
        loadExistingFiles(jigId);
        refreshSelectedFilesDisplay();
    }

    // ===== Existing Files (Edit mode) =====

    private void loadExistingFiles(Long jigId) {
        existingFilesBox.getChildren().clear();
        List<JigFile> files = jigService.findFiles(jigId);

        if (files.isEmpty()) {
            Label empty = new Label("No files attached yet / 尚未上傳附件");
            empty.getStyleClass().add("jig-form-hint");
            existingFilesBox.getChildren().add(empty);
            return;
        }

        long totalBytes = files.stream().mapToLong(f -> f.getFileSize() != null ? f.getFileSize() : 0L).sum();
        Label header = new Label(files.size() + " existing file(s)  ·  " + formatSize(totalBytes));
        header.getStyleClass().add("jig-form-hint");
        existingFilesBox.getChildren().add(header);

        for (JigFile f : files) {
            Label nameLabel = new Label(f.getOriginalFilename());
            nameLabel.getStyleClass().add("jig-form-hint");
            nameLabel.setMaxWidth(Double.MAX_VALUE);

            Label sizeLabel = new Label(formatSize(f.getFileSize() != null ? f.getFileSize() : 0L));
            sizeLabel.getStyleClass().add("jig-form-hint");

            Button openBtn = new Button("Open / 開啟");
            openBtn.getStyleClass().add("jig-btn-outline-sm");
            openBtn.setOnAction(e -> openFile(f));

            Button delBtn = new Button("✕");
            delBtn.getStyleClass().add("jig-btn-danger-sm");
            delBtn.setOnAction(e -> confirmDeleteFile(f, jigId));

            Region spacer = new Region();
            HBox row = new HBox(6, nameLabel, spacer, sizeLabel, openBtn, delBtn);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("jig-file-row");
            existingFilesBox.getChildren().add(row);
        }
    }

    private void openFile(JigFile jigFile) {
        Path base = Path.of(System.getProperty("user.home"), ".jig-standalone");
        Path filePath = base.resolve(jigFile.getStoredPath()).normalize();
        if (!Files.exists(filePath)) {
            showError("File not found on disk. / 找不到檔案，可能已被移動或刪除。");
            return;
        }
        java.io.File fileToOpen = filePath.toFile();
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(fileToOpen);
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        showError("Cannot open file: " + e.getMessage()));
            }
        }, "file-open-thread").start();
    }

    private void confirmDeleteFile(JigFile jigFile, Long jigId) {
        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Delete \"" + jigFile.getOriginalFilename() + "\"?\n刪除附件 \"" + jigFile.getOriginalFilename() + "\"？",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm Delete / 確認刪除");
        confirm.setHeaderText(null);
        confirm.initOwner(getStage());
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    jigService.deleteFile(jigFile.getId(), userSession.getCurrentUser().getUsername());
                    loadExistingFiles(jigId);
                } catch (Exception e) {
                    showError("Delete failed: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
                }
            }
        });
    }

    // ===== New File Selection =====

    @FXML
    public void handleAddFiles() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Files / 選擇附件");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files / 所有檔案", "*.*"),
                new FileChooser.ExtensionFilter("Images / 圖片", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Documents / 文件", "*.pdf", "*.dwg", "*.dxf", "*.doc", "*.docx", "*.xls", "*.xlsx")
        );
        List<File> picked = chooser.showOpenMultipleDialog(getStage());
        if (picked == null || picked.isEmpty()) return;
        for (File f : picked) {
            Path p = f.toPath();
            if (!selectedFiles.contains(p)) selectedFiles.add(p);
        }
        refreshSelectedFilesDisplay();
    }

    private void refreshSelectedFilesDisplay() {
        selectedFilesBox.getChildren().clear();
        if (selectedFiles.isEmpty()) {
            selectedFilesCountLabel.setText("No new files selected / 尚未選擇新附件");
            return;
        }
        long totalBytes = 0;
        for (Path p : selectedFiles) {
            try { totalBytes += Files.size(p); } catch (IOException ignored) {}
        }
        selectedFilesCountLabel.setText(selectedFiles.size() + " new file(s)  ·  " + formatSize(totalBytes));

        for (int i = 0; i < selectedFiles.size(); i++) {
            final int idx = i;
            Path p = selectedFiles.get(i);
            long sz = 0;
            try { sz = Files.size(p); } catch (IOException ignored) {}

            Label nameLabel = new Label(p.getFileName().toString());
            nameLabel.getStyleClass().add("jig-form-hint");
            nameLabel.setMaxWidth(Double.MAX_VALUE);

            Label sizeLabel = new Label(formatSize(sz));
            sizeLabel.getStyleClass().add("jig-form-hint");

            Button removeBtn = new Button("✕");
            removeBtn.getStyleClass().add("jig-btn-danger-sm");
            removeBtn.setOnAction(e -> {
                selectedFiles.remove(idx);
                refreshSelectedFilesDisplay();
            });

            Region spacer = new Region();
            HBox row = new HBox(6, nameLabel, spacer, sizeLabel, removeBtn);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("jig-file-row");
            selectedFilesBox.getChildren().add(row);
        }
    }

    // ===== Form Actions =====

    @FXML
    public void handleAutoGenerateJigNo() {
        String prefix = jigNoPrefixField.getText().trim();
        try {
            jigNoField.setText(jigService.nextJigNo(prefix.isEmpty() ? "AM-ME" : prefix));
            hideError();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        hideError();
        String jigNo     = jigNoField.getText().trim();
        String modelName = modelNameField.getText().trim();
        String jigName   = jigNameField.getText().trim();

        if (jigNo.isEmpty())     { showError("JT No. is required. / 治模具編號為必填項目。"); return; }
        if (modelName.isEmpty()) { showError("Model Name is required. / 型號為必填項目。"); return; }
        if (jigName.isEmpty())   { showError("JT Name is required. / 治模具名稱為必填項目。"); return; }

        if (!selectedFiles.isEmpty()) {
            if (selectedFiles.size() > 11) {
                showError("At most 11 files allowed. / 最多只能選 11 個附件。");
                return;
            }
            long totalBytes = 0;
            for (Path p : selectedFiles) {
                try { totalBytes += Files.size(p); } catch (IOException ignored) {}
            }
            if (totalBytes > 100L * 1024 * 1024) {
                showError("Total file size exceeds 100MB. / 附件總大小超過 100MB。");
                return;
            }
        }

        JigForm form = buildForm(jigNo, modelName, jigName);
        try {
            String username = userSession.getCurrentUser().getUsername();
            Jig savedJig;
            if (editingJigId == null) {
                savedJig = jigService.create(form, username);
            } else {
                savedJig = jigService.update(editingJigId, form, username);
            }
            if (!selectedFiles.isEmpty()) {
                jigService.uploadFiles(savedJig.getId(), selectedFiles, username);
            }
            getStage().close();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        getStage().close();
    }

    // ===== Helpers =====

    private JigForm buildForm(String jigNo, String modelName, String jigName) {
        JigForm form = new JigForm();
        form.setJigNo(jigNo);
        form.setModelName(modelName);
        form.setJigName(jigName);
        form.setClassification(classificationField.getText().trim());
        form.setCustomer(customerField.getText().trim());
        form.setAssemblyLine(assemblyLineField.getText().trim());
        form.setQuantity(quantitySpinner.getValue() != null ? quantitySpinner.getValue() : 1);
        form.setStatus(statusCombo.getValue() != null ? statusCombo.getValue() : JigStatus.Normal);
        form.setDri(driField.getText().trim());
        form.setNote(noteField.getText().trim());
        form.setStartDate(startDatePicker.getValue());
        form.setDueDate(dueDatePicker.getValue());
        form.setMroNo(mroNoField.getText().trim());
        form.setPrNo(prNoField.getText().trim());
        return form;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorBar.setVisible(true);
        errorBar.setManaged(true);
    }

    private void hideError() {
        errorBar.setVisible(false);
        errorBar.setManaged(false);
    }

    private String nullToEmpty(String v) { return v == null ? "" : v; }

    private String formatSize(long bytes) {
        if (bytes < 1024)        return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    private Stage getStage() {
        return (Stage) modelNameField.getScene().getWindow();
    }
}
