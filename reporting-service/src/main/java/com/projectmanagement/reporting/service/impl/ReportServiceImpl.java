package com.projectmanagement.reporting.service.impl;

import com.projectmanagement.reporting.dto.response.CompletionDynamicsResponse.CompletionDynamicsItem;
import com.projectmanagement.reporting.dto.response.EmployeeWorkloadResponse.EmployeeWorkloadItem;
import com.projectmanagement.reporting.dto.response.ProjectComparisonResponse.ProjectComparisonItem;
import com.projectmanagement.reporting.dto.response.CompletionDynamicsResponse;
import com.projectmanagement.reporting.dto.response.EmployeeWorkloadResponse;
import com.projectmanagement.reporting.dto.response.OverdueTasksResponse;
import com.projectmanagement.reporting.dto.response.ProjectComparisonResponse;
import com.projectmanagement.reporting.dto.response.ReportSummaryResponse;
import com.projectmanagement.reporting.entity.ProjectEventEntity;
import com.projectmanagement.reporting.entity.TaskEventEntity;
import com.projectmanagement.reporting.exception.InvalidReportParameterException;
import com.projectmanagement.reporting.repository.ProjectEventRepository;
import com.projectmanagement.reporting.repository.TaskEventRepository;
import com.projectmanagement.reporting.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String PROJECT_CREATED = "ProjectCreated";
    private static final String PROJECT_COMPLETED = "ProjectCompleted";
    private static final String TASK_COMPLETED_STATUS = "DONE";

    private final TaskEventRepository taskEventRepository;
    private final ProjectEventRepository projectEventRepository;

    @Override
    public ReportSummaryResponse getOverview(
            LocalDate startDate,
            LocalDate endDate
    ) {
        validatePeriod(startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        return ReportSummaryResponse.builder()
                .createdTasks(taskEventRepository.countCreatedTasks(null, start, end))
                .completedTasks(taskEventRepository.countCompletedTasks(null, start, end))
                .overdueTasks(taskEventRepository.countOverdueTasks(null, endDate))
                .createdProjects(projectEventRepository.countCreatedProjects(start, end))
                .completedProjects(projectEventRepository.countCompletedProjects(start, end))
                .build();
    }

    @Override
    public ReportSummaryResponse getProjectReport(
            UUID projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateProjectId(projectId);
        validatePeriod(startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<ProjectEventEntity> projectEvents =
                projectEventRepository.findByProjectIdAndPeriod(
                        projectId,
                        start,
                        end
                );

        long createdProjects = projectEvents.stream()
                .filter(event -> PROJECT_CREATED.equals(event.getEventType()))
                .count();

        long completedProjects = projectEvents.stream()
                .filter(event -> PROJECT_COMPLETED.equals(event.getEventType()))
                .count();

        return ReportSummaryResponse.builder()
                .createdTasks(taskEventRepository.countCreatedTasks(projectId, start, end))
                .completedTasks(taskEventRepository.countCompletedTasks(projectId, start, end))
                .overdueTasks(taskEventRepository.countOverdueTasks(projectId, endDate))
                .createdProjects(createdProjects)
                .completedProjects(completedProjects)
                .build();
    }

    @Override
    public OverdueTasksResponse getOverdueTasks(
            UUID projectId,
            LocalDate date
    ) {
        if (date == null) {
            throw new InvalidReportParameterException(
                    "Report date must not be null"
            );
        }

        return OverdueTasksResponse.builder()
                .projectId(projectId)
                .date(date)
                .overdueTasks(taskEventRepository.countOverdueTasks(projectId, date))
                .build();
    }

    @Override
    public CompletionDynamicsResponse getCompletionDynamics(
            UUID projectId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateProjectId(projectId);
        validatePeriod(startDate, endDate);

        List<CompletionDynamicsItem> dynamics = new ArrayList<>();

        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            LocalDateTime start = currentDate.atStartOfDay();
            LocalDateTime end = currentDate.plusDays(1).atStartOfDay();

            dynamics.add(CompletionDynamicsItem.builder()
                    .date(currentDate)
                    .createdTasks(taskEventRepository.countCreatedTasks(
                            projectId,
                            start,
                            end
                    ))
                    .completedTasks(taskEventRepository.countCompletedTasks(
                            projectId,
                            start,
                            end
                    ))
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return CompletionDynamicsResponse.builder()
                .projectId(projectId)
                .startDate(startDate)
                .endDate(endDate)
                .dynamics(dynamics)
                .build();
    }

    @Override
    public EmployeeWorkloadResponse getEmployeeWorkload(UUID projectId) {
        validateProjectId(projectId);

        List<TaskEventEntity> events =
                taskEventRepository.findByProjectId(projectId);

        Map<UUID, List<TaskEventEntity>> eventsByTask = events.stream()
                .collect(Collectors.groupingBy(TaskEventEntity::getTaskId));

        Map<UUID, EmployeeWorkloadAccumulator> workload = new HashMap<>();

        for (List<TaskEventEntity> taskEvents : eventsByTask.values()) {
            UUID assigneeId = getLatestAssignee(taskEvents);

            if (assigneeId == null) {
                continue;
            }

            EmployeeWorkloadAccumulator accumulator = workload.computeIfAbsent(
                    assigneeId,
                    ignored -> new EmployeeWorkloadAccumulator()
            );

            accumulator.assignedTasks++;

            String status = getLatestStatus(taskEvents);
            LocalDate dueDate = getLatestDueDate(taskEvents);

            if (TASK_COMPLETED_STATUS.equals(status)) {
                accumulator.completedTasks++;
            } else if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
                accumulator.overdueTasks++;
            }
        }

        List<EmployeeWorkloadItem> employees = workload.entrySet()
                .stream()
                .map(entry -> EmployeeWorkloadItem.builder()
                        .employeeId(entry.getKey())
                        .assignedTasks(entry.getValue().assignedTasks)
                        .completedTasks(entry.getValue().completedTasks)
                        .overdueTasks(entry.getValue().overdueTasks)
                        .build())
                .toList();

        return EmployeeWorkloadResponse.builder()
                .projectId(projectId)
                .employees(employees)
                .build();
    }

    @Override
    public ProjectComparisonResponse getProjectComparison(
            LocalDate startDate,
            LocalDate endDate
    ) {
        validatePeriod(startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<UUID> projectIds =
                projectEventRepository.findDistinctProjectIdsByPeriod(start, end);

        List<ProjectComparisonItem> projects = projectIds.stream()
                .distinct()
                .map(projectId -> ProjectComparisonItem.builder()
                        .projectId(projectId)
                        .createdTasks(taskEventRepository.countCreatedTasks(
                                projectId,
                                start,
                                end
                        ))
                        .completedTasks(taskEventRepository.countCompletedTasks(
                                projectId,
                                start,
                                end
                        ))
                        .overdueTasks(taskEventRepository.countOverdueTasks(
                                projectId,
                                endDate
                        ))
                        .build())
                .toList();

        return ProjectComparisonResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .projects(projects)
                .build();
    }

    private UUID getLatestAssignee(List<TaskEventEntity> events) {
        return events.stream()
                .filter(event -> event.getAssigneeId() != null)
                .max(Comparator.comparing(TaskEventEntity::getOccurredAt))
                .map(TaskEventEntity::getAssigneeId)
                .orElse(null);
    }

    private String getLatestStatus(List<TaskEventEntity> events) {
        return events.stream()
                .filter(event -> event.getNewStatus() != null)
                .max(Comparator.comparing(TaskEventEntity::getOccurredAt))
                .map(TaskEventEntity::getNewStatus)
                .orElse(null);
    }

    private LocalDate getLatestDueDate(List<TaskEventEntity> events) {
        return events.stream()
                .filter(event -> event.getDueDate() != null)
                .max(Comparator.comparing(TaskEventEntity::getOccurredAt))
                .map(TaskEventEntity::getDueDate)
                .orElse(null);
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidReportParameterException(
                    "Start date and end date must not be null"
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new InvalidReportParameterException(
                    "Start date must not be after end date"
            );
        }
    }

    private void validateProjectId(UUID projectId) {
        if (projectId == null) {
            throw new InvalidReportParameterException(
                    "Project ID must not be null"
            );
        }
    }

    private static class EmployeeWorkloadAccumulator {

        private long assignedTasks;
        private long completedTasks;
        private long overdueTasks;
    }
}