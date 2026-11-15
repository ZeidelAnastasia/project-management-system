package com.projectmanagement.reporting.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportingEventType {

    PROJECT_CREATED("ProjectCreated"),
    PROJECT_COMPLETED("ProjectCompleted"),
    TASK_CREATED("TaskCreated"),
    TASK_UPDATED("TaskUpdated"),
    TASK_STATUS_CHANGED("TaskStatusChanged"),
    TASK_DEADLINE_CHANGED("TaskDeadlineChanged"),
    TASK_EFFORT_UPDATED("TaskEffortUpdated"),
    TASK_DELETED("TaskDeleted");

    private final String value;

    public static ReportingEventType fromValue(String value) {
        for (ReportingEventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown reporting event type: " + value);
    }
}