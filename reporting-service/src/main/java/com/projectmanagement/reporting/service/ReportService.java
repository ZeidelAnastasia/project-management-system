package com.projectmanagement.reporting.service;

import com.projectmanagement.reporting.dto.response.CompletionDynamicsResponse;
import com.projectmanagement.reporting.dto.response.EmployeeWorkloadResponse;
import com.projectmanagement.reporting.dto.response.OverdueTasksResponse;
import com.projectmanagement.reporting.dto.response.ProjectComparisonResponse;
import com.projectmanagement.reporting.dto.response.ReportSummaryResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface ReportService {

    ReportSummaryResponse getOverview(
            LocalDate startDate,
            LocalDate endDate
    );

    ReportSummaryResponse getProjectReport(
            UUID projectId,
            LocalDate startDate,
            LocalDate endDate
    );

    OverdueTasksResponse getOverdueTasks(
            UUID projectId,
            LocalDate date
    );

    CompletionDynamicsResponse getCompletionDynamics(
            UUID projectId,
            LocalDate startDate,
            LocalDate endDate
    );

    EmployeeWorkloadResponse getEmployeeWorkload(UUID projectId);

    ProjectComparisonResponse getProjectComparison(
            LocalDate startDate,
            LocalDate endDate
    );
}