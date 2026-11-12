package com.projectmanagement.task.event;

import com.projectmanagement.task.config.KafkaTopicProperties;
import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.entity.TaskLabel;
import com.projectmanagement.task.entity.enums.TaskStatus;
import com.projectmanagement.task.service.TaskEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KafkaTaskEventPublisher implements TaskEventPublisher {

    private final KafkaTemplate<String, TaskEvent<?>> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    @Override
    public void publishTaskCreated(Task task) {
        TaskCreatedPayload payload = new TaskCreatedPayload(
                task.getId(),
                task.getProjectId(),
                task.getCreatedBy(),
                task.getAssigneeId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getEstimatedEffort(),
                task.getActualEffort(),
                getLabelIds(task)
        );

        publish(topicProperties.getTaskCreated(), task.getId(),
                createEvent(TaskEventType.TASK_CREATED, payload));
    }

    @Override
    public void publishTaskUpdated(Task task) {
        TaskUpdatedPayload payload = new TaskUpdatedPayload(
                task.getId(),
                task.getProjectId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getDueDate(),
                task.getEstimatedEffort(),
                task.getActualEffort(),
                getLabelIds(task)
        );

        publish(topicProperties.getTaskUpdated(), task.getId(),
                createEvent(TaskEventType.TASK_UPDATED, payload));
    }

    @Override
    public void publishTaskAssigned(Task task, UUID oldAssigneeId, UUID newAssigneeId) {
        TaskAssignedPayload payload = new TaskAssignedPayload(
                task.getId(),
                task.getProjectId(),
                oldAssigneeId,
                newAssigneeId
        );

        publish(topicProperties.getTaskAssigned(), task.getId(),
                createEvent(TaskEventType.TASK_ASSIGNED, payload));
    }

    @Override
    public void publishTaskStatusChanged(
            Task task,
            TaskStatus oldStatus,
            TaskStatus newStatus
    ) {
        TaskStatusChangedPayload payload = new TaskStatusChangedPayload(
                task.getId(),
                task.getProjectId(),
                task.getAssigneeId(),
                oldStatus,
                newStatus
        );

        publish(topicProperties.getTaskStatusChanged(), task.getId(),
                createEvent(TaskEventType.TASK_STATUS_CHANGED, payload));
    }

    @Override
    public void publishTaskDeadlineChanged(
            Task task,
            LocalDate oldDeadline,
            LocalDate newDeadline
    ) {
        TaskDeadlineChangedPayload payload = new TaskDeadlineChangedPayload(
                task.getId(),
                task.getProjectId(),
                task.getAssigneeId(),
                oldDeadline,
                newDeadline
        );

        publish(topicProperties.getTaskDeadlineChanged(), task.getId(),
                createEvent(TaskEventType.TASK_DEADLINE_CHANGED, payload));
    }

    @Override
    public void publishTaskEffortUpdated(
            Task task,
            Integer oldEffort,
            Integer newEffort
    ) {
        TaskEffortUpdatedPayload payload = new TaskEffortUpdatedPayload(
                task.getId(),
                task.getProjectId(),
                task.getAssigneeId(),
                oldEffort,
                newEffort
        );

        publish(topicProperties.getTaskEffortUpdated(), task.getId(),
                createEvent(TaskEventType.TASK_EFFORT_UPDATED, payload));
    }

    @Override
    public void publishTaskDeleted(Task task) {
        TaskDeletedPayload payload = new TaskDeletedPayload(
                task.getId(),
                task.getProjectId(),
                task.getAssigneeId()
        );

        publish(topicProperties.getTaskDeleted(), task.getId(),
                createEvent(TaskEventType.TASK_DELETED, payload));
    }

    private <T> TaskEvent<T> createEvent(TaskEventType eventType, T payload) {
        return new TaskEvent<>(
                UUID.randomUUID(),
                eventType.getValue(),
                Instant.now(),
                payload
        );
    }

    private void publish(String topic, UUID taskId, TaskEvent<?> event) {
        kafkaTemplate.send(topic, taskId.toString(), event);
    }

    private Set<UUID> getLabelIds(Task task) {
        return task.getTaskLabels().stream()
                .map(TaskLabel::getLabel)
                .map(label -> label.getId())
                .collect(Collectors.toSet());
    }
}
