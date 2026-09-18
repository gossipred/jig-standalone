package com.jj.jig.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingsService {

    private static final Path SETTINGS_FILE = Path.of(
            System.getProperty("user.home"), ".jig-standalone", "system-settings.properties");

    public SystemSettings loadSettings() {
        Properties props = new Properties();
        if (Files.exists(SETTINGS_FILE)) {
            try (InputStream in = Files.newInputStream(SETTINGS_FILE)) {
                props.load(in);
            } catch (IOException ignored) {}
        }
        SystemSettings settings = new SystemSettings();
        settings.setOrganizationName(props.getProperty("organization-name", ""));
        return settings;
    }

    public void saveSettings(SystemSettings settings) throws IOException {
        Files.createDirectories(SETTINGS_FILE.getParent());
        Properties props = new Properties();
        props.setProperty("organization-name", settings.getOrganizationName());
        try (OutputStream out = Files.newOutputStream(SETTINGS_FILE)) {
            props.store(out, "Jig Standalone System Settings");
        }
    }
}
