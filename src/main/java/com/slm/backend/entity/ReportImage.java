package com.slm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "report_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(nullable = false, length = 255)
    private String alt;

    @Column(length = 500)
    private String caption;

    @Builder.Default
    @Column(nullable = false)
    private Integer displayOrder = 0;
}
