package com.jj.jig.ui.stats;

import com.jj.jig.export.JigPdfReportService;
import com.jj.jig.jig.JigService;
import com.jj.jig.jig.JigStatus;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class StatsViewController {

    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    @FXML private PieChart statusPieChart;
    @FXML private Label totalLabel;
    @FXML private VBox statsCardBox;

    private Map<JigStatus, Long> lastStats;
    private long lastTotal;

    private final JigService jigService;
    private final JigPdfReportService pdfReportService;

    public StatsViewController(JigService jigService, JigPdfReportService pdfReportService) {
        this.jigService = jigService;
        this.pdfReportService = pdfReportService;
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
    public void handleExportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Status Report PDF / 匯出狀態報表");
        chooser.setInitialFileName("jt-status-report-" + LocalDateTime.now().format(FILE_FMT) + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = chooser.showSaveDialog(getStage());
        if (file == null) return;

        try {
            pdfReportService.exportStatusReport(
                    lastStats, lastTotal,
                    jigService.findJigs(""),
                    file.toPath());
            Alert ok = new Alert(AlertType.INFORMATION, "PDF exported. / PDF 已匯出。", ButtonType.OK);
            ok.setHeaderText(null);
            ok.showAndWait();
        } catch (IOException e) {
            Alert err = new Alert(AlertType.ERROR, "Export failed: " + e.getMessage(), ButtonType.OK);
            err.setHeaderText(null);
            err.showAndWait();
        }
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
