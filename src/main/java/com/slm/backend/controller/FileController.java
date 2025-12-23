package com.slm.backend.controller;

import com.slm.backend.config.UploadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class FileController {

    private final UploadProperties uploadProperties;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = uploadProperties.getUploadPath().resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = determineContentType(filename);

                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/reports/{reportId}/{filename:.+}")
    public ResponseEntity<Resource> serveReportFile(
            @PathVariable Long reportId,
            @PathVariable String filename) {
        System.out.println("=== FileController.serveReportFile called ===");
        System.out.println("reportId: " + reportId);
        System.out.println("filename: " + filename);

        try {
            Path basePath = uploadProperties.getUploadPath().normalize();
            Path filePath = uploadProperties.getReportUploadPath(reportId).resolve(filename).normalize();

            System.out.println("basePath: " + basePath);
            System.out.println("filePath: " + filePath);
            System.out.println("file exists: " + java.nio.file.Files.exists(filePath));
            System.out.println("file readable: " + java.nio.file.Files.isReadable(filePath));

            // Path traversal protection
            if (!filePath.startsWith(basePath)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = determineContentType(filename);

                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            default -> "application/octet-stream";
        };
    }
}
