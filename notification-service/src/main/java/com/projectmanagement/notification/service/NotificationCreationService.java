package com.projectmanagement.notification.service;

import com.projectmanagement.notification.entity.enums.NotificationType;

import java.util.UUID;

public interface NotificationCreationService {

    boolean createNotification(
            UUID recipientId,
            NotificationType type,
            String title,
            String message,
            UUID taskId,
            UUID projectId,
            String sourceEventId
    );
}