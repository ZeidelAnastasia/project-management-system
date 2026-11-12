package com.projectmanagement.task.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.kafka.topics")
public class KafkaTopicProperties {

    private String taskCreated;
    private String taskUpdated;
    private String taskAssigned;
    private String taskStatusChanged;
    private String taskDeadlineChanged;
    private String taskEffortUpdated;
    private String taskDeleted;
}