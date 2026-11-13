package com.projectmanagement.notification.kafka.event;

import java.util.UUID;

public record TaskAssignedPayload(
        UUID taskId,
        UUID projectId,
        UUID assigneeId,
        String taskTitle
) {}