package com.projectmanagement.project.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.project.entity.OutboxEvent;
import com.projectmanagement.project.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.outbox.batch-size:100}")
    private int batchSize;

    @Value("${app.kafka.outbox.send-timeout-ms:10000}")
    private long sendTimeoutMs;

    @Scheduled(
            fixedDelayString = "${app.kafka.outbox.publish-interval-ms:5000}"
    )
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository
                .findUnpublishedEvents(PageRequest.of(0, batchSize));

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {
        try {
            JsonNode payload = objectMapper.readTree(event.getPayload());

            kafkaTemplate.send(
                    event.getTopic(),
                    event.getAggregateId().toString(),
                    payload
            ).get(sendTimeoutMs, TimeUnit.MILLISECONDS);

            event.setPublishedAt(Instant.now());
            event.setLastError(null);

            log.info(
                    "Kafka event published: eventId={}, eventType={}, topic={}",
                    event.getId(),
                    event.getEventType(),
                    event.getTopic()
            );
        } catch (Exception exception) {
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(getErrorMessage(exception));

            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            log.error(
                    "Failed to publish Kafka event: eventId={}, eventType={}, attempts={}",
                    event.getId(),
                    event.getEventType(),
                    event.getAttempts(),
                    exception
            );
        }
    }

    private String getErrorMessage(Exception exception) {
        String message = exception.getMessage();

        if (message == null) {
            return exception.getClass().getSimpleName();
        }

        return message.length() > 2000
                ? message.substring(0, 2000)
                : message;
    }
}