package com.jj.jig.ui.jig;

import com.jj.jig.auth.UserSession;
import com.jj.jig.jig.Jig;
import com.jj.jig.jig.JigService;
import com.jj.jig.jig.JigStatus;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class JigStatusDialogController {

    @FXML private Label currentStatusLabel;
    @FXML private ComboBox<JigStatus> newStatusCombo;
    @FXML private TextArea noteField;
    @FXML private HBox errorBar;
    @FXML private Label errorLabel;

    private Long jigId;

    private final JigService jigService;
    private final UserSession userSession;

    public JigStatusDialogController(JigService jigService, UserSession userSession) {
        this.jigService = jigService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        newStatusCombo.getItems().addAll(JigStatus.values());
        newStatusCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(JigStatus s) {
                return s == null ? "" : s.getLabel() + " / " + s.getLabelZh();
            }
            @Override
            public JigStatus fromString(String string) {
                return null;
            }
        });
    }

    public void initDialog(Long jigId) {
        this.jigId = jigId;
        Jig jig = jigService.findById(jigId);
        JigStatus current = jig.getStatus() != null ? jig.getStatus() : JigStatus.Normal;
        currentStatusLabel.setText(current.getLabel() + " / " + current.getLabelZh());
        newStatusCombo.getSelectionModel().select(current);
    }

    @FXML
    public void handleConfirm() {
        hideError();
        JigStatus newStatus = newStatusCombo.getValue();
        if (newStatus == null) {
            showError("Please select a new status. / 請選擇新狀態。");
            return;
        }

        String note = noteField.getText().trim();
        try {
            jigService.updateStatus(jigId, newStatus, note.isEmpty() ? null : note,
                    userSession.getCurrentUser().getUsername());
            getStage().close();
        } catch (Exception e) {
            showError(e.getMessage());
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
        return (Stage) newStatusCombo.getScene().getWindow();
    }
}
