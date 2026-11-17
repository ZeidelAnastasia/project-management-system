package com.projectmanagement.intelligence.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.intelligence.kafka.event.KafkaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventDeserializer {

    private final ObjectMapper objectMapper;

    public KafkaEvent deserialize(String message) {
        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);

            if (event.eventId() == null
                    || event.eventType() == null
                    || event.occurredAt() == null
                    || event.payload() == null) {
                throw new IllegalArgumentException("Kafka event contains missing required fields");
            }

            return event;
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to deserialize Kafka event", exception);
        }
    }
}