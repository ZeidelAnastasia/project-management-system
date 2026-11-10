package com.projectmanagement.project.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic projectCreatedTopic(
            @Value("${app.kafka.topics.project-created}") String topic
    ) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic projectUpdatedTopic(
            @Value("${app.kafka.topics.project-updated}") String topic
    ) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic projectArchivedTopic(
            @Value("${app.kafka.topics.project-archived}") String topic
    ) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic projectRestoredTopic(
            @Value("${app.kafka.topics.project-restored}") String topic
    ) {
        return createTopic(topic);
    }

    @Bean
    public NewTopic projectCompletedTopic(
            @Value("${app.kafka.topics.project-completed}") String topic
    ) {
        return createTopic(topic);
    }

    private NewTopic createTopic(String topic) {
        return TopicBuilder.name(topic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}