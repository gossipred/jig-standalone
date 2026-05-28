package com.jj.jig;

import com.jj.jig.ui.SpringFxmlLoader;
import com.jj.jig.ui.StageHolder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JigFxApp extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(JigSpringApplication.class)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) throws Exception {
        springContext.getBean(StageHolder.class).setPrimaryStage(stage);

        Parent loginRoot = springContext.getBean(SpringFxmlLoader.class).load("/fxml/login.fxml");

        Scene scene = new Scene(loginRoot, 960, 600);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        stage.setTitle("Jig and Toolings Management System");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }
}
