package com.slm.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestimonialRequest {

    @NotBlank(message = "Quote is required")
    private String quote;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "Title is required")
    private String title;

    private String company;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating = 5;

    private String status = "draft";

    private Integer order = 0;

    private String avatarUrl;
}
