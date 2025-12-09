package com.slm.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    /**
     * Base directory for file storage (e.g., "./" for project root)
     */
    private String baseDir = "./";

    /**
     * Relative path within base directory for uploads (e.g., "uploads/reports/")
     */
    private String path = "uploads/reports/";

    /**
     * URL prefix for serving uploaded files (e.g., "http://localhost:3000")
     */
    private String urlPrefix = "http://localhost:3000";

    /**
     * Returns the full filesystem path for uploads.
     * Combines baseDir and path.
     */
    public Path getUploadPath() {
        return Paths.get(baseDir, path).normalize();
    }

    /**
     * Returns the full filesystem path for a specific report's uploads.
     */
    public Path getReportUploadPath(Long reportId) {
        return getUploadPath().resolve(String.valueOf(reportId));
    }

    /**
     * Returns the full URL for an uploaded file.
     * Format: {urlPrefix}/api/uploads/reports/{reportId}/{filename}
     */
    public String getFileUrl(Long reportId, String filename) {
        return String.format("%s/api/uploads/reports/%d/%s", urlPrefix, reportId, filename);
    }

    /**
     * Initialize upload directory on startup.
     */
    @PostConstruct
    public void init() throws IOException {
        Path uploadPath = getUploadPath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }
}