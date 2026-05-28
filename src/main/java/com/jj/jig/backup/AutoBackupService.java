package com.jj.jig.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Properties;
import org.springframework.stereotype.Service;

@Service
public class AutoBackupService {

    private static final Path SETTINGS_FILE = Path.of(
            System.getProperty("user.home"), ".jig-standalone", "auto-backup.properties");

    private final BackupRestoreService backupRestoreService;

    public AutoBackupService(BackupRestoreService backupRestoreService) {
        this.backupRestoreService = backupRestoreService;
    }

    public AutoBackupSettings loadSettings() {
        Properties props = new Properties();
        if (Files.exists(SETTINGS_FILE)) {
            try (InputStream in = Files.newInputStream(SETTINGS_FILE)) {
                props.load(in);
            } catch (IOException ignored) {}
        }
        AutoBackupSettings s = new AutoBackupSettings();
        s.setEnabled(Boolean.parseBoolean(props.getProperty("enabled", "false")));
        s.setFrequency(props.getProperty("frequency", "daily"));
        s.setLocation(props.getProperty("location", ""));
        s.setLastRun(props.getProperty("last-run", ""));
        return s;
    }

    public void saveSettings(AutoBackupSettings settings) throws IOException {
        Files.createDirectories(SETTINGS_FILE.getParent());
        Properties props = new Properties();
        props.setProperty("enabled", String.valueOf(settings.isEnabled()));
        props.setProperty("frequency", settings.getFrequency());
        props.setProperty("location", settings.getLocation());
        props.setProperty("last-run", settings.getLastRun());
        try (OutputStream out = Files.newOutputStream(SETTINGS_FILE)) {
            props.store(out, "Jig Standalone Auto-Backup Settings");
        }
    }

    // Called on app startup after Spring is ready
    public void checkAndRunAutoBackup() {
        AutoBackupSettings settings = loadSettings();
        if (!settings.isEnabled() || settings.getLocation().isBlank()) return;

        Path backupDir = Path.of(settings.getLocation());
        if (!Files.exists(backupDir)) return;

        LocalDate today = LocalDate.now();
        LocalDate lastRun = settings.getLastRun().isBlank()
                ? LocalDate.MIN : LocalDate.parse(settings.getLastRun());

        boolean shouldRun = switch (settings.getFrequency()) {
            case "weekly"  -> lastRun.isBefore(today.minusDays(7));
            case "monthly" -> lastRun.isBefore(today.minusDays(30));
            default        -> !lastRun.isEqual(today); // daily
        };

        if (shouldRun) {
            try {
                backupRestoreService.createBackup(backupDir);
                settings.setLastRun(today.toString());
                saveSettings(settings);
            } catch (IOException ignored) {}
        }
    }
}
