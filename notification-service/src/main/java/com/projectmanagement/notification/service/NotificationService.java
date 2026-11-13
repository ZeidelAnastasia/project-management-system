package com.projectmanagement.notification.service;

import com.projectmanagement.notification.dto.MarkAllAsReadResponse;
import com.projectmanagement.notification.dto.NotificationPageResponse;
import com.projectmanagement.notification.dto.NotificationResponse;
import com.projectmanagement.notification.dto.UnreadCountResponse;

import java.util.UUID;

public interface NotificationService {

    NotificationPageResponse getNotifications(int page, int size);

    NotificationPageResponse getUnreadNotifications(int page, int size);

    UnreadCountResponse getUnreadCount();

    NotificationResponse markAsRead(UUID notificationId);

    MarkAllAsReadResponse markAllAsRead();
}