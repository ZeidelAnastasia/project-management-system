package com.projectmanagement.project.client;

import com.projectmanagement.project.client.dto.TaskResponse;
import com.projectmanagement.project.client.dto.UpdateTaskAssigneeRequest;
import com.projectmanagement.project.exception.TaskServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaskServiceClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${app.services.task-service.url}")
    private String taskServiceUrl;

    private RestClient getRestClient() {
        return restClientBuilder
                .baseUrl(taskServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    var authentication = SecurityContextHolder
                            .getContext()
                            .getAuthentication();

                    if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
                        request.getHeaders().setBearerAuth(
                                jwtAuthentication.getToken().getTokenValue()
                        );
                    }

                    return execution.execute(request, body);
                })
                .build();
    }

    public List<TaskResponse> getProjectTasks(UUID projectId) {
        try {
            List<TaskResponse> tasks = getRestClient()
                    .get()
                    .uri("/api/projects/{projectId}/tasks", projectId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            return Objects.requireNonNullElse(tasks, List.of());
        } catch (RestClientException exception) {
            throw new TaskServiceException("Failed to retrieve tasks for project " + projectId);
        }
    }

    public List<TaskResponse> getTasksAssignedToUser(
            UUID projectId,
            UUID userId
    ) {
        return getProjectTasks(projectId)
                .stream()
                .filter(task -> userId.equals(task.assigneeId()))
                .toList();
    }

    public void updateTaskAssignee(
            UUID taskId,
            UUID assigneeId
    ) {
        try {
            getRestClient()
                    .patch()
                    .uri("/api/tasks/{taskId}/assignee", taskId)
                    .body(new UpdateTaskAssigneeRequest(assigneeId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new TaskServiceException("Failed to update assignee for task " + taskId);
        }
    }
}