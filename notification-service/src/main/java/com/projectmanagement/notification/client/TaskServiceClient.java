package com.projectmanagement.notification.client;

import com.projectmanagement.notification.dto.TaskDeadlineReminderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskServiceClient {

    private final RestClient taskServiceRestClient;

    public List<TaskDeadlineReminderResponse> getTasksWithApproachingDeadlines(
            LocalDateTime from,
            LocalDateTime to
    ) {
        return taskServiceRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/internal/tasks/deadline-reminders")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build()
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}