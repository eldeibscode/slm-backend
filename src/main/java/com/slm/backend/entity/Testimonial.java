package com.slm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "testimonials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Testimonial extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String quote;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String company;

    @Builder.Default
    @Column(nullable = false)
    private Integer rating = 5;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DRAFT;

    @Builder.Default
    @Column(nullable = false, name = "display_order")
    private Integer displayOrder = 0;

    @Column(length = 500)
    private String avatarUrl;

    public enum Status {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }
}
