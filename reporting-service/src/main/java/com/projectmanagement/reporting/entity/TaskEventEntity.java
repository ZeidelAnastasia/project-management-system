
package com.projectmanagement.reporting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEventEntity {

    private UUID eventId;

    private String eventType;

    private UUID taskId;

    private UUID projectId;

    private UUID assigneeId;

    private String oldStatus;

    private String newStatus;

    private String priority;

    private LocalDate dueDate;

    private LocalDateTime occurredAt;

    private LocalDateTime receivedAt;
}
