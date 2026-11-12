package com.projectmanagement.task.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskEventType {

    TASK_CREATED("TaskCreated"),
    TASK_UPDATED("TaskUpdated"),
    TASK_ASSIGNED("TaskAssigned"),
    TASK_STATUS_CHANGED("TaskStatusChanged"),
    TASK_DEADLINE_CHANGED("TaskDeadlineChanged"),
    TASK_EFFORT_UPDATED("TaskEffortUpdated"),
    TASK_DELETED("TaskDeleted");

    private final String value;
}