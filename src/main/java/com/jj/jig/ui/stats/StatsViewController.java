package com.jj.jig.ui.stats;

import com.jj.jig.jig.JigService;
import com.jj.jig.jig.JigStatus;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class StatsViewController {

    @FXML private PieChart statusPieChart;
    @FXML private Label totalLabel;
    @FXML private VBox statsCardBox;

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

    private void loadStats() {
        Map<JigStatus, Long> stats = jigService.getStatusStats();
        long total = jigService.getTotalJigCount();

        totalLabel.setText("Total JTs / 治模具總數：" + total);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        statsCardBox.getChildren().clear();

        for (JigStatus status : JigStatus.values()) {
            long count = stats.getOrDefault(status, 0L);
            if (count > 0) {
                pieData.add(new PieChart.Data(status.getLabel() + " / " + status.getLabelZh(), count));
            }
            statsCardBox.getChildren().add(buildStatRow(status, count, total));
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
}
