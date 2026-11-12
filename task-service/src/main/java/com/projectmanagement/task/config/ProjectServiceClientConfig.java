package com.projectmanagement.task.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ProjectServiceClientConfig {

    @Bean
    public RestClient projectServiceRestClient(
            RestClient.Builder builder,
            @Value("${services.project-service.url:http://localhost:8081}") String projectServiceUrl
    ) {
        return builder
                .baseUrl(projectServiceUrl)
                .build();
    }
}