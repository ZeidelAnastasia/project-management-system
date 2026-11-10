package com.projectmanagement.project.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.project.entity.OutboxEvent;
import com.projectmanagement.project.entity.Project;
import com.projectmanagement.project.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectEventOutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.project-created}")
    private String projectCreatedTopic;

    @Value("${app.kafka.topics.project-updated}")
    private String projectUpdatedTopic;

    @Value("${app.kafka.topics.project-archived}")
    private String projectArchivedTopic;

    @Value("${app.kafka.topics.project-restored}")
    private String projectRestoredTopic;

    @Value("${app.kafka.topics.project-completed}")
    private String projectCompletedTopic;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishProjectCreated(Project project) {
        saveEvent(
                project,
                ProjectEventType.PROJECT_CREATED,
                "ProjectCreated",
                projectCreatedTopic
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishProjectUpdated(Project project) {
        saveEvent(
                project,
                ProjectEventType.PROJECT_UPDATED,
                "ProjectUpdated",
                projectUpdatedTopic
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishProjectArchived(Project project) {
        saveEvent(
                project,
                ProjectEventType.PROJECT_ARCHIVED,
                "ProjectArchived",
                projectArchivedTopic
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishProjectRestored(Project project) {
        saveEvent(
                project,
                ProjectEventType.PROJECT_RESTORED,
                "ProjectRestored",
                projectRestoredTopic
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishProjectCompleted(Project project) {
        saveEvent(
                project,
                ProjectEventType.PROJECT_COMPLETED,
                "ProjectCompleted",
                projectCompletedTopic
        );
    }

    private void saveEvent(
            Project project,
            ProjectEventType eventType,
            String eventName,
            String topic
    ) {
        Instant occurredAt = Instant.now();

        ProjectEventPayload payload = new ProjectEventPayload(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus().name(),
                project.getCreatedBy(),
                toInstant(project.getCreatedAt()),
                toInstant(project.getUpdatedAt()),
                toInstant(project.getArchivedAt()),
                toInstant(project.getCompletedAt())
        );

        ProjectEvent<ProjectEventPayload> event = new ProjectEvent<>(
                UUID.randomUUID(),
                eventName,
                occurredAt,
                "project-service",
                payload
        );

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(event.eventId());
        outboxEvent.setAggregateId(project.getId());
        outboxEvent.setEventType(eventType.name());
        outboxEvent.setTopic(topic);
        outboxEvent.setCreatedAt(occurredAt);
        outboxEvent.setAttempts(0);

        try {
            outboxEvent.setPayload(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize project event",
                    exception
            );
        }

        outboxEventRepository.save(outboxEvent);
    }

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null
                ? null
                : dateTime.toInstant(ZoneOffset.UTC);
    }
}