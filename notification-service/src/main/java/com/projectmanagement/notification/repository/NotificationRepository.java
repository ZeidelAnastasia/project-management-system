package com.projectmanagement.notification.repository;

import com.projectmanagement.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findAllByRecipientIdOrderByCreatedAtDesc(
            UUID recipientId,
            Pageable pageable
    );

    Page<Notification> findAllByRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(
            UUID recipientId,
            Pageable pageable
    );

    Optional<Notification> findByIdAndRecipientId(
            UUID id,
            UUID recipientId
    );

    long countByRecipientIdAndReadAtIsNull(UUID recipientId);

    boolean existsByRecipientIdAndSourceEventId(
            UUID recipientId,
            String sourceEventId
    );

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.readAt = :readAt
            WHERE n.recipientId = :recipientId
              AND n.readAt IS NULL
            """)
    int markAllAsRead(
            @Param("recipientId") UUID recipientId,
            @Param("readAt") LocalDateTime readAt
    );

    @Modifying
    @Query(value = """
        INSERT INTO notifications (
            id,
            recipient_id,
            type,
            title,
            message,
            task_id,
            project_id,
            source_event_id,
            created_at,
            read_at
        )
        VALUES (
            :id,
            :recipientId,
            :type,
            :title,
            :message,
            :taskId,
            :projectId,
            :sourceEventId,
            :createdAt,
            NULL
        )
        ON CONFLICT (recipient_id, source_event_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfNotExists(
            @Param("id") UUID id,
            @Param("recipientId") UUID recipientId,
            @Param("type") String type,
            @Param("title") String title,
            @Param("message") String message,
            @Param("taskId") UUID taskId,
            @Param("projectId") UUID projectId,
            @Param("sourceEventId") String sourceEventId,
            @Param("createdAt") LocalDateTime createdAt
    );
}