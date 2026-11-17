package com.projectmanagement.intelligence.mapper;

import com.projectmanagement.intelligence.entity.TaskSnapshotEntity;
import com.projectmanagement.intelligence.kafka.event.TaskSnapshotEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class TaskSnapshotMapper {

    public TaskSnapshotEntity toEntity(TaskSnapshotEvent event) {
        return TaskSnapshotEntity.builder()
                .taskId(event.taskId())
                .projectId(event.projectId())
                .assigneeId(event.assigneeId())
                .title(event.title())
                .description(event.description())
                .priority(event.priority())
                .status(event.status())
                .dueDate(event.dueDate())
                .estimatedEffort(event.estimatedEffort())
                .actualEffort(event.actualEffort())
                .labels(event.labels() == null ? new ArrayList<>() : event.labels())
                .statusChangeCount(defaultValue(event.statusChangeCount()))
                .deadlineChangeCount(defaultValue(event.deadlineChangeCount()))
                .reassignmentCount(defaultValue(event.reassignmentCount()))
                .createdAt(event.createdAt())
                .updatedAt(event.updatedAt())
                .completedAt(event.completedAt())
                .build();
    }

    public void updateEntity(TaskSnapshotEntity entity, TaskSnapshotEvent event) {
        entity.setProjectId(event.projectId());
        entity.setAssigneeId(event.assigneeId());
        entity.setTitle(event.title());
        entity.setDescription(event.description());
        entity.setPriority(event.priority());
        entity.setStatus(event.status());
        entity.setDueDate(event.dueDate());
        entity.setEstimatedEffort(event.estimatedEffort());
        entity.setActualEffort(event.actualEffort());
        entity.setLabels(event.labels() == null ? new ArrayList<>() : event.labels());
        entity.setStatusChangeCount(defaultValue(event.statusChangeCount()));
        entity.setDeadlineChangeCount(defaultValue(event.deadlineChangeCount()));
        entity.setReassignmentCount(defaultValue(event.reassignmentCount()));
        entity.setUpdatedAt(event.updatedAt());
        entity.setCompletedAt(event.completedAt());
    }

    private Integer defaultValue(Integer value) {
        return value == null ? 0 : value;
    }
}