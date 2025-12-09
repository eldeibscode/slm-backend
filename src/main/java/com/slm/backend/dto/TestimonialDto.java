package com.slm.backend.dto;

import com.slm.backend.entity.Testimonial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialDto {
    private Long id;
    private String quote;
    private String author;
    private String title;
    private String company;
    private Integer rating;
    private String status;
    private Integer order;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TestimonialDto fromEntity(Testimonial testimonial) {
        return TestimonialDto.builder()
                .id(testimonial.getId())
                .quote(testimonial.getQuote())
                .author(testimonial.getAuthor())
                .title(testimonial.getTitle())
                .company(testimonial.getCompany())
                .rating(testimonial.getRating())
                .status(testimonial.getStatus().name().toLowerCase())
                .order(testimonial.getDisplayOrder())
                .avatarUrl(testimonial.getAvatarUrl())
                .createdAt(testimonial.getCreatedAt())
                .updatedAt(testimonial.getUpdatedAt())
                .build();
    }
}
