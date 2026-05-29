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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.print.PageLayout;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.util.ArrayList;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class LogViewController {

    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FILE_FMT  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

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
            @Override public String toString(ActionTypeItem i)   { return i == null ? "" : i.label(); }
            @Override public ActionTypeItem fromString(String s) { return null; }
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
            new Alert(AlertType.INFORMATION,
                    "No data to export. Please search first. / 請先搜尋再匯出。", ButtonType.OK).showAndWait();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Log CSV / 匯出日誌");
        chooser.setInitialFileName("jig-logs-" + LocalDateTime.now().format(FILE_FMT) + ".csv");
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
            new Alert(AlertType.INFORMATION, "Exported successfully. / 匯出完成。", ButtonType.OK).showAndWait();
        } catch (IOException e) {
            new Alert(AlertType.ERROR, "Export failed: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void handlePrint() {
        if (currentResults.isEmpty()) {
            new Alert(AlertType.INFORMATION,
                    "No data to print. Search first. / 請先搜尋再列印。", ButtonType.OK).showAndWait();
            return;
        }

        List<Printer> printers = new ArrayList<>(Printer.getAllPrinters());
        if (printers.isEmpty()) {
            new Alert(AlertType.ERROR, "No printer configured. / 未設定印表機。", ButtonType.OK).showAndWait();
            return;
        }

        Printer chosen = showPrinterChooser(printers);
        if (chosen == null) return;

        PrinterJob job = PrinterJob.createPrinterJob(chosen);
        if (job == null) return;

        Node printNode = buildLogPrintNode();
        PageLayout layout = job.getJobSettings().getPageLayout();
        WritableImage img = printNode.snapshot(new SnapshotParameters(), null);
        ImageView iv = new ImageView(img);
        double scale = Math.min(
            layout.getPrintableWidth()  / img.getWidth(),
            layout.getPrintableHeight() / img.getHeight()
        );
        iv.setFitWidth(img.getWidth() * scale);
        iv.setFitHeight(img.getHeight() * scale);
        iv.setPreserveRatio(true);

        if (job.printPage(layout, iv)) {
            job.endJob();
            new Alert(AlertType.INFORMATION, "Sent to printer. / 已送出至印表機。", ButtonType.OK).showAndWait();
        } else {
            new Alert(AlertType.ERROR, "Print failed. / 列印失敗。", ButtonType.OK).showAndWait();
        }
    }

    private Printer showPrinterChooser(List<Printer> printers) {
        javafx.scene.control.Dialog<Printer> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Print / 列印");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Printer> combo = new ComboBox<>();
        combo.getItems().addAll(printers);
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Printer p)   { return p == null ? "" : p.getName(); }
            @Override public Printer fromString(String s) { return null; }
        });
        Printer def = Printer.getDefaultPrinter();
        combo.setValue(def != null && printers.contains(def) ? def : printers.get(0));
        combo.setPrefWidth(300);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(8,
            new Label("Select printer / 選擇印表機："), combo);
        content.setPadding(new Insets(12, 16, 4, 16));
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? combo.getValue() : null);
        return dialog.showAndWait().orElse(null);
    }

    // ── Print layout builder ──────────────────────────────────────────────────

    private Node buildLogPrintNode() {
        double[] colWidths = {130, 75, 130, 95, 215, 85};

        VBox root = new VBox(0);
        root.setPadding(new Insets(12));
        root.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        root.setPrefWidth(742);

        Label title = new Label("JT Activity Log Report");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#1e3a5f"));
        root.getChildren().add(title);

        Label info = new Label(buildPrintInfoLine());
        info.setFont(Font.font("System", 8));
        info.setTextFill(Color.GRAY);
        info.setPadding(new Insets(2, 0, 6, 0));
        root.getChildren().add(info);

        root.getChildren().add(buildLogRow(
            new String[]{"Date / 日期", "JT No.", "JT Name / 名稱", "Action / 動作", "Details / 詳情", "Operator / 操作者"},
            colWidths, true, true));

        for (int i = 0; i < currentResults.size(); i++) {
            JigLog log = currentResults.get(i);
            root.getChildren().add(buildLogRow(new String[]{
                log.getCreatedAt() != null ? log.getCreatedAt().format(DT_FMT) : "-",
                log.getJig() != null ? log.getJig().getJigNo() : "-",
                log.getJig() != null && log.getJig().getJigName() != null ? log.getJig().getJigName() : "-",
                actionTypeLabel(log.getActionType()),
                formatDetails(log).isEmpty() ? "-" : formatDetails(log),
                log.getUser() != null ? log.getUser().getUsername() : "-"
            }, colWidths, false, i % 2 == 0));
        }

        root.applyCss();
        root.layout();
        return root;
    }

    private HBox buildLogRow(String[] cells, double[] widths, boolean isHeader, boolean isLight) {
        HBox row = new HBox(0);
        Color bg = isHeader ? Color.web("#1e3a5f")
                 : (isLight ? Color.WHITE : Color.web("#f8fafc"));
        row.setBackground(new Background(new BackgroundFill(bg, CornerRadii.EMPTY, Insets.EMPTY)));
        for (int i = 0; i < cells.length; i++) {
            Label cell = new Label(cells[i]);
            cell.setPrefWidth(widths[i]);
            cell.setMinWidth(widths[i]);
            cell.setMaxWidth(widths[i]);
            cell.setPadding(new Insets(2, 4, 2, 4));
            cell.setFont(Font.font("System", isHeader ? FontWeight.BOLD : FontWeight.NORMAL, 8));
            cell.setTextFill(isHeader ? Color.WHITE : Color.web("#333333"));
            row.getChildren().add(cell);
        }
        return row;
    }

    private String buildPrintInfoLine() {
        String fromStr = fromDatePicker.getValue() != null ? fromDatePicker.getValue().toString() : "-";
        String toStr   = toDatePicker.getValue() != null   ? toDatePicker.getValue().toString()
                                                           : LocalDate.now().toString();
        ActionTypeItem typeItem = actionTypeFilter.getValue();
        String actionStr = (typeItem != null && typeItem.type() != null) ? typeItem.label() : "All Actions";
        String opStr = operatorFilter.getValue();
        if (opStr == null || opStr.isBlank()) opStr = "All Operators";
        return "Period: " + fromStr + " -> " + toStr
            + "   |   Action: " + actionStr
            + "   |   Operator: " + opStr
            + "   |   Records: " + currentResults.size()
            + "   |   Printed: " + LocalDateTime.now().format(DT_FMT);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
            case CREATE          -> "Create / 新增";
            case UPDATE          -> "Update / 編輯";
            case DELETE          -> "Delete / 刪除";
            case STATUS_CHANGE   -> "Status / 狀態變更";
            case DUE_DATE_CHANGE -> "Due Date / 到期日";
            case NOTE            -> "Note / 備註";
            case FILE_UPLOAD     -> "File Upload / 上傳";
            case FILE_REPLACE    -> "File Replace / 替換";
            case FILE_DELETE     -> "File Delete / 刪除檔案";
        };
    }

    private String actionBadgeStyle(JigLogActionType t) {
        if (t == null) return "";
        return switch (t) {
            case CREATE                          -> "jig-log-badge-create";
            case UPDATE, NOTE, DUE_DATE_CHANGE   -> "jig-log-badge-update";
            case DELETE, FILE_DELETE             -> "jig-log-badge-delete";
            case STATUS_CHANGE                   -> "jig-log-badge-status";
            case FILE_UPLOAD, FILE_REPLACE       -> "jig-log-badge-file";
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
