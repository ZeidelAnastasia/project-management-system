package com.projectmanagement.task.service;

import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.entity.enums.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

public interface TaskEventPublisher {

    void publishTaskCreated(Task task);

    void publishTaskUpdated(Task task);

    void publishTaskAssigned(Task task, UUID oldAssigneeId, UUID newAssigneeId);

    void publishTaskStatusChanged(Task task, TaskStatus oldStatus, TaskStatus newStatus);

    void publishTaskDeadlineChanged(Task task, LocalDate oldDeadline, LocalDate newDeadline);

    void publishTaskEffortUpdated(Task task, Integer oldEffort, Integer newEffort);

    void publishTaskDeleted(Task task);
}