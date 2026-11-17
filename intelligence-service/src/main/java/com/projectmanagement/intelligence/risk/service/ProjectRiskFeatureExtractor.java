package com.projectmanagement.intelligence.risk.service;

import com.projectmanagement.intelligence.entity.TaskAnalysisEntity;
import com.projectmanagement.intelligence.entity.TaskSnapshotEntity;
import com.projectmanagement.intelligence.repository.TaskAnalysisRepository;
import com.projectmanagement.intelligence.repository.TaskSnapshotRepository;
import com.projectmanagement.intelligence.risk.model.ProjectRiskMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectRiskFeatureExtractor {

    private static final String DONE_STATUS = "DONE";
    private static final int OVERLOADED_TASKS_THRESHOLD = 5;

    private final TaskSnapshotRepository taskSnapshotRepository;
    private final TaskAnalysisRepository taskAnalysisRepository;

    public ProjectRiskMetrics extract(
            UUID projectId,
            LocalDate plannedEndDate
    ) {
        List<TaskSnapshotEntity> tasks =
                taskSnapshotRepository.findAllByProjectId(projectId);

        if (tasks.isEmpty()) {
            throw new IllegalArgumentException(
                    "Project has no tasks: " + projectId
            );
        }

        LocalDate today = LocalDate.now();

        List<TaskSnapshotEntity> completedTasks = tasks.stream()
                .filter(task -> DONE_STATUS.equals(task.getStatus()))
                .toList();

        List<TaskSnapshotEntity> unfinishedTasks = tasks.stream()
                .filter(task -> !DONE_STATUS.equals(task.getStatus()))
                .toList();

        int overdueTasks = (int) unfinishedTasks.stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> task.getDueDate().isBefore(today))
                .count();

        int highPriorityTasks = (int) unfinishedTasks.stream()
                .filter(task -> "HIGH".equals(task.getPriority()))
                .count();

        Map<UUID, TaskAnalysisEntity> analyses = tasks.stream()
                .map(TaskSnapshotEntity::getTaskId)
                .map(taskAnalysisRepository::findByTaskId)
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(
                        TaskAnalysisEntity::getTaskId,
                        Function.identity(),
                        (first, second) -> second
                ));

        int highComplexityTasks = (int) unfinishedTasks.stream()
                .map(task -> analyses.get(task.getTaskId()))
                .filter(Objects::nonNull)
                .filter(analysis -> analysis.getComplexityScore() >= 70)
                .count();

        double totalComplexity = unfinishedTasks.stream()
                .map(task -> analyses.get(task.getTaskId()))
                .filter(Objects::nonNull)
                .mapToDouble(TaskAnalysisEntity::getComplexityScore)
                .sum();

        long analyzedTasks = unfinishedTasks.stream()
                .map(TaskSnapshotEntity::getTaskId)
                .filter(analyses::containsKey)
                .count();

        double averageComplexity = analyzedTasks == 0
                ? 0
                : totalComplexity / analyzedTasks;

        double remainingEffort = unfinishedTasks.stream()
                .mapToDouble(task -> {
                    TaskAnalysisEntity analysis = analyses.get(task.getTaskId());

                    if (analysis != null && analysis.getPredictedEffort() != null) {
                        return analysis.getPredictedEffort();
                    }

                    return task.getEstimatedEffort() == null
                            ? 0
                            : task.getEstimatedEffort();
                })
                .sum();

        double completedEffort = completedTasks.stream()
                .filter(task -> task.getActualEffort() != null)
                .mapToDouble(TaskSnapshotEntity::getActualEffort)
                .sum();

        int reassignmentCount = tasks.stream()
                .mapToInt(TaskSnapshotEntity::getReassignmentCount)
                .sum();

        int deadlineChangeCount = tasks.stream()
                .mapToInt(TaskSnapshotEntity::getDeadlineChangeCount)
                .sum();

        Map<UUID, Long> assigneeWorkload = unfinishedTasks.stream()
                .filter(task -> task.getAssigneeId() != null)
                .collect(Collectors.groupingBy(
                        TaskSnapshotEntity::getAssigneeId,
                        Collectors.counting()
                ));

        int activeAssignees = assigneeWorkload.size();

        int overloadedAssignees = (int) assigneeWorkload.values()
                .stream()
                .filter(count -> count >= OVERLOADED_TASKS_THRESHOLD)
                .count();

        double tasksPerDay = calculateTasksPerDay(tasks, completedTasks);
        double averageTaskDuration = calculateAverageTaskDuration(completedTasks);

        return ProjectRiskMetrics.builder()
                .projectId(projectId)
                .totalTasks(tasks.size())
                .completedTasks(completedTasks.size())
                .unfinishedTasks(unfinishedTasks.size())
                .overdueTasks(overdueTasks)
                .highPriorityTasks(highPriorityTasks)
                .highComplexityTasks(highComplexityTasks)
                .reassignmentCount(reassignmentCount)
                .deadlineChangeCount(deadlineChangeCount)
                .overloadedAssignees(overloadedAssignees)
                .activeAssignees(activeAssignees)
                .remainingEffort(remainingEffort)
                .completedEffort(completedEffort)
                .averageComplexity(averageComplexity)
                .tasksPerDay(tasksPerDay)
                .averageTaskDuration(averageTaskDuration)
                .plannedEndDate(plannedEndDate)
                .build();
    }

    private double calculateTasksPerDay(
            List<TaskSnapshotEntity> tasks,
            List<TaskSnapshotEntity> completedTasks
    ) {
        if (completedTasks.isEmpty()) {
            return 0;
        }

        LocalDateTime firstTaskDate = tasks.stream()
                .map(TaskSnapshotEntity::getCreatedAt)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        if (firstTaskDate == null) {
            return 0;
        }

        long elapsedDays = Math.max(
                1,
                ChronoUnit.DAYS.between(firstTaskDate.toLocalDate(), LocalDate.now())
        );

        return (double) completedTasks.size() / elapsedDays;
    }

    private double calculateAverageTaskDuration(
            List<TaskSnapshotEntity> completedTasks
    ) {
        return completedTasks.stream()
                .filter(task -> task.getCreatedAt() != null)
                .filter(task -> task.getCompletedAt() != null)
                .mapToLong(task -> Math.max(
                        1,
                        ChronoUnit.DAYS.between(
                                task.getCreatedAt(),
                                task.getCompletedAt()
                        )
                ))
                .average()
                .orElse(0);
    }
}