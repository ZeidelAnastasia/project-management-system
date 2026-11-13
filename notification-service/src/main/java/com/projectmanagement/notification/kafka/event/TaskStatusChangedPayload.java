package com.projectmanagement.notification.kafka.event;

import java.util.List;
import java.util.UUID;

public record TaskStatusChangedPayload(
        UUID taskId,
        UUID projectId,
        String taskTitle,
        String oldStatus,
        String newStatus,
        UUID changedBy,
        List<UUID> recipientIds
) {}