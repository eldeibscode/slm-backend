package com.slm.backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private AuthorDto author;
    private CategoryDto category;

    @Builder.Default
    private List<TagDto> tags = new ArrayList<>();

    @Builder.Default
    private List<ReportImageDto> images = new ArrayList<>();

    private Long viewCount;
    private String featuredImage;
    private Long featuredImageId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDto {
        private Long id;
        private String name;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagDto {
        private Long id;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportImageDto {
        private Long id;
        private Long reportId;
        private String url;
        private String thumbnailUrl;
        private String alt;
        private String caption;
        private Integer order;
        private LocalDateTime uploadedAt;
    }
}
