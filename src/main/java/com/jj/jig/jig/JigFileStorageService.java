package com.jj.jig.jig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class JigFileStorageService {

    private final Path uploadDir;

    public JigFileStorageService(@Value("${app.upload.jig-files-dir:uploads/jigs}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public StoredJigFile store(MultipartFile file, String jigNo) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "jig-file" : file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("File name cannot contain path traversal.");
        }

        String extension = getExtension(originalFilename);
        String safeJigNo = jigNo.replaceAll("[^A-Z0-9-]", "_");
        String storedFilename = safeJigNo + "-" + UUID.randomUUID() + extension;

        try {
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(storedFilename).normalize();
            file.transferTo(target);
            return new StoredJigFile(
                    originalFilename,
                    Path.of("uploads", "jigs", storedFilename).toString(),
                    file.getContentType(),
                    file.getSize()
            );
        } catch (IOException ex) {
            throw new IllegalArgumentException("File upload failed. Please try again.", ex);
        }
    }

    public Resource loadAsResource(String storedPath) {
        try {
            Path requestedPath = Path.of(storedPath).toAbsolutePath().normalize();
            if (!requestedPath.startsWith(uploadDir.getParent())) {
                throw new IllegalArgumentException("Invalid file path.");
            }

            Resource resource = new UrlResource(requestedPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("File is not available for download.");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("File is not available for download.", ex);
        }
    }

    public void deleteIfExists(String storedPath) {
        if (!StringUtils.hasText(storedPath)) {
            return;
        }

        Path requestedPath = Path.of(storedPath).toAbsolutePath().normalize();
        if (!requestedPath.startsWith(uploadDir.getParent())) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        try {
            Files.deleteIfExists(requestedPath);
        } catch (IOException ex) {
            throw new IllegalArgumentException("File delete failed. Please try again.", ex);
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase();
    }

    public record StoredJigFile(String originalFilename, String storedPath, String contentType, long fileSize) {
    }
}
