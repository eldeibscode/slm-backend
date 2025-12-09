package com.slm.backend.service;

import com.slm.backend.dto.report.*;
import com.slm.backend.entity.*;
import com.slm.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ReportImageRepository reportImageRepository;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public ReportListResponse getReports(
            Integer page,
            Integer pageSize,
            String search,
            Long categoryId,
            Long authorId,
            String status,
            List<Long> tagIds,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String sortBy,
            String sortOrder
    ) {
        int pageNum = page != null ? page : 0;
        int size = pageSize != null && pageSize > 0 ? pageSize : 10;

        String sortField = sortBy != null ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(pageNum, size, Sort.by(direction, sortField));

        Report.Status reportStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                reportStatus = Report.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        // Convert empty tagIds list to null for query
        List<Long> effectiveTagIds = (tagIds != null && !tagIds.isEmpty()) ? tagIds : null;

        Page<Report> reportPage = reportRepository.findWithFilters(
            reportStatus,
            categoryId,
            authorId,
            search,
            effectiveTagIds,
            dateFrom,
            dateTo,
            pageable
        );

        List<ReportDto> reports = reportPage.getContent().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());

        return ReportListResponse.builder()
            .reports(reports)
            .total(reportPage.getTotalElements())
            .page(pageNum)
            .pageSize(size)
            .totalPages(reportPage.getTotalPages())
            .build();
    }

    @Transactional(readOnly = true)
    public ReportDto getReportById(Long id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));
        return mapToDto(report);
    }

    @Transactional(readOnly = true)
    public ReportDto getReportBySlug(String slug) {
        Report report = reportRepository.findBySlug(slug)
            .orElseThrow(() -> new IllegalArgumentException("Report not found with slug: " + slug));
        return mapToDto(report);
    }

    @Transactional
    public ReportDto createReport(CreateReportRequest request, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
            .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        String slug = generateSlug(request.getTitle());

        Report report = Report.builder()
            .title(request.getTitle())
            .slug(slug)
            .excerpt(request.getExcerpt())
            .content(request.getContent())
            .status(parseStatus(request.getStatus()))
            .author(author)
            .featuredImage(request.getFeaturedImage())
            .featuredImageId(request.getFeaturedImageId())
            .viewCount(0L)
            .build();

        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            report.setCategory(category);
        }

        // Set tags if provided
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Long> tagIdSet = new HashSet<>(request.getTagIds());
            List<Tag> tags = tagRepository.findByIdIn(tagIdSet);
            tags.forEach(report::addTag);
        }

        // Set published date if status is PUBLISHED
        if (report.getStatus() == Report.Status.PUBLISHED) {
            report.setPublishedAt(LocalDateTime.now());
        }

        report = reportRepository.save(report);
        return mapToDto(report);
    }

    @Transactional
    public ReportDto updateReport(Long id, UpdateReportRequest request) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));

        if (request.getTitle() != null) {
            report.setTitle(request.getTitle());
            // Update slug only if title changes
            report.setSlug(generateSlug(request.getTitle()));
        }

        if (request.getExcerpt() != null) {
            report.setExcerpt(request.getExcerpt());
        }

        if (request.getContent() != null) {
            report.setContent(request.getContent());
        }

        if (request.getStatus() != null) {
            Report.Status newStatus = parseStatus(request.getStatus());
            // Set published date when transitioning to PUBLISHED
            if (newStatus == Report.Status.PUBLISHED && report.getStatus() != Report.Status.PUBLISHED) {
                report.setPublishedAt(LocalDateTime.now());
            }
            report.setStatus(newStatus);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            report.setCategory(category);
        }

        if (request.getTagIds() != null) {
            // Clear existing tags
            report.getTags().clear();
            // Add new tags
            if (!request.getTagIds().isEmpty()) {
                Set<Long> tagIdSet = new HashSet<>(request.getTagIds());
                List<Tag> tags = tagRepository.findByIdIn(tagIdSet);
                tags.forEach(report::addTag);
            }
        }

        if (request.getFeaturedImage() != null) {
            report.setFeaturedImage(request.getFeaturedImage());
        }

        if (request.getFeaturedImageId() != null) {
            report.setFeaturedImageId(request.getFeaturedImageId());
        }

        report = reportRepository.save(report);
        return mapToDto(report);
    }

    @Transactional
    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new IllegalArgumentException("Report not found with id: " + id);
        }
        // Soft-delete the report folder (rename to deleted-{id})
        imageService.softDeleteReportFolder(id);
        reportRepository.deleteById(id);
    }

    @Transactional
    public ReportDto publishReport(Long id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));

        report.setStatus(Report.Status.PUBLISHED);
        report.setPublishedAt(LocalDateTime.now());

        report = reportRepository.save(report);
        return mapToDto(report);
    }

    @Transactional
    public ReportDto archiveReport(Long id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));

        report.setStatus(Report.Status.ARCHIVED);

        report = reportRepository.save(report);
        return mapToDto(report);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));
        report.setViewCount(report.getViewCount() + 1);
        reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getLatestPublished(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Report> reports = reportRepository.findLatestPublished(Report.Status.PUBLISHED, pageable);
        return reports.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    /**
     * Get reports for the current user
     * ADMIN sees all reports, other roles see only their own
     */
    @Transactional(readOnly = true)
    public ReportListResponse getMyReports(
            String email,
            String role,
            Integer page,
            Integer pageSize,
            String search,
            String status,
            String sortBy,
            String sortOrder
    ) {
        Long authorId = null;
        if (!"ADMIN".equals(role)) {
            // Non-admin users only see their own reports
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            authorId = user.getId();
        }
        return getReports(page, pageSize, search, null, authorId, status, null, null, null, sortBy, sortOrder);
    }

    private Report.Status parseStatus(String status) {
        if (status == null || status.isEmpty()) {
            return Report.Status.DRAFT;
        }
        try {
            return Report.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Report.Status.DRAFT;
        }
    }

    private String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return UUID.randomUUID().toString();
        }

        // Normalize and remove diacritics
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");

        // Convert to lowercase and replace non-alphanumeric with hyphens
        slug = slug.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("[\\s]+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");

        // Ensure uniqueness
        String baseSlug = slug;
        int counter = 1;
        while (reportRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }

    private ReportDto mapToDto(Report report) {
        // Resolve featuredImage URL from featuredImageId if not directly set
        String featuredImageUrl = report.getFeaturedImage();
        if (featuredImageUrl == null && report.getFeaturedImageId() != null) {
            // Look up the image URL from the images list
            featuredImageUrl = report.getImages().stream()
                .filter(img -> img.getId().equals(report.getFeaturedImageId()))
                .map(ReportImage::getUrl)
                .findFirst()
                .orElse(null);
        }
        // If still no featured image but there are images, use the first one
        if (featuredImageUrl == null && !report.getImages().isEmpty()) {
            featuredImageUrl = report.getImages().stream()
                .min(Comparator.comparingInt(ReportImage::getDisplayOrder))
                .map(ReportImage::getUrl)
                .orElse(null);
        }

        return ReportDto.builder()
            .id(report.getId())
            .title(report.getTitle())
            .slug(report.getSlug())
            .excerpt(report.getExcerpt())
            .content(report.getContent())
            .publishedAt(report.getPublishedAt())
            .createdAt(report.getCreatedAt())
            .updatedAt(report.getUpdatedAt())
            .status(report.getStatus().name().toLowerCase())
            .author(mapAuthorToDto(report.getAuthor()))
            .category(report.getCategory() != null ? mapCategoryToDto(report.getCategory()) : null)
            .tags(report.getTags().stream().map(this::mapTagToDto).collect(Collectors.toList()))
            .images(report.getImages().stream().map(this::mapImageToDto).collect(Collectors.toList()))
            .viewCount(report.getViewCount())
            .featuredImage(featuredImageUrl)
            .featuredImageId(report.getFeaturedImageId())
            .build();
    }

    private ReportDto.AuthorDto mapAuthorToDto(User author) {
        return ReportDto.AuthorDto.builder()
            .id(author.getId())
            .name(author.getName())
            .email(author.getEmail())
            .build();
    }

    private ReportDto.CategoryDto mapCategoryToDto(Category category) {
        return ReportDto.CategoryDto.builder()
            .id(category.getId())
            .name(category.getName())
            .slug(category.getSlug())
            .description(category.getDescription())
            .color(category.getColor())
            .build();
    }

    private ReportDto.TagDto mapTagToDto(Tag tag) {
        return ReportDto.TagDto.builder()
            .id(tag.getId())
            .name(tag.getName())
            .slug(tag.getSlug())
            .build();
    }

    private ReportDto.ReportImageDto mapImageToDto(ReportImage image) {
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
