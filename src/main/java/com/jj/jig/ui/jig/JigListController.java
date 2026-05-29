package com.jj.jig.ui.jig;

import com.jj.jig.auth.UserSession;
import com.jj.jig.jig.Jig;
import com.jj.jig.jig.JigCsvExportService;
import com.jj.jig.jig.JigService;
import com.jj.jig.jig.JigStatus;
import com.jj.jig.ui.SpringFxmlLoader;
import com.jj.jig.ui.StageHolder;
import com.jj.jig.user.UserRole;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class JigListController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button detailBtn;
    @FXML private Button changeStatusBtn;
    @FXML private Button newJigBtn;
    @FXML private Button editJigBtn;
    @FXML private Button deleteJigBtn;
    @FXML private TableView<Jig> jigsTable;
    @FXML private TableColumn<Jig, String> colJigNo;
    @FXML private TableColumn<Jig, String> colClassification;
    @FXML private TableColumn<Jig, String> colModelName;
    @FXML private TableColumn<Jig, String> colJigName;
    @FXML private TableColumn<Jig, String> colCustomer;
    @FXML private TableColumn<Jig, String> colLine;
    @FXML private TableColumn<Jig, Number> colQty;
    @FXML private TableColumn<Jig, JigStatus> colStatus;
    @FXML private TableColumn<Jig, LocalDate> colDueDate;
    @FXML private TableColumn<Jig, String> colDri;
    @FXML private TableColumn<Jig, String> colNote;
    @FXML private Label statusBarLabel;

    private final JigService        jigService;
    private final JigCsvExportService exportService;
    private final UserSession       userSession;
    private final SpringFxmlLoader  fxmlLoader;
    private final StageHolder       stageHolder;

    private final Map<String, JigStatus> statusFilterMap = new LinkedHashMap<>();
    private List<Jig> allJigs = List.of();
    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(300));

    public JigListController(JigService jigService, JigCsvExportService exportService,
                             UserSession userSession, SpringFxmlLoader fxmlLoader,
                             StageHolder stageHolder) {
        this.jigService    = jigService;
        this.exportService = exportService;
        this.userSession   = userSession;
        this.fxmlLoader    = fxmlLoader;
        this.stageHolder   = stageHolder;
    }

    @FXML
    public void initialize() {
        setupStatusFilter();
        setupColumns();
        setupSearchListener();
        setupSelectionListener();
        setupDoubleClick();
        setupRoleAccess();
        loadJigs();
    }

    private void setupDoubleClick() {
        jigsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && jigsTable.getSelectionModel().getSelectedItem() != null) {
                handleViewDetail();
            }
        });
    }

    private void setupStatusFilter() {
        statusFilterMap.put("All Statuses / 全部狀態", null);
        for (JigStatus s : JigStatus.values()) {
            statusFilterMap.put(s.getLabel() + " / " + s.getLabelZh(), s);
        }
        statusFilter.getItems().addAll(statusFilterMap.keySet());
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.setOnAction(e -> applyFilters());
    }

    private void setupColumns() {
        colJigNo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getJigNo()));
        colClassification.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClassification()));
        colModelName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getModelName()));
        colJigName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getJigName()));
        colCustomer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomer()));
        colLine.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAssemblyLine()));
        colQty.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getQuantity()));
        colDri.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDri()));
        colNote.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNote()));
        setupStatusColumn();
        setupDueDateColumn();
    }

    private void setupStatusColumn() {
        colStatus.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(JigStatus status, boolean empty) {
                super.updateItem(status, empty);
                setGraphic(null);
                setText(null);
                if (!empty && status != null) {
                    Label badge = new Label(status.getLabel() + " / " + status.getLabelZh());
                    badge.getStyleClass().addAll("jig-status-badge", "jig-status-" + status.getCssClass());
                    setGraphic(badge);
                }
            }
        });
    }

    private void setupDueDateColumn() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        colDueDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDueDate()));
        colDueDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                getStyleClass().removeAll("jig-duedate-overdue", "jig-duedate-warning", "jig-duedate-empty");
                if (empty) {
                    setText(null);
                    return;
                }
                if (date == null) {
                    setText("—");
                    getStyleClass().add("jig-duedate-empty");
                } else {
                    setText(date.format(fmt));
                    LocalDate today = LocalDate.now();
                    if (date.isBefore(today)) {
                        getStyleClass().add("jig-duedate-overdue");
                    } else if (date.isBefore(today.plusDays(30))) {
                        getStyleClass().add("jig-duedate-warning");
                    }
                }
            }
        });
    }

    private void setupSearchListener() {
        searchDelay.setOnFinished(e -> {
            String keyword = searchField.getText().trim();
            allJigs = jigService.findJigs(keyword.isEmpty() ? null : keyword);
            applyFilters();
        });
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchDelay.stop();
            searchDelay.playFromStart();
        });
    }

    private void setupSelectionListener() {
        jigsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (userSession.getCurrentUser() == null) return;
            boolean hasSelection = selected != null;
            UserRole role = userSession.getCurrentUser().getRole();
            boolean canAct = hasSelection && (role == UserRole.ADMIN || role == UserRole.SUPERVISOR || role == UserRole.ENGINEER);
            detailBtn.setDisable(!hasSelection);
            changeStatusBtn.setDisable(!canAct);
            editJigBtn.setDisable(!canAct);
            deleteJigBtn.setDisable(!hasSelection || role != UserRole.ADMIN);
        });
    }

    private void setupRoleAccess() {
        UserRole role = userSession.getCurrentUser().getRole();
        boolean canAct = role == UserRole.ADMIN || role == UserRole.SUPERVISOR || role == UserRole.ENGINEER;
        changeStatusBtn.setVisible(canAct);
        changeStatusBtn.setManaged(canAct);
        newJigBtn.setVisible(canAct);
        newJigBtn.setManaged(canAct);
        editJigBtn.setVisible(canAct);
        editJigBtn.setManaged(canAct);
        deleteJigBtn.setVisible(role == UserRole.ADMIN);
        deleteJigBtn.setManaged(role == UserRole.ADMIN);
    }

    private void loadJigs() {
        String keyword = searchField.getText().trim();
        allJigs = jigService.findJigs(keyword.isEmpty() ? null : keyword);
        applyFilters();
    }

    private void applyFilters() {
        String selectedKey = statusFilter.getSelectionModel().getSelectedItem();
        JigStatus selectedStatus = selectedKey != null ? statusFilterMap.get(selectedKey) : null;

        List<Jig> filtered = allJigs.stream()
                .filter(jig -> selectedStatus == null || jig.getStatus() == selectedStatus)
                .collect(Collectors.toList());

        jigsTable.setItems(FXCollections.observableArrayList(filtered));
        statusBarLabel.setText(filtered.size() + " JT(s) shown / 顯示 " + filtered.size() + " 筆");
    }

    @FXML
    public void handleRefresh() {
        loadJigs();
    }

    @FXML
    public void handleExportCsv() {
        List<Jig> jigs = jigService.findJigs(null);
        if (jigs.isEmpty()) {
            Alert alert = new Alert(AlertType.INFORMATION, "No JTs to export. / 沒有治模具可匯出。", ButtonType.OK);
            alert.setHeaderText(null);
            alert.initOwner(stageHolder.getPrimaryStage());
            alert.showAndWait();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export CSV / 匯出 CSV");
        chooser.setInitialFileName("jigs-export-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) + ".csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(stageHolder.getPrimaryStage());
        if (file == null) return;

        try {
            Files.write(file.toPath(), exportService.export(jigs));
            Alert ok = new Alert(AlertType.INFORMATION,
                    "Exported " + jigs.size() + " jigs to:\n" + file.getAbsolutePath(), ButtonType.OK);
            ok.setTitle("Export Complete / 匯出完成");
            ok.setHeaderText(null);
            ok.initOwner(stageHolder.getPrimaryStage());
            ok.showAndWait();
        } catch (IOException e) {
            Alert err = new Alert(AlertType.ERROR, "Export failed: " + e.getMessage(), ButtonType.OK);
            err.setHeaderText(null);
            err.initOwner(stageHolder.getPrimaryStage());
            err.showAndWait();
        }
    }

    @FXML
    public void handleImportCsv() {
        try {
            FXMLLoader loader = fxmlLoader.createLoader("/fxml/jig-import-dialog.fxml");
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Import CSV / 匯入 CSV");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(stageHolder.getPrimaryStage());
            stage.setResizable(false);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            loadJigs();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR, "Failed to open import dialog.", ButtonType.OK);
            alert.setHeaderText(null);
            alert.initOwner(stageHolder.getPrimaryStage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleViewDetail() {
        Jig selected = jigsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = fxmlLoader.createLoader("/fxml/jig-detail.fxml");
            Parent root = loader.load();
            JigDetailController detailController = loader.getController();
            detailController.initDetail(selected.getId());

            Stage stage = new Stage();
            stage.setTitle("JT Details / 治模具詳情  —  " + selected.getJigNo());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(stageHolder.getPrimaryStage());
            stage.setResizable(true);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(scene);
            stage.setWidth(940);
            stage.setHeight(700);
            stage.showAndWait();
            loadJigs();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR, "Failed to open detail view.", ButtonType.OK);
            alert.setHeaderText(null);
            alert.initOwner(stageHolder.getPrimaryStage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleChangeStatus() {
        Jig selected = jigsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = fxmlLoader.createLoader("/fxml/jig-status-dialog.fxml");
            Parent root = loader.load();
            JigStatusDialogController statusController = loader.getController();
            statusController.initDialog(selected.getId());

            Stage stage = new Stage();
            stage.setTitle("Change Status / 變更狀態  —  " + selected.getJigNo());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(stageHolder.getPrimaryStage());
            stage.setResizable(false);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            loadJigs();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR, "Failed to open status dialog.", ButtonType.OK);
            alert.setHeaderText(null);
            alert.initOwner(stageHolder.getPrimaryStage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleNewJig() {
        openFormDialog(null);
    }

    @FXML
    public void handleEditJig() {
        Jig selected = jigsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openFormDialog(selected.getId());
        }
    }

    @FXML
    public void handleDeleteJig() {
        Jig selected = jigsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Delete JT " + selected.getJigNo() + "?\n刪除治模具 " + selected.getJigNo() + "？\n\nThis action cannot be undone. / 此操作無法復原。",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm Delete / 確認刪除");
        confirm.setHeaderText(null);
        confirm.initOwner(stageHolder.getPrimaryStage());
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    jigService.deleteJig(selected.getId(), userSession.getCurrentUser().getUsername());
                    loadJigs();
                } catch (Exception e) {
                    Alert err = new Alert(AlertType.ERROR,
                            "Delete failed / 刪除失敗：\n" + e.getMessage(), ButtonType.OK);
                    err.setHeaderText(null);
                    err.initOwner(stageHolder.getPrimaryStage());
                    err.showAndWait();
                }
            }
        });
    }

    private void openFormDialog(Long jigId) {
        try {
            FXMLLoader loader = fxmlLoader.createLoader("/fxml/jig-form-dialog.fxml");
            Parent root = loader.load();
            JigFormDialogController formController = loader.getController();
            if (jigId == null) {
                formController.initForCreate();
            } else {
                formController.initForEdit(jigId);
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle(jigId == null ? "New JT / 新增治模具" : "Edit JT / 編輯治模具");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stageHolder.getPrimaryStage());
            dialogStage.setResizable(false);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            loadJigs();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR, "Failed to open form. / 無法開啟表單。", ButtonType.OK);
            alert.setHeaderText(null);
            alert.initOwner(stageHolder.getPrimaryStage());
            alert.showAndWait();
        }
    }
}
