package com.projectmanagement.notification.kafka.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.notification.entity.enums.NotificationType;
import com.projectmanagement.notification.kafka.event.CommentCreatedPayload;
import com.projectmanagement.notification.kafka.event.DomainEvent;
import com.projectmanagement.notification.kafka.event.TaskAssignedPayload;
import com.projectmanagement.notification.kafka.event.TaskStatusChangedPayload;
import com.projectmanagement.notification.service.NotificationCreationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationCreationService notificationCreationService;

    @KafkaListener(
            topics = "${app.kafka.topics.task-assigned}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTaskAssigned(String message) {
        DomainEvent<TaskAssignedPayload> event = deserialize(
                message,
                new TypeReference<>() {}
        );

        validateEvent(event, "TaskAssigned");

        TaskAssignedPayload payload = event.payload();

        notificationCreationService.createNotification(
                payload.assigneeId(),
                NotificationType.TASK_ASSIGNED,
                "Вам назначена задача",
                "Вам назначена задача: " + payload.taskTitle(),
                payload.taskId(),
                payload.projectId(),
                event.eventId()
        );

        log.info("Processed TaskAssigned event: {}", event.eventId());
    }

    @KafkaListener(
            topics = "${app.kafka.topics.task-status-changed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTaskStatusChanged(String message) {
        DomainEvent<TaskStatusChangedPayload> event = deserialize(
                message,
                new TypeReference<>() {}
        );

        validateEvent(event, "TaskStatusChanged");

        TaskStatusChangedPayload payload = event.payload();

        String notificationMessage = String.format(
                "Статус задачи \"%s\" изменён с %s на %s",
                payload.taskTitle(),
                payload.oldStatus(),
                payload.newStatus()
        );

        createNotifications(
                payload.recipientIds(),
                NotificationType.TASK_STATUS_CHANGED,
                "Изменён статус задачи",
                notificationMessage,
                payload.taskId(),
                payload.projectId(),
                event.eventId()
        );

        log.info("Processed TaskStatusChanged event: {}", event.eventId());
    }

    @KafkaListener(
            topics = "${app.kafka.topics.comment-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeCommentCreated(String message) {
        DomainEvent<CommentCreatedPayload> event = deserialize(
                message,
                new TypeReference<>() {}
        );

        validateEvent(event, "CommentCreated");

        CommentCreatedPayload payload = event.payload();

        createNotifications(
                payload.recipientIds(),
                NotificationType.COMMENT_CREATED,
                "Новый комментарий",
                "Добавлен новый комментарий к задаче: " + payload.taskTitle(),
                payload.taskId(),
                payload.projectId(),
                event.eventId()
        );

        log.info("Processed CommentCreated event: {}", event.eventId());
    }

    private void createNotifications(
            List<UUID> recipientIds,
            NotificationType type,
            String title,
            String message,
            UUID taskId,
            UUID projectId,
            String eventId
    ) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return;
        }

        recipientIds.stream()
                .filter(recipientId -> recipientId != null)
                .distinct()
                .forEach(recipientId ->
                        notificationCreationService.createNotification(
                                recipientId,
                                type,
                                title,
                                message,
                                taskId,
                                projectId,
                                eventId
                        )
                );
    }

    private <T> DomainEvent<T> deserialize(
            String message,
            TypeReference<DomainEvent<T>> typeReference
    ) {
        try {
            return objectMapper.readValue(message, typeReference);
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Failed to deserialize Kafka event",
                    exception
            );
        }
    }

    private void validateEvent(
            DomainEvent<?> event,
            String expectedType
    ) {
        if (event == null ||
                event.eventId() == null ||
                event.eventId().isBlank() ||
                !expectedType.equals(event.eventType()) ||
                event.payload() == null) {
            throw new IllegalArgumentException(
                    "Invalid " + expectedType + " event"
            );
        }
    }
}