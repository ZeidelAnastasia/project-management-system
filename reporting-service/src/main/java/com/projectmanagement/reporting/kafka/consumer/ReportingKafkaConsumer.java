package com.projectmanagement.reporting.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.reporting.kafka.dto.KafkaEventMessage;
import com.projectmanagement.reporting.kafka.processor.ReportingKafkaEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportingKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final ReportingKafkaEventProcessor eventProcessor;

    @KafkaListener(
            topics = "${app.kafka.topics.project-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProjectCreated(
            String message,
            Acknowledgment acknowledgment
    ) throws Exception {
        consume(message, acknowledgment);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.project-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProjectCompleted(
            String message,
            Acknowledgment acknowledgment
    ) throws Exception {
        consume(message, acknowledgment);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.task-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTaskCreated(
            String message,
            Acknowledgment acknowledgment
    ) throws Exception {
        consume(message, acknowledgment);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.task-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTaskUpdated(
            String message,
            Acknowledgment acknowledgment
    ) throws Exception {
        consume(message, acknowledgment);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.task-status-changed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTaskStatusChanged(
            String message,
            Acknowledgment acknowledgment
    ) throws Exception {
        consume(message, acknowledgment);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.task-deadline-changed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTaskDeadlineChanged(
            String message,
            Acknowledgment acknowledgment
    ) throws Exception {
        consume(message, acknowledgment);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.task-effort-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTaskEffortUpdated(
            String message,
            Acknowledgment acknowledgment
    ) throws Exception {
        consume(message, acknowledgment);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.task-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTaskDeleted(
            String message,
            Acknowledgment acknowledgment
    ) throws Exception {
        consume(message, acknowledgment);
    }

    private void consume(
            String message,
            Acknowledgment acknowledgment
    ) throws Exception {
        KafkaEventMessage event = objectMapper.readValue(
                message,
                KafkaEventMessage.class
        );

        eventProcessor.process(event);

        acknowledgment.acknowledge();

        log.info(
                "Kafka event processed successfully: eventId={}, eventType={}",
                event.getEventId(),
                event.getEventType()
        );
    }
}