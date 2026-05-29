package com.jj.jig.ui.log;

import com.jj.jig.jig.JigService;
import com.jj.jig.jig.JigStatus;
import com.jj.jig.log.JigLog;
import com.jj.jig.log.JigLogActionType;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class LogViewController {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<ActionTypeItem> actionTypeFilter;
    @FXML private ComboBox<String> operatorFilter;
    @FXML private TableView<JigLog> logsTable;
    @FXML private TableColumn<JigLog, String> colDate;
    @FXML private TableColumn<JigLog, String> colJigNo;
    @FXML private TableColumn<JigLog, String> colJigName;
    @FXML private TableColumn<JigLog, String> colAction;
    @FXML private TableColumn<JigLog, String> colDetails;
    @FXML private TableColumn<JigLog, String> colOperator;
    @FXML private Label countLabel;

    private List<JigLog> currentResults = List.of();

    private final JigService jigService;

    public LogViewController(JigService jigService) {
        this.jigService = jigService;
    }

    @FXML
    public void initialize() {
        setupColumns();
        setupFilters();
    }

    private void setupFilters() {
        List<ActionTypeItem> items = new ArrayList<>();
        items.add(new ActionTypeItem(null, "All Actions / 全部動作"));
        for (JigLogActionType t : JigLogActionType.values()) {
            items.add(new ActionTypeItem(t, actionTypeLabel(t)));
        }
        actionTypeFilter.setItems(FXCollections.observableArrayList(items));
        actionTypeFilter.setConverter(new StringConverter<>() {
            @Override public String toString(ActionTypeItem i)    { return i == null ? "" : i.label(); }
            @Override public ActionTypeItem fromString(String s)  { return null; }
        });
        actionTypeFilter.getSelectionModel().selectFirst();

        List<String> operators = new ArrayList<>();
        operators.add("");
        operators.addAll(jigService.getAllUsernames());
        operatorFilter.setItems(FXCollections.observableArrayList(operators));
        operatorFilter.setConverter(new StringConverter<>() {
            @Override public String toString(String s)  { return s == null || s.isBlank() ? "All Operators / 全部" : s; }
            @Override public String fromString(String s) { return s; }
        });
        operatorFilter.getSelectionModel().selectFirst();
    }

    private void setupColumns() {
        colDate.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCreatedAt() != null
                        ? d.getValue().getCreatedAt().format(DT_FMT) : ""));

        colJigNo.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getJig() != null
                        ? d.getValue().getJig().getJigNo() : ""));

        colJigName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getJig() != null
                        ? d.getValue().getJig().getJigName() : ""));

        colAction.setCellValueFactory(d ->
                new SimpleStringProperty(actionTypeLabel(d.getValue().getActionType())));
        colAction.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String text, boolean empty) {
                super.updateItem(text, empty);
                setGraphic(null); setText(null);
                if (!empty && text != null) {
                    Label badge = new Label(text);
                    JigLog log = getTableRow() != null ? (JigLog) getTableRow().getItem() : null;
                    badge.getStyleClass().add("jig-log-action-badge");
                    if (log != null) badge.getStyleClass().add(actionBadgeStyle(log.getActionType()));
                    setGraphic(badge);
                }
            }
        });

        colDetails.setCellValueFactory(d ->
                new SimpleStringProperty(formatDetails(d.getValue())));

        colOperator.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getUser() != null
                        ? d.getValue().getUser().getUsername() : "—"));
    }

    @FXML
    public void handleSearch() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to   = toDatePicker.getValue();

        // 未填日期時預設最近 7 天
        if (from == null && to == null) {
            from = LocalDate.now().minusDays(6);
            fromDatePicker.setValue(from);
        }

        ActionTypeItem typeItem = actionTypeFilter.getValue();
        JigLogActionType actionType = typeItem != null ? typeItem.type() : null;
        String operator = operatorFilter.getValue();

        currentResults = jigService.searchLogs(from, to, actionType, operator);
        logsTable.setItems(FXCollections.observableArrayList(currentResults));
        countLabel.setText(currentResults.size() + " record(s) / 筆");
    }

    @FXML
    public void handleClear() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        actionTypeFilter.getSelectionModel().selectFirst();
        operatorFilter.getSelectionModel().selectFirst();
        logsTable.getItems().clear();
        currentResults = List.of();
        countLabel.setText("");
    }

    @FXML
    public void handleExportCsv() {
        if (currentResults.isEmpty()) {
            Alert alert = new Alert(AlertType.INFORMATION,
                    "No data to export. Please search first. / 請先搜尋再匯出。", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Log CSV / 匯出日誌");
        chooser.setInitialFileName("jig-logs-" + java.time.LocalDateTime.now().format(FILE_FMT) + ".csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(getStage());
        if (file == null) return;

        try {
            List<String> lines = new ArrayList<>();
            lines.add("Date,JT No.,JT Name,Action,Details,Operator");
            for (JigLog log : currentResults) {
                lines.add(String.join(",",
                        csv(log.getCreatedAt() != null ? log.getCreatedAt().format(DT_FMT) : ""),
                        csv(log.getJig() != null ? log.getJig().getJigNo() : ""),
                        csv(log.getJig() != null ? log.getJig().getJigName() : ""),
                        csv(actionTypeLabel(log.getActionType())),
                        csv(formatDetails(log)),
                        csv(log.getUser() != null ? log.getUser().getUsername() : "")));
            }
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);

            Alert ok = new Alert(AlertType.INFORMATION, "Exported successfully. / 匯出完成。", ButtonType.OK);
            ok.setHeaderText(null);
            ok.showAndWait();
        } catch (IOException e) {
            Alert err = new Alert(AlertType.ERROR, "Export failed: " + e.getMessage(), ButtonType.OK);
            err.setHeaderText(null);
            err.showAndWait();
        }
    }

    private String formatDetails(JigLog log) {
        if (log.getActionType() == null) return "";
        StringBuilder sb = new StringBuilder();
        if (log.getActionType() == JigLogActionType.STATUS_CHANGE) {
            String oldS = log.getOldStatus() != null ? statusLabel(log.getOldStatus()) : "—";
            String newS = log.getNewStatus() != null ? statusLabel(log.getNewStatus()) : "—";
            sb.append(oldS).append(" → ").append(newS);
        } else if (log.getActionType() == JigLogActionType.DUE_DATE_CHANGE) {
            String oldD = log.getOldDueDate() != null ? log.getOldDueDate().toString() : "—";
            String newD = log.getNewDueDate() != null ? log.getNewDueDate().toString() : "—";
            sb.append(oldD).append(" → ").append(newD);
        }
        if (log.getNote() != null && !log.getNote().isBlank()) {
            if (!sb.isEmpty()) sb.append("  ");
            sb.append(log.getNote());
        }
        return sb.toString();
    }

    private String actionTypeLabel(JigLogActionType t) {
        if (t == null) return "";
        return switch (t) {
            case CREATE         -> "Create / 新增";
            case UPDATE         -> "Update / 編輯";
            case DELETE         -> "Delete / 刪除";
            case STATUS_CHANGE  -> "Status / 狀態變更";
            case DUE_DATE_CHANGE -> "Due Date / 到期日";
            case NOTE           -> "Note / 備註";
            case FILE_UPLOAD    -> "File Upload / 上傳";
            case FILE_REPLACE   -> "File Replace / 替換";
            case FILE_DELETE    -> "File Delete / 刪除檔案";
        };
    }

    private String actionBadgeStyle(JigLogActionType t) {
        if (t == null) return "";
        return switch (t) {
            case CREATE                      -> "jig-log-badge-create";
            case UPDATE, NOTE, DUE_DATE_CHANGE -> "jig-log-badge-update";
            case DELETE, FILE_DELETE         -> "jig-log-badge-delete";
            case STATUS_CHANGE               -> "jig-log-badge-status";
            case FILE_UPLOAD, FILE_REPLACE   -> "jig-log-badge-file";
        };
    }

    private String statusLabel(JigStatus s) {
        return s.getLabel() + " / " + s.getLabelZh();
    }

    private String csv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private Stage getStage() {
        return (Stage) logsTable.getScene().getWindow();
    }

    record ActionTypeItem(JigLogActionType type, String label) {}
}
