package com.jj.jig.ui.jig;

import com.jj.jig.auth.UserSession;
import com.jj.jig.jig.JigCsvImportService;
import com.jj.jig.jig.JigCsvImportService.ImportResult;
import com.jj.jig.jig.JigCsvImportService.ParsedRow;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class JigImportDialogController {

    @FXML private TextField  filePathField;
    @FXML private HBox       summaryBox;
    @FXML private Label      summaryLabel;
    @FXML private CheckBox   updateExistingCheck;
    @FXML private TableView<ParsedRow>              previewTable;
    @FXML private TableColumn<ParsedRow, String>    colLine;
    @FXML private TableColumn<ParsedRow, String>    colPreviewJigNo;
    @FXML private TableColumn<ParsedRow, String>    colPreviewModel;
    @FXML private TableColumn<ParsedRow, String>    colPreviewJigName;
    @FXML private TableColumn<ParsedRow, String>    colPreviewAction;
    @FXML private TableColumn<ParsedRow, String>    colPreviewError;
    @FXML private Button     importBtn;
    @FXML private HBox       errorBar;
    @FXML private Label      errorLabel;

    private List<ParsedRow> parsedRows;

    private final JigCsvImportService importService;
    private final UserSession         userSession;

    public JigImportDialogController(JigCsvImportService importService, UserSession userSession) {
        this.importService = importService;
        this.userSession   = userSession;
    }

    @FXML
    public void initialize() {
        setupPreviewColumns();
    }

    private void setupPreviewColumns() {
        colLine.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().lineNumber())));

        colPreviewJigNo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().jigNo()));
        colPreviewModel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().modelName()));
        colPreviewJigName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().jigName()));

        colPreviewError.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().validationError() != null ? d.getValue().validationError() : ""));

        // Action column with colored badge
        colPreviewAction.setCellValueFactory(d -> {
            if (d.getValue().validationError() != null) return new SimpleStringProperty("ERROR");
            return new SimpleStringProperty(d.getValue().isExisting() ? "UPDATE" : "NEW");
        });
        colPreviewAction.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String action, boolean empty) {
                super.updateItem(action, empty);
                setGraphic(null);
                setText(null);
                if (!empty && action != null) {
                    Label badge = new Label();
                    switch (action) {
                        case "NEW"    -> { badge.setText("New / 新增");    badge.getStyleClass().addAll("jig-import-badge", "jig-import-new"); }
                        case "UPDATE" -> { badge.setText("Update / 更新"); badge.getStyleClass().addAll("jig-import-badge", "jig-import-update"); }
                        default       -> { badge.setText("Error / 錯誤");  badge.getStyleClass().addAll("jig-import-badge", "jig-import-error"); }
                    }
                    setGraphic(badge);
                }
            }
        });
    }

    @FXML
    public void handleBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select CSV File / 選擇 CSV 檔案");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv", "*.CSV"));
        File file = chooser.showOpenDialog(getStage());
        if (file == null) return;

        filePathField.setText(file.getAbsolutePath());
        loadPreview(file.toPath());
    }

    private void loadPreview(Path csvPath) {
        hideError();
        try {
            parsedRows = importService.preview(csvPath);
        } catch (IOException e) {
            showError("Failed to read CSV: " + e.getMessage());
            importBtn.setDisable(true);
            return;
        }

        if (parsedRows.isEmpty()) {
            showError("No data rows found in the CSV. / CSV 中沒有資料列。");
            importBtn.setDisable(true);
            return;
        }

        previewTable.setItems(FXCollections.observableArrayList(parsedRows));
        previewTable.setVisible(true);
        previewTable.setManaged(true);

        long newCount    = parsedRows.stream().filter(r -> r.isValid() && !r.isExisting()).count();
        long updateCount = parsedRows.stream().filter(r -> r.isValid() && r.isExisting()).count();
        long errorCount  = parsedRows.stream().filter(r -> !r.isValid()).count();

        summaryLabel.setText(
                parsedRows.size() + " rows  ·  "
                + newCount + " new  ·  "
                + updateCount + " update  ·  "
                + errorCount + " error(s)");
        summaryBox.setVisible(true);
        summaryBox.setManaged(true);

        importBtn.setDisable(newCount == 0 && updateCount == 0);

        getStage().sizeToScene();
    }

    @FXML
    public void handleImport() {
        if (parsedRows == null || parsedRows.isEmpty()) return;
        hideError();

        boolean updateExisting = updateExistingCheck.isSelected();
        String username = userSession.getCurrentUser().getUsername();

        ImportResult result = importService.execute(parsedRows, updateExisting, username);

        // Show result summary
        StringBuilder msg = new StringBuilder();
        msg.append("Import complete / 匯入完成\n\n");
        msg.append("Created / 新增：").append(result.created()).append("\n");
        msg.append("Updated / 更新：").append(result.updated()).append("\n");
        msg.append("Skipped / 略過：").append(result.skipped()).append("\n");
        msg.append("Failed / 失敗：").append(result.failed()).append("\n");

        if (!result.errors().isEmpty()) {
            msg.append("\nErrors / 錯誤清單：\n");
            result.errors().stream().limit(10).forEach(e -> msg.append("• ").append(e).append("\n"));
            if (result.errors().size() > 10) {
                msg.append("... and ").append(result.errors().size() - 10).append(" more.");
            }
        }

        Alert resultAlert = new Alert(
                result.failed() > 0 && result.created() == 0 && result.updated() == 0
                        ? AlertType.ERROR : AlertType.INFORMATION,
                msg.toString(), ButtonType.OK);
        resultAlert.setTitle("Import Result / 匯入結果");
        resultAlert.setHeaderText(null);
        resultAlert.initOwner(getStage());
        resultAlert.showAndWait();

        if (result.created() > 0 || result.updated() > 0) {
            getStage().close();
        }
    }

    @FXML
    public void handleDownloadTemplate() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Import Template / 儲存匯入範本");
        chooser.setInitialFileName("jig-import-template.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(getStage());
        if (file == null) return;
        try {
            Files.write(file.toPath(), importService.generateTemplate());
            Alert ok = new Alert(AlertType.INFORMATION, "Template saved. / 範本已儲存。", ButtonType.OK);
            ok.setHeaderText(null);
            ok.initOwner(getStage());
            ok.showAndWait();
        } catch (IOException e) {
            showError("Failed to save template: " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        getStage().close();
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

    private Stage getStage() {
        return (Stage) filePathField.getScene().getWindow();
    }
}
