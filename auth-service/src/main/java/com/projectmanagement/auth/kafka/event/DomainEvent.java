package com.projectmanagement.auth.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record DomainEvent<T>(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        String source,
        T payload
) {
}