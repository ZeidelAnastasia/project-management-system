package com.projectmanagement.task.client;

import com.projectmanagement.task.client.dto.ProjectAccessResponse;
import com.projectmanagement.task.exception.ProjectNotFoundException;
import com.projectmanagement.task.exception.ProjectServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectAccessClientImpl implements ProjectAccessClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${app.services.project-service.url}")
    private String projectServiceUrl;

    @Override
    public boolean isProjectManager(UUID projectId, UUID userId) {
        return getProjectAccess(projectId, userId).projectManager();
    }

    @Override
    public boolean isProjectMember(UUID projectId, UUID userId) {
        return getProjectAccess(projectId, userId).projectMember();
    }

    private ProjectAccessResponse getProjectAccess(UUID projectId, UUID userId) {
        try {
            ProjectAccessResponse response = getRestClient()
                    .get()
                    .uri("/internal/projects/{projectId}/access?userId={userId}", projectId, userId)
                    .retrieve()
                    .body(ProjectAccessResponse.class);

            if (response == null || !response.projectExists()) {
                throw new ProjectNotFoundException("Project not found");
            }

            return response;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ProjectNotFoundException("Project not found");
        } catch (RestClientException exception) {
            throw new ProjectServiceUnavailableException("Project service unavailable");
        }
    }

    private RestClient getRestClient() {
        return restClientBuilder
                .baseUrl(projectServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    var authentication = SecurityContextHolder
                            .getContext()
                            .getAuthentication();

                    if (authentication != null
                            && authentication.getDetails() instanceof String token) {
                        request.getHeaders().setBearerAuth(token);
                    }

                    return execution.execute(request, body);
                })
                .build();
    }
}