package com.projectmanagement.task.event;

import com.projectmanagement.task.entity.enums.TaskStatus;

import java.util.UUID;

public record TaskStatusChangedPayload(
        UUID taskId,
        UUID projectId,
        UUID assigneeId,
        TaskStatus oldStatus,
        TaskStatus newStatus
) {
}