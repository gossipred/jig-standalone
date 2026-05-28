package com.jj.jig.ui;

import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class StageHolder {

    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
