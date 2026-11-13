package com.projectmanagement.notification.controller;

import com.projectmanagement.notification.controller.api.NotificationApi;
import com.projectmanagement.notification.dto.MarkAllAsReadResponse;
import com.projectmanagement.notification.dto.NotificationPageResponse;
import com.projectmanagement.notification.dto.NotificationResponse;
import com.projectmanagement.notification.dto.UnreadCountResponse;
import com.projectmanagement.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    public NotificationPageResponse getNotifications(int page, int size) {
        return notificationService.getNotifications(page, size);
    }

    @Override
    public NotificationPageResponse getUnreadNotifications(int page, int size) {
        return notificationService.getUnreadNotifications(page, size);
    }

    @Override
    public UnreadCountResponse getUnreadCount() {
        return notificationService.getUnreadCount();
    }

    @Override
    public NotificationResponse markAsRead(UUID id) {
        return notificationService.markAsRead(id);
    }

    @Override
    public MarkAllAsReadResponse markAllAsRead() {
        return notificationService.markAllAsRead();
    }
}