package com.projectmanagement.reporting.kafka.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.projectmanagement.reporting.entity.ProjectEventEntity;
import com.projectmanagement.reporting.entity.TaskEventEntity;
import com.projectmanagement.reporting.kafka.ReportingEventType;
import com.projectmanagement.reporting.kafka.dto.KafkaEventMessage;
import com.projectmanagement.reporting.repository.ProjectEventRepository;
import com.projectmanagement.reporting.repository.TaskEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportingKafkaEventProcessor {

    private final TaskEventRepository taskEventRepository;
    private final ProjectEventRepository projectEventRepository;

    public void process(KafkaEventMessage event) {
        validateEvent(event);

        ReportingEventType eventType = ReportingEventType.fromValue(
                event.getEventType()
        );

        switch (eventType) {
            case PROJECT_CREATED, PROJECT_COMPLETED ->
                    processProjectEvent(event, eventType);

            case TASK_CREATED,
                 TASK_UPDATED,
                 TASK_STATUS_CHANGED,
                 TASK_DEADLINE_CHANGED,
                 TASK_EFFORT_UPDATED,
                 TASK_DELETED ->
                    processTaskEvent(event, eventType);
        }
    }

    private void processTaskEvent(
            KafkaEventMessage event,
            ReportingEventType eventType
    ) {
        JsonNode payload = event.getPayload();

        TaskEventEntity entity = TaskEventEntity.builder()
                .eventId(event.getEventId())
                .eventType(eventType.getValue())
                .taskId(readUuid(payload, "taskId"))
                .projectId(readUuid(payload, "projectId"))
                .assigneeId(readNullableUuid(payload, "assigneeId"))
                .oldStatus(readNullableText(payload, "oldStatus"))
                .newStatus(readNullableText(payload, "newStatus"))
                .priority(readNullableText(payload, "priority"))
                .dueDate(readNullableDate(payload, "dueDate"))
                .occurredAt(event.getOccurredAt().toLocalDateTime())
                .receivedAt(LocalDateTime.now())
                .build();

        taskEventRepository.save(entity);
    }

    private void processProjectEvent(
            KafkaEventMessage event,
            ReportingEventType eventType
    ) {
        JsonNode payload = event.getPayload();

        ProjectEventEntity entity = ProjectEventEntity.builder()
                .eventId(event.getEventId())
                .eventType(eventType.getValue())
                .projectId(readUuid(payload, "projectId"))
                .occurredAt(event.getOccurredAt().toLocalDateTime())
                .receivedAt(LocalDateTime.now())
                .build();

        projectEventRepository.save(entity);
    }

    private void validateEvent(KafkaEventMessage event) {
        if (event == null) {
            throw new IllegalArgumentException("Kafka event must not be null");
        }

        if (event.getEventId() == null) {
            throw new IllegalArgumentException("Event ID must not be null");
        }

        if (event.getEventType() == null || event.getEventType().isBlank()) {
            throw new IllegalArgumentException("Event type must not be empty");
        }

        if (event.getOccurredAt() == null) {
            throw new IllegalArgumentException("Event timestamp must not be null");
        }

        if (event.getPayload() == null || event.getPayload().isNull()
                || !event.getPayload().isObject()) {
            throw new IllegalArgumentException("Event payload must be a JSON object");
        }
    }

    private UUID readUuid(JsonNode payload, String fieldName) {
        String value = readRequiredText(payload, fieldName);

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid UUID in field: " + fieldName,
                    exception
            );
        }
    }

    private UUID readNullableUuid(JsonNode payload, String fieldName) {
        String value = readNullableText(payload, fieldName);

        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid UUID in field: " + fieldName,
                    exception
            );
        }
    }

    private String readRequiredText(JsonNode payload, String fieldName) {
        JsonNode node = payload.get(fieldName);

        if (node == null || node.isNull() || !node.isValueNode()
                || node.asText().isBlank()) {
            throw new IllegalArgumentException(
                    "Required field is missing: " + fieldName
            );
        }

        return node.asText();
    }

    private String readNullableText(JsonNode payload, String fieldName) {
        JsonNode node = payload.get(fieldName);

        if (node == null || node.isNull() || !node.isValueNode()
                || node.asText().isBlank()) {
            return null;
        }

        return node.asText();
    }

    private LocalDate readNullableDate(JsonNode payload, String fieldName) {
        String value = readNullableText(payload, fieldName);

        if (value == null) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid date in field: " + fieldName,
                    exception
            );
        }
    }
}