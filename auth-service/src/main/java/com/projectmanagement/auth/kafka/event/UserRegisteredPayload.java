package com.projectmanagement.auth.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredPayload(
        UUID userId,
        String email,
        Instant registeredAt
) {
}