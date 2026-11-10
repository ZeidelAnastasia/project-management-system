package com.projectmanagement.project.repository;

import com.projectmanagement.project.entity.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT event
            FROM OutboxEvent event
            WHERE event.publishedAt IS NULL
            ORDER BY event.createdAt
            """)
    List<OutboxEvent> findUnpublishedEvents(Pageable pageable);
}