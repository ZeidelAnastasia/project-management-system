package com.projectmanagement.intelligence.risk.model;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record ProjectRiskMetrics(
        UUID projectId,
        int totalTasks,
        int completedTasks,
        int unfinishedTasks,
        int overdueTasks,
        int highPriorityTasks,
        int highComplexityTasks,
        int reassignmentCount,
        int deadlineChangeCount,
        int overloadedAssignees,
        int activeAssignees,
        double remainingEffort,
        double completedEffort,
        double averageComplexity,
        double tasksPerDay,
        double averageTaskDuration,
        LocalDate plannedEndDate,
        LocalDate predictedCompletionDate
) {}