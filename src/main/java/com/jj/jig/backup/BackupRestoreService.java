package com.jj.jig.backup;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class BackupRestoreService {

    private static final String PENDING_RESTORE_MARKER =
        System.getProperty("java.io.tmpdir") + "/jig-pending-restore.txt";

    private final JdbcTemplate jdbcTemplate;

    public BackupRestoreService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Creates a backup folder containing DB zip + uploads copy
    public Path createBackup(Path targetDir) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        Path backupDir = targetDir.resolve("jig-backup-" + timestamp);
        Files.createDirectories(backupDir);

        // H2 BACKUP TO creates a zip of the database files while it's running
        Path dbZip = backupDir.resolve("jigdb-backup.zip");
        jdbcTemplate.execute("BACKUP TO '" + dbZip.toString().replace("\\", "/") + "'");

        // Copy uploads directory
        Path uploads = Path.of(System.getProperty("user.home"), ".jig-standalone", "uploads");
        if (Files.exists(uploads)) {
            copyDirectory(uploads, backupDir.resolve("uploads"));
        }

        return backupDir;
    }

    // Lists jig-backup-* subdirectories in a folder, newest first
    public List<Path> listAvailableBackups(Path folder) {
        if (!Files.exists(folder)) return List.of();
        try {
            return Files.list(folder)
                    .filter(p -> Files.isDirectory(p)
                            && p.getFileName().toString().startsWith("jig-backup-"))
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    // Marks a pending restore — written to system temp so it survives app exit
    public void markPendingRestore(Path backupDir) throws IOException {
        Files.writeString(Path.of(PENDING_RESTORE_MARKER), backupDir.toAbsolutePath().toString());
    }

    // --- Static methods called from JigFxApp.init() BEFORE Spring starts ---

    public static boolean hasPendingRestore() {
        return Files.exists(Path.of(PENDING_RESTORE_MARKER));
    }

    public static void executePendingRestore() throws IOException {
        Path marker = Path.of(PENDING_RESTORE_MARKER);
        if (!Files.exists(marker)) return;

        String backupDirStr = Files.readString(marker).trim();
        Files.delete(marker);

        Path backupDir = Path.of(backupDirStr);
        if (!Files.exists(backupDir)) return;

        Path dataDir = Path.of(System.getProperty("user.home"), ".jig-standalone", "data");

        // Clear existing data directory before restore
        if (Files.exists(dataDir)) {
            Files.walk(dataDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(java.io.File::delete);
        }
        Files.createDirectories(dataDir);

        // Extract H2 backup zip
        Path dbZip = backupDir.resolve("jigdb-backup.zip");
        if (Files.exists(dbZip)) {
            extractZip(dbZip, dataDir);
        }

        // Restore uploads
        Path backupUploads = backupDir.resolve("uploads");
        Path targetUploads = Path.of(System.getProperty("user.home"), ".jig-standalone", "uploads");
        if (Files.exists(backupUploads)) {
            copyDirectory(backupUploads, targetUploads);
        }
    }

    private static void extractZip(Path zipPath, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(src -> {
            try {
                Path dest = target.resolve(source.relativize(src));
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
