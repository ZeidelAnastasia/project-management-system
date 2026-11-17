package com.projectmanagement.intelligence.analysis.service.impl;

import com.projectmanagement.intelligence.analysis.model.TaskFeatures;
import com.projectmanagement.intelligence.analysis.service.TaskFeatureExtractor;
import com.projectmanagement.intelligence.dto.request.TaskAnalysisRequest;
import com.projectmanagement.intelligence.entity.TaskSnapshotEntity;
import com.projectmanagement.intelligence.repository.TaskSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskFeatureExtractorImpl implements TaskFeatureExtractor {

    private final TaskSnapshotRepository taskSnapshotRepository;

    @Override
    public TaskFeatures extract(
            TaskAnalysisRequest request,
            TaskSnapshotEntity task
    ) {
        List<TaskSnapshotEntity> historicalTasks =
                taskSnapshotRepository.findAllByStatusAndActualEffortIsNotNull("DONE")
                        .stream()
                        .filter(item -> !item.getTaskId().equals(task.getTaskId()))
                        .filter(item -> isSimilar(request, item))
                        .toList();

        double averageHistoricalEffort = historicalTasks.isEmpty()
                ? 0
                : historicalTasks.stream()
                .mapToInt(TaskSnapshotEntity::getActualEffort)
                .average()
                .orElse(0);

        List<TaskSnapshotEntity> assigneeTasks = task.getAssigneeId() == null
                ? List.of()
                : taskSnapshotRepository.findAllByAssigneeIdAndStatusNot(
                task.getAssigneeId(),
                "DONE"
        );

        int activeAssigneeEffort = assigneeTasks.stream()
                .filter(item -> !item.getTaskId().equals(task.getTaskId()))
                .mapToInt(item -> item.getEstimatedEffort() == null
                        ? 0
                        : item.getEstimatedEffort())
                .sum();

        int activeAssigneeTasks = (int) assigneeTasks.stream()
                .filter(item -> !item.getTaskId().equals(task.getTaskId()))
                .count();

        int daysUntilDeadline = task.getDueDate() == null
                ? 0
                : (int) Math.max(
                0,
                ChronoUnit.DAYS.between(LocalDate.now(), task.getDueDate())
        );

        return new TaskFeatures(
                request.title().length(),
                request.description().length(),
                request.labels() == null ? 0 : request.labels().size(),
                getPriorityWeight(request.priority()),
                request.estimatedEffort() == null ? 0 : request.estimatedEffort(),
                task.getStatusChangeCount(),
                task.getDeadlineChangeCount(),
                task.getReassignmentCount(),
                activeAssigneeTasks,
                activeAssigneeEffort,
                daysUntilDeadline,
                averageHistoricalEffort,
                historicalTasks.size()
        );
    }

    private boolean isSimilar(
            TaskAnalysisRequest request,
            TaskSnapshotEntity historicalTask
    ) {
        boolean samePriority = request.priority().equals(historicalTask.getPriority());

        List<String> labels = request.labels() == null
                ? List.of()
                : request.labels();

        boolean hasCommonLabel = labels.stream()
                .anyMatch(label -> historicalTask.getLabels() != null
                        && historicalTask.getLabels().contains(label));

        return samePriority || hasCommonLabel;
    }

    private int getPriorityWeight(String priority) {
        return switch (priority.toUpperCase()) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            default -> 1;
        };
    }
}