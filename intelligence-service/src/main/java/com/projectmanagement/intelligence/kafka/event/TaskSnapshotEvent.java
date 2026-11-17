package com.projectmanagement.intelligence.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskSnapshotEvent(
        UUID taskId,
        UUID projectId,
        UUID assigneeId,
        String title,
        String description,
        String priority,
        String status,
        LocalDate dueDate,
        Integer estimatedEffort,
        Integer actualEffort,
        List<String> labels,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        Integer statusChangeCount,
        Integer deadlineChangeCount,
        Integer reassignmentCount
) {
}