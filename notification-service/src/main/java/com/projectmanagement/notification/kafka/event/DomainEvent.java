package com.projectmanagement.notification.kafka.event;

import java.time.Instant;

public record DomainEvent<T>(
        String eventId,
        String eventType,
        Instant occurredAt,
        String source,
        T payload
) {}