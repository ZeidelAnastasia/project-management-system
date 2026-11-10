package com.projectmanagement.project.event;

import java.time.Instant;
import java.util.UUID;

public record ProjectEvent<T>(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        String source,
        T payload
) {
}