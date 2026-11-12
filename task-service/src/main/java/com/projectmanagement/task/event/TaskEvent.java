package com.projectmanagement.task.event;

import java.time.Instant;
import java.util.UUID;

public record TaskEvent<T>(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        T payload
) {
}