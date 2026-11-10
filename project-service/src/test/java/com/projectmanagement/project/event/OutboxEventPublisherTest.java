package com.projectmanagement.project.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projectmanagement.project.entity.OutboxEvent;
import com.projectmanagement.project.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.support.SendResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    private static final String TOPIC = "project-created";

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private OutboxEventPublisher outboxEventPublisher;

    private OutboxEvent outboxEvent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(outboxEventPublisher, "batchSize", 100);
        ReflectionTestUtils.setField(outboxEventPublisher, "sendTimeoutMs", 10000L);

        outboxEvent = new OutboxEvent();
        outboxEvent.setId(UUID.randomUUID());
        outboxEvent.setAggregateId(UUID.randomUUID());
        outboxEvent.setEventType("PROJECT_CREATED");
        outboxEvent.setTopic(TOPIC);
        outboxEvent.setPayload("""
                {
                    "eventId": "%s",
                    "eventType": "PROJECT_CREATED",
                    "source": "project-service"
                }
                """.formatted(UUID.randomUUID()));
        outboxEvent.setCreatedAt(Instant.now());
        outboxEvent.setAttempts(0);
    }

    @Test
    void shouldPublishOutboxEvent() {
        when(outboxEventRepository.findUnpublishedEvents(any()))
                .thenReturn(List.of(outboxEvent));

        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send(
                eq(TOPIC),
                eq(outboxEvent.getAggregateId().toString()),
                any()
        )).thenReturn(future);

        outboxEventPublisher.publishPendingEvents();

        verify(kafkaTemplate).send(
                eq(TOPIC),
                eq(outboxEvent.getAggregateId().toString()),
                any()
        );

        assertNotNull(outboxEvent.getPublishedAt());
        assertNull(outboxEvent.getLastError());
        assertEquals(0, outboxEvent.getAttempts());
    }

    @Test
    void shouldKeepEventUnpublishedWhenKafkaFails() {
        when(outboxEventRepository.findUnpublishedEvents(any()))
                .thenReturn(List.of(outboxEvent));

        CompletableFuture<SendResult<String, Object>> failedFuture =
                CompletableFuture.failedFuture(
                        new RuntimeException("Kafka unavailable")
                );

        when(kafkaTemplate.send(
                eq(TOPIC),
                eq(outboxEvent.getAggregateId().toString()),
                any()
        )).thenReturn(failedFuture);

        outboxEventPublisher.publishPendingEvents();

        verify(kafkaTemplate).send(
                eq(TOPIC),
                eq(outboxEvent.getAggregateId().toString()),
                any()
        );

        assertNull(outboxEvent.getPublishedAt());
        assertEquals(1, outboxEvent.getAttempts());
        assertNotNull(outboxEvent.getLastError());
    }

    @Test
    void shouldNotSendAnythingWhenOutboxIsEmpty() {
        when(outboxEventRepository.findUnpublishedEvents(any()))
                .thenReturn(List.of());

        outboxEventPublisher.publishPendingEvents();

        verify(kafkaTemplate, never())
                .send(anyString(), anyString(), any());
    }
}