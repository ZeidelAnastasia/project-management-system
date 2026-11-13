package com.projectmanagement.communication.client;

import com.projectmanagement.communication.dto.response.TaskAccessResponse;
import com.projectmanagement.communication.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaskServiceClient {

    private final RestClient taskServiceRestClient;

    public TaskAccessResponse getTaskAccess(
            UUID taskId,
            UUID userId,
            String accessToken
    ) {
        try {
            TaskAccessResponse response = taskServiceRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/tasks/{taskId}/access")
                            .queryParam("userId", userId)
                            .build(taskId))
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .body(TaskAccessResponse.class);

            if (response == null) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Task service returned an empty response"
                );
            }

            return response;

        } catch (HttpClientErrorException.NotFound exception) {
            return new TaskAccessResponse(false, null, null, false, false);
        } catch (ResourceAccessException exception) {
            throw new ServiceUnavailableException(
                    "Task service is currently unavailable",
                    exception
            );
        } catch (RestClientException exception) {
            throw new ServiceUnavailableException(
                    "Unable to communicate with task service",
                    exception
            );
        }
    }
}