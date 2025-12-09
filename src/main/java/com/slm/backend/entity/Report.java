package com.slm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(nullable = false, length = 500)
    private String excerpt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private LocalDateTime publishedAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "report_tags",
        joinColumns = @JoinColumn(name = "report_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReportImage> images = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(length = 500)
    private String featuredImage;

    @Column(name = "featured_image_id")
    private Long featuredImageId;

    public enum Status {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getReports().add(this);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getReports().remove(this);
    }

    public void addImage(ReportImage image) {
        this.images.add(image);
        image.setReport(this);
    }

    public void removeImage(ReportImage image) {
        this.images.remove(image);
        image.setReport(null);
    }
}
