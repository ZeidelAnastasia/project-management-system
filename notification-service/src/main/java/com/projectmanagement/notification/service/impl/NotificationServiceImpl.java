package com.projectmanagement.notification.service.impl;

import com.projectmanagement.notification.dto.MarkAllAsReadResponse;
import com.projectmanagement.notification.dto.NotificationPageResponse;
import com.projectmanagement.notification.dto.NotificationResponse;
import com.projectmanagement.notification.dto.UnreadCountResponse;
import com.projectmanagement.notification.entity.Notification;
import com.projectmanagement.notification.exception.NotificationNotFoundException;
import com.projectmanagement.notification.mapper.NotificationMapper;
import com.projectmanagement.notification.repository.NotificationRepository;
import com.projectmanagement.notification.security.CurrentUserService;
import com.projectmanagement.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final CurrentUserService currentUserService;

    @Override
    public NotificationPageResponse getNotifications(int page, int size) {
        UUID recipientId = currentUserService.getCurrentUserId();

        Pageable pageable = createPageable(page, size);

        return notificationMapper.toPageResponse(
                notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(
                        recipientId,
                        pageable
                )
        );
    }

    @Override
    public NotificationPageResponse getUnreadNotifications(int page, int size) {
        UUID recipientId = currentUserService.getCurrentUserId();

        Pageable pageable = createPageable(page, size);

        return notificationMapper.toPageResponse(
                notificationRepository.findAllByRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(
                        recipientId,
                        pageable
                )
        );
    }

    @Override
    public UnreadCountResponse getUnreadCount() {
        UUID recipientId = currentUserService.getCurrentUserId();

        return notificationMapper.toUnreadCountResponse(
                notificationRepository.countByRecipientIdAndReadAtIsNull(recipientId)
        );
    }

    @Override
    public NotificationResponse markAsRead(UUID notificationId) {
        UUID recipientId = currentUserService.getCurrentUserId();

        Notification notification = notificationRepository
                .findByIdAndRecipientId(notificationId, recipientId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found"
                ));

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    public MarkAllAsReadResponse markAllAsRead() {
        UUID recipientId = currentUserService.getCurrentUserId();

        int updatedCount = notificationRepository.markAllAsRead(
                recipientId,
                LocalDateTime.now()
        );

        return notificationMapper.toMarkAllAsReadResponse(updatedCount);
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }

        return PageRequest.of(page, size);
    }
}