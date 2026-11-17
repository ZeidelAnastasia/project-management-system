package com.projectmanagement.intelligence.kafka;

import com.projectmanagement.intelligence.kafka.event.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntelligenceKafkaConsumer {

    private final KafkaEventDeserializer deserializer;
    private final IntelligenceEventProcessor eventProcessor;

    @KafkaListener(
            topics = {
                    KafkaTopics.PROJECT_CREATED,
                    KafkaTopics.PROJECT_UPDATED,
                    KafkaTopics.PROJECT_ARCHIVED,
                    KafkaTopics.PROJECT_RESTORED,
                    KafkaTopics.PROJECT_COMPLETED
            },
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeProjectEvent(String message) {
        KafkaEvent event = deserializer.deserialize(message);

        log.info("Received project event: {}", event.eventType());

        eventProcessor.processProjectEvent(event);
    }

    @KafkaListener(
            topics = {
                    KafkaTopics.TASK_CREATED,
                    KafkaTopics.TASK_UPDATED,
                    KafkaTopics.TASK_ASSIGNED,
                    KafkaTopics.TASK_STATUS_CHANGED,
                    KafkaTopics.TASK_DEADLINE_CHANGED,
                    KafkaTopics.TASK_EFFORT_UPDATED,
                    KafkaTopics.TASK_DELETED
            },
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTaskEvent(String message) {
        KafkaEvent event = deserializer.deserialize(message);

        log.info("Received task event: {}", event.eventType());

        eventProcessor.processTaskEvent(event);
    }
}