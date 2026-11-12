package com.projectmanagement.task.event;

import com.projectmanagement.task.entity.enums.TaskPriority;
import com.projectmanagement.task.entity.enums.TaskStatus;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record TaskCreatedPayload(
        UUID taskId,
        UUID projectId,
        UUID createdById,
        UUID assigneeId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Integer estimatedEffort,
        Integer actualEffort,
        Set<UUID> labelIds
) {
}