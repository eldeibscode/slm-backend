package com.slm.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    /**
     * Base directory for file storage (e.g., "./" for project root)
     */
    private String baseDir = "./";

    /**
     * Relative path within base directory for uploads (e.g., "uploads/reports/")
     */
    private String path = "/uploads/reports/";

    /**
     * URL prefix for serving uploaded files (e.g., "http://localhost:3000")
     */
    private String urlPrefix = "http://localhost:3000";

    /**
     * Returns the full filesystem path for uploads.
     * Combines baseDir and path.
     * .g., "./uploads/reports/"
     */
    public Path getUploadPath() {
        return Paths.get(baseDir, path).normalize();
    }

    /**
     * Returns the full filesystem path for a specific report's uploads.
     * .e., "./uploads/reports/{reportId}/"
     */
    public Path getReportUploadPath(Long reportId) {
        return getUploadPath().resolve(String.valueOf(reportId));
    }

    /**
     * Returns the full URL for an uploaded file.
     * Format: {urlPrefix}{contextPath}/{path}{reportId}/{filename}
     * e.g., "http://localhost:3000/api/uploads/reports/1/image.jpg"
     */
    public String getFileUrl(Long reportId, String filename) {
        return String.format("%s%s/%s%d/%s", urlPrefix, contextPath, path, reportId, filename);
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