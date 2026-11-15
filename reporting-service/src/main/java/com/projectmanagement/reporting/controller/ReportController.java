package com.projectmanagement.reporting.controller;

import com.projectmanagement.reporting.controller.api.ReportApi;
import com.projectmanagement.reporting.dto.response.CompletionDynamicsResponse;
import com.projectmanagement.reporting.dto.response.EmployeeWorkloadResponse;
import com.projectmanagement.reporting.dto.response.OverdueTasksResponse;
import com.projectmanagement.reporting.dto.response.ProjectComparisonResponse;
import com.projectmanagement.reporting.dto.response.ReportSummaryResponse;
import com.projectmanagement.reporting.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReportController implements ReportApi {

    private final ReportService reportService;

    @Override
    public ReportSummaryResponse getOverview(
            LocalDate startDate,
            LocalDate endDate
    ) {
        return reportService.getOverview(startDate, endDate);
    }

    @Override
    public ReportSummaryResponse getProjectReport(
            UUID projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return reportService.getProjectReport(projectId, startDate, endDate);
    }

    @Override
    public OverdueTasksResponse getOverdueTasks(
            UUID projectId,
            LocalDate date
    ) {
        return reportService.getOverdueTasks(projectId, date);
    }

    @Override
    public CompletionDynamicsResponse getCompletionDynamics(
            UUID projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return reportService.getCompletionDynamics(projectId, startDate, endDate);
    }

    @Override
    public EmployeeWorkloadResponse getEmployeeWorkload(UUID projectId) {
        return reportService.getEmployeeWorkload(projectId);
    }

    @Override
    public ProjectComparisonResponse getProjectComparison(
            LocalDate startDate,
            LocalDate endDate
    ) {
        return reportService.getProjectComparison(startDate, endDate);
    }
}