package com.projectmanagement.project.client;

import com.projectmanagement.project.client.dto.TaskResponse;
import com.projectmanagement.project.client.dto.UpdateTaskAssigneeRequest;
import com.projectmanagement.project.exception.TaskServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.security.core.context.SecurityContextHolder;

class TaskServiceClientTest {

    private static final String BASE_URL = "http://localhost:8082";

    private final UUID projectId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();
    private final UUID assigneeId = UUID.randomUUID();

    private MockRestServiceServer mockServer;
    private TaskServiceClient taskServiceClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        mockServer = MockRestServiceServer.bindTo(builder).build();

        taskServiceClient = new TaskServiceClient(builder);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGetProjectTasks() {
        mockServer.expect(requestTo(BASE_URL + "/api/projects/" + projectId + "/tasks"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        [
                            {
                                "id": "%s",
                                "projectId": "%s",
                                "assigneeId": "%s",
                                "title": "Implement authentication",
                                "status": "IN_PROGRESS"
                            }
                        ]
                        """.formatted(taskId, projectId, assigneeId), MediaType.APPLICATION_JSON));

        List<TaskResponse> result = taskServiceClient.getProjectTasks(projectId);

        assertEquals(1, result.size());
        assertEquals(taskId, result.get(0).id());
        assertEquals(projectId, result.get(0).projectId());
        assertEquals(assigneeId, result.get(0).assigneeId());
        assertEquals("Implement authentication", result.get(0).title());
        assertEquals("IN_PROGRESS", result.get(0).status());

        mockServer.verify();
    }

    @Test
    void shouldUpdateTaskAssignee() {
        mockServer.expect(requestTo(BASE_URL + "/api/tasks/" + taskId + "/assignee"))
                .andExpect(method(PATCH))
                .andExpect(jsonPath("$.assigneeId").value(assigneeId.toString()))
                .andRespond(withSuccess());

        taskServiceClient.updateTaskAssignee(taskId, assigneeId);

        mockServer.verify();
    }

    @Test
    void shouldForwardJwtToken() {
        String tokenValue = "test-access-token";

        Jwt jwt = Jwt.withTokenValue(tokenValue)
                .header("alg", "HS256")
                .subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_PROJECT_MANAGER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockServer.expect(requestTo(BASE_URL + "/api/projects/" + projectId + "/tasks"))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        taskServiceClient.getProjectTasks(projectId);

        mockServer.verify();
    }

    @Test
    void shouldThrowExceptionWhenTaskServiceUnavailable() {
        mockServer.expect(requestTo(BASE_URL + "/api/projects/" + projectId + "/tasks"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        assertThrows(
                TaskServiceException.class,
                () -> taskServiceClient.getProjectTasks(projectId)
        );

        mockServer.verify();
    }
}