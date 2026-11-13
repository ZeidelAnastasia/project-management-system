package com.projectmanagement.notification.service.impl;

import com.projectmanagement.notification.entity.enums.NotificationType;
import com.projectmanagement.notification.repository.NotificationRepository;
import com.projectmanagement.notification.service.NotificationCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationCreationServiceImpl implements NotificationCreationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public boolean createNotification(
            UUID recipientId,
            NotificationType type,
            String title,
            String message,
            UUID taskId,
            UUID projectId,
            String sourceEventId
    ) {
        Objects.requireNonNull(recipientId, "Recipient ID must not be null");
        Objects.requireNonNull(type, "Notification type must not be null");
        Objects.requireNonNull(title, "Notification title must not be null");
        Objects.requireNonNull(message, "Notification message must not be null");

        if (sourceEventId == null || sourceEventId.isBlank()) {
            throw new IllegalArgumentException(
                    "Source event ID must not be empty"
            );
        }

        int insertedCount = notificationRepository.insertIfNotExists(
                UUID.randomUUID(),
                recipientId,
                type.name(),
                title,
                message,
                taskId,
                projectId,
                sourceEventId,
                LocalDateTime.now()
        );

        return insertedCount > 0;
    }
}