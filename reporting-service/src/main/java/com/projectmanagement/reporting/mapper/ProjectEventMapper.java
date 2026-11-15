package com.projectmanagement.reporting.mapper;

import com.projectmanagement.reporting.dto.event.ProjectEventMessage;
import com.projectmanagement.reporting.entity.ProjectEventEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProjectEventMapper {

    public ProjectEventEntity toEntity(ProjectEventMessage message) {
        return ProjectEventEntity.builder()
                .eventId(message.getEventId())
                .eventType(message.getEventType())
                .projectId(message.getProjectId())
                .occurredAt(message.getOccurredAt())
                .receivedAt(LocalDateTime.now())
                .build();
    }
}