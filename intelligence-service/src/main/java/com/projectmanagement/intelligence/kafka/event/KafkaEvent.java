package com.projectmanagement.intelligence.kafka.event;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

public record KafkaEvent(
        UUID eventId,
        String eventType,
        LocalDateTime occurredAt,
        JsonNode payload
) {
}