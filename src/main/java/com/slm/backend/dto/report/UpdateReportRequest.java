package com.slm.backend.dto.report;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportRequest {

    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Size(max = 500, message = "Excerpt must be at most 500 characters")
    private String excerpt;

    private String content;

    private String status;

    private Long categoryId;

    private List<Long> tagIds;

    private Long featuredImageId;

    private String featuredImage;
}
