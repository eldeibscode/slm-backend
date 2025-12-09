package com.slm.backend.repository;

import com.slm.backend.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Report> findByStatus(Report.Status status, Pageable pageable);

    Page<Report> findByAuthorId(Long authorId, Pageable pageable);

    Page<Report> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.publishedAt DESC")
    List<Report> findLatestPublished(@Param("status") Report.Status status, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Report r LEFT JOIN r.tags t WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:categoryId IS NULL OR r.category.id = :categoryId) AND " +
           "(:authorId IS NULL OR r.author.id = :authorId) AND " +
           "(:search IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.excerpt) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:tagIds IS NULL OR t.id IN :tagIds) AND " +
           "(:dateFrom IS NULL OR r.createdAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR r.createdAt <= :dateTo)")
    Page<Report> findWithFilters(
        @Param("status") Report.Status status,
        @Param("categoryId") Long categoryId,
        @Param("authorId") Long authorId,
        @Param("search") String search,
        @Param("tagIds") List<Long> tagIds,
        @Param("dateFrom") java.time.LocalDateTime dateFrom,
        @Param("dateTo") java.time.LocalDateTime dateTo,
        Pageable pageable
    );

    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status")
    long countByStatus(@Param("status") Report.Status status);
}
