package com.slm.backend.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @NotBlank(message = "Excerpt is required")
    @Size(max = 500, message = "Excerpt must be at most 500 characters")
    private String excerpt;

    @NotBlank(message = "Content is required")
    private String content;

    @Builder.Default
    private String status = "draft";

    private Long categoryId;

    @Builder.Default
    private List<Long> tagIds = new ArrayList<>();

    private Long featuredImageId;

    private String featuredImage;
}
