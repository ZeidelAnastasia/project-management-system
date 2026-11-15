package com.projectmanagement.reporting.mapper;

import com.projectmanagement.reporting.dto.event.TaskEventMessage;
import com.projectmanagement.reporting.entity.TaskEventEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskEventMapper {

    public TaskEventEntity toEntity(TaskEventMessage message) {
        return TaskEventEntity.builder()
                .eventId(message.getEventId())
                .eventType(message.getEventType())
                .taskId(message.getTaskId())
                .projectId(message.getProjectId())
                .assigneeId(message.getAssigneeId())
                .oldStatus(message.getOldStatus())
                .newStatus(message.getNewStatus())
                .priority(message.getPriority())
                .dueDate(message.getDueDate())
                .occurredAt(message.getOccurredAt())
                .receivedAt(LocalDateTime.now())
                .build();
    }
}