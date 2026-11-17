package com.projectmanagement.intelligence.kafka.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.intelligence.analysis.service.TaskSnapshotService;
import com.projectmanagement.intelligence.kafka.IntelligenceEventProcessor;
import com.projectmanagement.intelligence.kafka.event.KafkaEvent;
import com.projectmanagement.intelligence.kafka.event.ProjectSnapshotEvent;
import com.projectmanagement.intelligence.kafka.event.TaskSnapshotEvent;
import com.projectmanagement.intelligence.risk.service.ProjectSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligenceEventProcessorImpl implements IntelligenceEventProcessor {

    private static final Set<String> PROJECT_SNAPSHOT_EVENTS = Set.of(
            "ProjectCreated",
            "ProjectUpdated"
    );

    private static final Set<String> PROJECT_STATUS_EVENTS = Set.of(
            "ProjectArchived",
            "ProjectRestored",
            "ProjectCompleted"
    );

    private static final Set<String> TASK_EVENTS = Set.of(
            "TaskCreated",
            "TaskUpdated",
            "TaskAssigned",
            "TaskStatusChanged",
            "TaskDeadlineChanged",
            "TaskEffortUpdated"
    );

    private final ObjectMapper objectMapper;
    private final TaskSnapshotService taskSnapshotService;
    private final ProjectSnapshotService projectSnapshotService;

    @Override
    public void processProjectEvent(KafkaEvent event) {
        String eventType = event.eventType();

        log.info("Processing project event: {}", eventType);

        if (PROJECT_SNAPSHOT_EVENTS.contains(eventType)) {
            ProjectSnapshotEvent projectEvent = objectMapper.convertValue(
                    event.payload(),
                    ProjectSnapshotEvent.class
            );

            projectSnapshotService.saveOrUpdate(projectEvent);
            return;
        }

        if (PROJECT_STATUS_EVENTS.contains(eventType)) {
            UUID projectId = extractProjectId(event);

            if (projectId == null) {
                throw new IllegalArgumentException(
                        "Project ID is missing in event: " + eventType
                );
            }

            String status = switch (eventType) {
                case "ProjectArchived" -> "ARCHIVED";
                case "ProjectRestored" -> "ACTIVE";
                case "ProjectCompleted" -> "COMPLETED";
                default -> throw new IllegalArgumentException(
                        "Unsupported project event: " + eventType
                );
            };

            projectSnapshotService.updateStatus(projectId, status);
            return;
        }

        log.warn("Unsupported project event: {}", eventType);
    }

    @Override
    public void processTaskEvent(KafkaEvent event) {
        String eventType = event.eventType();

        if ("TaskDeleted".equals(eventType)) {
            UUID taskId = extractTaskId(event);

            if (taskId != null) {
                taskSnapshotService.delete(taskId);
            }

            return;
        }

        if (!TASK_EVENTS.contains(eventType)) {
            log.warn("Unsupported task event: {}", eventType);
            return;
        }

        TaskSnapshotEvent taskEvent = objectMapper.convertValue(
                event.payload(),
                TaskSnapshotEvent.class
        );

        taskSnapshotService.saveOrUpdate(taskEvent);
    }

    private UUID extractProjectId(KafkaEvent event) {
        String projectId = event.payload().path("projectId").asText(null);

        return projectId == null || projectId.isBlank()
                ? null
                : UUID.fromString(projectId);
    }

    private UUID extractTaskId(KafkaEvent event) {
        String taskId = event.payload().path("taskId").asText(null);

        return taskId == null || taskId.isBlank()
                ? null
                : UUID.fromString(taskId);
    }
}