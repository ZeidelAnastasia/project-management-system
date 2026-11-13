package com.projectmanagement.notification.kafka.event;

import java.util.List;
import java.util.UUID;

public record CommentCreatedPayload(
        UUID taskId,
        UUID projectId,
        String taskTitle,
        UUID authorId,
        List<UUID> recipientIds
) {}