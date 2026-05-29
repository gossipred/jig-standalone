package com.jj.jig.ui.stats;

import com.jj.jig.jig.JigService;
import com.jj.jig.jig.JigStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.print.PageLayout;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class StatsViewController {

    @FXML private PieChart statusPieChart;
    @FXML private Label totalLabel;
    @FXML private VBox statsCardBox;

    private Map<JigStatus, Long> lastStats;
    private long lastTotal;

    private final JigService jigService;

    public StatsViewController(JigService jigService) {
        this.jigService = jigService;
    }

    @FXML
    public void initialize() {
        loadStats();
    }

    @FXML
    public void handleRefresh() {
        loadStats();
    }

    @FXML
    public void handlePrint() {
        List<Printer> printers = new ArrayList<>(Printer.getAllPrinters());
        if (printers.isEmpty()) {
            new Alert(AlertType.ERROR, "No printer configured. / 未設定印表機。", ButtonType.OK).showAndWait();
            return;
        }

        Printer chosen = showPrinterChooser(printers);
        if (chosen == null) return;

        PrinterJob job = PrinterJob.createPrinterJob(chosen);
        if (job == null) return;

        Node contentNode = statusPieChart.getParent();
        PageLayout layout = job.getJobSettings().getPageLayout();
        WritableImage img = contentNode.snapshot(new SnapshotParameters(), null);
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
        Dialog<Printer> dialog = new Dialog<>();
        dialog.setTitle("Print / 列印");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Printer> combo = new ComboBox<>();
        combo.getItems().addAll(printers);
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Printer p)    { return p == null ? "" : p.getName(); }
            @Override public Printer fromString(String s)  { return null; }
        });
        Printer def = Printer.getDefaultPrinter();
        combo.setValue(def != null && printers.contains(def) ? def : printers.get(0));
        combo.setPrefWidth(300);

        VBox content = new VBox(8, new Label("Select printer / 選擇印表機："), combo);
        content.setPadding(new Insets(12, 16, 4, 16));
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? combo.getValue() : null);
        return dialog.showAndWait().orElse(null);
    }

    private void loadStats() {
        lastStats = jigService.getStatusStats();
        lastTotal = jigService.getTotalJigCount();

        totalLabel.setText("Total JTs / 治模具總數：" + lastTotal);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        statsCardBox.getChildren().clear();

        for (JigStatus status : JigStatus.values()) {
            long count = lastStats.getOrDefault(status, 0L);
            if (count > 0) {
                pieData.add(new PieChart.Data(status.getLabel() + " / " + status.getLabelZh(), count));
            }
            statsCardBox.getChildren().add(buildStatRow(status, count, lastTotal));
        }
        statusPieChart.setData(pieData);
    }

    private HBox buildStatRow(JigStatus status, long count, long total) {
        HBox row = new HBox(8);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().add("jig-stats-row");

        Label dot = new Label("●");
        dot.getStyleClass().addAll("jig-stats-dot", "jig-status-" + status.getCssClass());

        Label nameLabel = new Label(status.getLabel() + " / " + status.getLabelZh());
        nameLabel.getStyleClass().add("jig-stats-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        String pct = total > 0 ? String.format("%.0f%%", count * 100.0 / total) : "—";
        Label countLabel = new Label(count + "  (" + pct + ")");
        countLabel.getStyleClass().add("jig-stats-count");

        row.getChildren().addAll(dot, nameLabel, new Region(), countLabel);
        HBox.setHgrow(row.getChildren().get(2), Priority.ALWAYS);
        return row;
    }

    private Stage getStage() {
        return (Stage) statusPieChart.getScene().getWindow();
    }
}
