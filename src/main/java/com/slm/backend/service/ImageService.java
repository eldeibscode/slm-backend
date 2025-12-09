package com.slm.backend.service;

import com.slm.backend.dto.report.ReportDto;
import com.slm.backend.entity.Report;
import com.slm.backend.entity.ReportImage;
import com.slm.backend.repository.ReportImageRepository;
import com.slm.backend.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ReportRepository reportRepository;
    private final ReportImageRepository reportImageRepository;

    @Value("${app.upload.base-dir:/reports}")
    private String baseDir;

    @Value("${app.upload.path:/uploads/reports}")
    private String uploadPath;

    @Value("${app.upload.url-prefix:http://localhost:3000/reports}")
    private String urlPrefix;

    @Transactional
    public Map<String, Object> uploadImage(Long reportId, MultipartFile file, String alt, String caption) throws IOException {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        // Create report-specific folder (using report ID as folder name)
        String reportFolder = String.valueOf(reportId);
        Path path = Paths.get(baseDir, this.uploadPath, reportFolder);
        Files.createDirectories(path);

        // Save file in report folder
        Path filePath = path.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create image record with folder in URL
        String imageUrl = urlPrefix + "/" + uploadPath + "/" + reportFolder + "/" + filename;

        ReportImage image = ReportImage.builder()
            .report(report)
            .url(imageUrl)
            .alt(alt != null ? alt : originalFilename)
            .caption(caption)
            .displayOrder(report.getImages().size())
            .build();

        image = reportImageRepository.save(image);

        return Map.of(
            "image", mapToDto(image),
            "message", "Image uploaded successfully"
        );
    }

    @Transactional
    public void deleteImage(Long reportId, Long imageId) {
        ReportImage image = reportImageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        if (!image.getReport().getId().equals(reportId)) {
            throw new IllegalArgumentException("Image does not belong to this report");
        }

        // Delete file from disk (handles both flat and nested paths)
        try {
            String relativePath = image.getUrl().replace(urlPrefix + "/", "");
            Path filePath = Paths.get(baseDir).resolve(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but continue with database deletion
        }

        reportImageRepository.delete(image);
    }

    public void softDeleteReportFolder(Long reportId) {
        String reportFolder = String.valueOf(reportId);
        String deletedFolder = "del-" + reportId;
        Path sourcePath = Paths.get(baseDir, reportFolder);
        Path targetPath = Paths.get(baseDir, deletedFolder);

        try {
            if (Files.exists(sourcePath)) {
                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            // Log error but don't fail the report deletion
        }
    }

    @Transactional
    public ReportDto.ReportImageDto updateImageOrder(Long reportId, Long imageId, Integer order) {
        ReportImage image = reportImageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        if (!image.getReport().getId().equals(reportId)) {
            throw new IllegalArgumentException("Image does not belong to this report");
        }

        image.setDisplayOrder(order);
        image = reportImageRepository.save(image);

        return mapToDto(image);
    }

    private ReportDto.ReportImageDto mapToDto(ReportImage image) {
        return ReportDto.ReportImageDto.builder()
            .id(image.getId())
            .reportId(image.getReport().getId())
            .url(image.getUrl())
            .thumbnailUrl(image.getThumbnailUrl())
            .alt(image.getAlt())
            .caption(image.getCaption())
            .order(image.getDisplayOrder())
            .uploadedAt(image.getCreatedAt())
            .build();
    }
}
