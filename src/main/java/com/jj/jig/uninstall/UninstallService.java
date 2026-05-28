package com.jj.jig.uninstall;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.springframework.stereotype.Service;

@Service
public class UninstallService {

    private volatile boolean pendingUninstall = false;

    public void markForUninstall() {
        this.pendingUninstall = true;
    }

    public boolean isPendingUninstall() {
        return pendingUninstall;
    }

    // Called AFTER Spring context closes (H2 already released the DB lock)
    public void deleteDataDirectory() throws IOException {
        Path dataDir = Path.of(System.getProperty("user.home"), ".jig-standalone");
        if (Files.exists(dataDir)) {
            Files.walk(dataDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }
}
