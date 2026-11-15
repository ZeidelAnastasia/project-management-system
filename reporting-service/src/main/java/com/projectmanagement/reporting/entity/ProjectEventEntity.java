
package com.projectmanagement.reporting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEventEntity {

    private UUID eventId;

    private String eventType;

    private UUID projectId;

    private LocalDateTime occurredAt;

    private LocalDateTime receivedAt;
}
