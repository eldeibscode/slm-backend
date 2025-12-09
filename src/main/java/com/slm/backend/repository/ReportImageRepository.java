package com.slm.backend.repository;

import com.slm.backend.entity.ReportImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportImageRepository extends JpaRepository<ReportImage, Long> {

    List<ReportImage> findByReportIdOrderByDisplayOrderAsc(Long reportId);

    void deleteByReportId(Long reportId);
}
