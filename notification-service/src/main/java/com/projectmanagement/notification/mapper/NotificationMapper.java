package com.projectmanagement.notification.mapper;

import com.projectmanagement.notification.dto.MarkAllAsReadResponse;
import com.projectmanagement.notification.dto.NotificationPageResponse;
import com.projectmanagement.notification.dto.NotificationResponse;
import com.projectmanagement.notification.dto.UnreadCountResponse;
import com.projectmanagement.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getTaskId(),
                notification.getProjectId(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    public NotificationPageResponse toPageResponse(Page<Notification> notifications) {
        return new NotificationPageResponse(
                notifications.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                notifications.getNumber(),
                notifications.getSize(),
                notifications.getTotalElements(),
                notifications.getTotalPages(),
                notifications.isFirst(),
                notifications.isLast()
        );
    }

    public UnreadCountResponse toUnreadCountResponse(long count) {
        return new UnreadCountResponse(count);
    }

    public MarkAllAsReadResponse toMarkAllAsReadResponse(int updatedCount) {
        return new MarkAllAsReadResponse(updatedCount);
    }
}