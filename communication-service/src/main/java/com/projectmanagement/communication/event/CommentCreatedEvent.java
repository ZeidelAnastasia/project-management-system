package com.projectmanagement.communication.event;

import java.time.Instant;
import java.util.UUID;

public record CommentCreatedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID commentId,
        UUID taskId,
        UUID projectId,
        UUID authorId,
        String body
) {
}