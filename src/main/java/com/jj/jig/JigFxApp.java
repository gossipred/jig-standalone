package com.jj.jig;

import com.jj.jig.backup.AutoBackupService;
import com.jj.jig.backup.BackupRestoreService;
import com.jj.jig.license.AuthStatus;
import com.jj.jig.license.LicenseService;
import com.jj.jig.ui.SpringFxmlLoader;
import com.jj.jig.ui.StageHolder;
import com.jj.jig.uninstall.UninstallService;
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
    public void init() throws Exception {
        // Restore from backup BEFORE Spring starts (H2 not yet connected)
        if (BackupRestoreService.hasPendingRestore()) {
            BackupRestoreService.executePendingRestore();
        }

        springContext = new SpringApplicationBuilder(JigSpringApplication.class)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) throws Exception {
        springContext.getBean(StageHolder.class).setPrimaryStage(stage);

        // Run auto-backup check in background after Spring is ready
        new Thread(() ->
                springContext.getBean(AutoBackupService.class).checkAndRunAutoBackup()).start();

        AuthStatus authStatus = springContext.getBean(LicenseService.class).checkAuthorization();
        String firstFxml = authStatus.isAllowed() ? "/fxml/login.fxml" : "/fxml/license-screen.fxml";
        Parent loginRoot = springContext.getBean(SpringFxmlLoader.class).load(firstFxml);

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
        UninstallService uninstallService = springContext.getBean(UninstallService.class);
        boolean doUninstall = uninstallService.isPendingUninstall();

        springContext.close();  // H2 releases the DB lock here

        if (doUninstall) {
            try {
                uninstallService.deleteDataDirectory();
            } catch (Exception ignored) {}
        }

        Platform.exit();
    }
}
