package com.projectmanagement.auth.kafka.publisher;

import com.projectmanagement.auth.event.UserRegisteredEvent;
import com.projectmanagement.auth.kafka.event.DomainEvent;
import com.projectmanagement.auth.kafka.event.UserRegisteredPayload;
import com.projectmanagement.auth.kafka.topic.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredKafkaPublisher {

    private static final String SOURCE = "auth-service";

    private final KafkaTemplate<String, DomainEvent<UserRegisteredPayload>> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        DomainEvent<UserRegisteredPayload> message = new DomainEvent<>(
                UUID.randomUUID(),
                "UserRegistered",
                Instant.now(),
                SOURCE,
                event.payload()
        );

        String key = event.payload().userId().toString();

        kafkaTemplate.send(KafkaTopics.USER_REGISTERED, key, message)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error(
                                "Failed to publish UserRegistered event for user {}",
                                key,
                                exception
                        );
                        return;
                    }

                    log.info(
                            "UserRegistered event published for user {}",
                            key
                    );
                });
    }
}