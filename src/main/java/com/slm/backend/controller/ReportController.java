package com.slm.backend.controller;

import com.slm.backend.dto.report.*;
import com.slm.backend.service.ImageService;
import com.slm.backend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ImageService imageService;

    /**
     * Get all reports with pagination and filters
     */
    @GetMapping
    public ResponseEntity<ReportListResponse> getReports(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder
    ) {
        // Parse date strings to LocalDateTime
        java.time.LocalDateTime dateFromParsed = null;
        java.time.LocalDateTime dateToParsed = null;
        if (dateFrom != null && !dateFrom.isEmpty()) {
            dateFromParsed = java.time.LocalDate.parse(dateFrom).atStartOfDay();
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            dateToParsed = java.time.LocalDate.parse(dateTo).atTime(23, 59, 59);
        }

        ReportListResponse response = reportService.getReports(
            page, pageSize, search, categoryId, authorId, status, tagIds, dateFromParsed, dateToParsed, sortBy, sortOrder
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user's reports
     * ADMIN sees all reports, REPORTER sees only their own
     */
    @GetMapping("/my/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPORTER')")
    public ResponseEntity<ReportListResponse> getMyReports(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        String role = auth.getAuthorities().stream()
            .findFirst()
            .map(a -> a.getAuthority().replace("ROLE_", ""))
            .orElse("USER");

        ReportListResponse response = reportService.getMyReports(
            email, role, page, pageSize, search, status, sortBy, sortOrder
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get report by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportDto> getReportById(@PathVariable Long id) {
        try {
            ReportDto report = reportService.getReportById(id);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get report by slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ReportDto> getReportBySlug(@PathVariable String slug) {
        try {
            ReportDto report = reportService.getReportBySlug(slug);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get latest published reports
     */
    @GetMapping("/latest")
    public ResponseEntity<List<ReportDto>> getLatestReports(
            @RequestParam(required = false, defaultValue = "5") int limit
    ) {
        List<ReportDto> reports = reportService.getLatestPublished(limit);
        return ResponseEntity.ok(reports);
    }

    /**
     * Create a new report
     * Only ADMIN and REPORTER roles can create reports
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REPORTER')")
    public ResponseEntity<?> createReport(@Valid @RequestBody CreateReportRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authorEmail = authentication.getName();

            ReportDto report = reportService.createReport(request, authorEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Update an existing report
     * Only ADMIN and REPORTER roles can update reports
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPORTER')")
    public ResponseEntity<?> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReportRequest request
    ) {
        try {
            ReportDto report = reportService.updateReport(id, request);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Delete a report
     * Only ADMIN role can delete reports
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        try {
            reportService.deleteReport(id);
            return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Publish a draft report
     * Only ADMIN and REPORTER roles can publish reports
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPORTER')")
    public ResponseEntity<?> publishReport(@PathVariable Long id) {
        try {
            ReportDto report = reportService.publishReport(id);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Archive a report
     * Only ADMIN role can archive reports
     */
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> archiveReport(@PathVariable Long id) {
        try {
            ReportDto report = reportService.archiveReport(id);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Increment view count for a report
     * Public endpoint
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<?> incrementViewCount(@PathVariable Long id) {
        try {
            reportService.incrementViewCount(id);
            return ResponseEntity.ok(Map.of("message", "View count incremented"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================================
    // IMAGE ENDPOINTS
    // ============================================================================

    /**
     * Upload an image for a report
     */
    @PostMapping("/{reportId}/images")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPORTER')")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long reportId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "alt", required = false) String alt,
            @RequestParam(value = "caption", required = false) String caption
    ) {
        try {
            Map<String, Object> result = imageService.uploadImage(reportId, file, alt, caption);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to upload image: " + e.getMessage()));
        }
    }

    /**
     * Delete an image from a report
     */
    @DeleteMapping("/{reportId}/images/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPORTER')")
    public ResponseEntity<?> deleteImage(
            @PathVariable Long reportId,
            @PathVariable Long imageId
    ) {
        try {
            imageService.deleteImage(reportId, imageId);
            return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Update image order
     */
    @PatchMapping("/{reportId}/images/{imageId}/order")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPORTER')")
    public ResponseEntity<?> updateImageOrder(
            @PathVariable Long reportId,
            @PathVariable Long imageId,
            @RequestBody Map<String, Integer> body
    ) {
        try {
            Integer order = body.get("order");
            if (order == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Order is required"));
            }
            ReportDto.ReportImageDto image = imageService.updateImageOrder(reportId, imageId, order);
            return ResponseEntity.ok(image);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }
}
