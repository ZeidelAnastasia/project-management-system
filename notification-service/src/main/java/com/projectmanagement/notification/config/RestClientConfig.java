package com.projectmanagement.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient taskServiceRestClient(
            @Value("${app.services.task-service.url}") String taskServiceUrl,
            @Value("${app.services.task-service.token}") String serviceToken
    ) {
        return RestClient.builder()
                .baseUrl(taskServiceUrl)
                .defaultHeader("X-Internal-Service-Token", serviceToken)
                .build();
    }
}