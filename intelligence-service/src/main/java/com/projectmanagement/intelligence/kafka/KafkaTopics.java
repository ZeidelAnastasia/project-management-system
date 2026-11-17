package com.projectmanagement.intelligence.kafka;

public final class KafkaTopics {

    public static final String PROJECT_CREATED = "${app.kafka.topics.project-created}";
    public static final String PROJECT_UPDATED = "${app.kafka.topics.project-updated}";
    public static final String PROJECT_ARCHIVED = "${app.kafka.topics.project-archived}";
    public static final String PROJECT_RESTORED = "${app.kafka.topics.project-restored}";
    public static final String PROJECT_COMPLETED = "${app.kafka.topics.project-completed}";

    public static final String TASK_CREATED = "${app.kafka.topics.task-created}";
    public static final String TASK_UPDATED = "${app.kafka.topics.task-updated}";
    public static final String TASK_ASSIGNED = "${app.kafka.topics.task-assigned}";
    public static final String TASK_STATUS_CHANGED = "${app.kafka.topics.task-status-changed}";
    public static final String TASK_DEADLINE_CHANGED = "${app.kafka.topics.task-deadline-changed}";
    public static final String TASK_EFFORT_UPDATED = "${app.kafka.topics.task-effort-updated}";
    public static final String TASK_DELETED = "${app.kafka.topics.task-deleted}";

    private KafkaTopics() {
    }
}