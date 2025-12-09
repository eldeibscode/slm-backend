package com.slm.backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportListResponse {
    private List<ReportDto> reports;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;
}
