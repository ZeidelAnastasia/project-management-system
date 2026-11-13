package com.projectmanagement.communication.kafka;

import com.projectmanagement.communication.event.CommentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentKafkaProducer {

    private final KafkaTemplate<String, CommentCreatedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.comment-created}")
    private String commentCreatedTopic;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        kafkaTemplate.send(
                commentCreatedTopic,
                event.taskId().toString(),
                event
        ).whenComplete((result, exception) -> {
            if (exception != null) {
                log.error(
                        "Failed to publish CommentCreated event: {}",
                        event.eventId(),
                        exception
                );
                return;
            }

            log.info(
                    "CommentCreated event published: {}",
                    event.eventId()
            );
        });
    }
}