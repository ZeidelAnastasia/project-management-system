package com.projectmanagement.task.event;

import com.projectmanagement.task.entity.enums.TaskPriority;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record TaskUpdatedPayload(
        UUID taskId,
        UUID projectId,
        String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate,
        Integer estimatedEffort,
        Integer actualEffort,
        Set<UUID> labelIds
) {
}
