package com.projectmanagement.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.project.dto.request.CompleteProjectRequest;
import com.projectmanagement.project.dto.request.CreateProjectRequest;
import com.projectmanagement.project.dto.request.UpdateProjectRequest;
import com.projectmanagement.project.dto.response.ProjectResponse;
import com.projectmanagement.project.entity.enums.ProjectStatus;
import com.projectmanagement.project.security.CurrentUser;
import com.projectmanagement.project.security.CurrentUserProvider;
import com.projectmanagement.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false) // Отключаем фильтры Spring Security, так как аутентификация замокана через CurrentUserProvider
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    private UUID currentUserId;
    private String currentUserRole;
    private UUID projectId;
    private ProjectResponse projectResponse;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        currentUserRole = "PROJECT_MANAGER";
        projectId = UUID.randomUUID();

        CurrentUser currentUser = new CurrentUser(currentUserId, currentUserRole);
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        projectResponse = new ProjectResponse(
                projectId,
                "Test Project",
                "Project Description",
                ProjectStatus.ACTIVE,
                currentUserId,
                LocalDateTime.now(),
                null,
                null,
                null
        );
    }

    @Test
    @DisplayName("POST /api/projects - Should create project and return 201 Created")
    void createProject_Success() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest("Test Project", "Project Description");

        when(projectService.createProject(any(CreateProjectRequest.class), eq(currentUserId), eq(currentUserRole)))
                .thenReturn(projectResponse);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(projectId.toString())))
                .andExpect(jsonPath("$.name", is("Test Project")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(projectService).createProject(any(CreateProjectRequest.class), eq(currentUserId), eq(currentUserRole));
    }

    @Test
    @DisplayName("GET /api/projects/{projectId} - Should return project and 200 OK")
    void getProject_Success() throws Exception {
        when(projectService.getProject(projectId, currentUserId, currentUserRole))
                .thenReturn(projectResponse);

        mockMvc.perform(get("/api/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(projectId.toString())))
                .andExpect(jsonPath("$.name", is("Test Project")));

        verify(projectService).getProject(projectId, currentUserId, currentUserRole);
    }

    @Test
    @DisplayName("GET /api/projects - Should return list of projects and 200 OK")
    void getProjects_Success() throws Exception {
        when(projectService.getProjects(currentUserId, currentUserRole))
                .thenReturn(List.of(projectResponse));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(projectId.toString())));

        verify(projectService).getProjects(currentUserId, currentUserRole);
    }

    @Test
    @DisplayName("PUT /api/projects/{projectId} - Should update project and return 200 OK")
    void updateProject_Success() throws Exception {
        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", "Updated Description");

        ProjectResponse updatedResponse = new ProjectResponse(
                projectId,
                "Updated Name",
                "Updated Description",
                ProjectStatus.ACTIVE,
                currentUserId,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );

        when(projectService.updateProject(eq(projectId), any(UpdateProjectRequest.class), eq(currentUserId), eq(currentUserRole)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(projectId.toString())))
                .andExpect(jsonPath("$.name", is("Updated Name")));

        verify(projectService).updateProject(eq(projectId), any(UpdateProjectRequest.class), eq(currentUserId), eq(currentUserRole));
    }

    @Test
    @DisplayName("PATCH /api/projects/{projectId}/archive - Should archive project and return 200 OK")
    void archiveProject_Success() throws Exception {
        ProjectResponse archivedResponse = new ProjectResponse(
                projectId,
                "Test Project",
                "Project Description",
                ProjectStatus.ARCHIVED,
                currentUserId,
                LocalDateTime.now(),
                null,
                LocalDateTime.now(),
                null
        );

        when(projectService.archiveProject(projectId, currentUserId, currentUserRole))
                .thenReturn(archivedResponse);

        mockMvc.perform(patch("/api/projects/{projectId}/archive", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(projectId.toString())))
                .andExpect(jsonPath("$.status", is("ARCHIVED")))
                .andExpect(jsonPath("$.archivedAt").isNotEmpty());

        verify(projectService).archiveProject(projectId, currentUserId, currentUserRole);
    }

    @Test
    @DisplayName("PATCH /api/projects/{projectId}/restore - Should restore project and return 200 OK")
    void restoreProject_Success() throws Exception {
        when(projectService.restoreProject(projectId, currentUserId, currentUserRole))
                .thenReturn(projectResponse);

        mockMvc.perform(patch("/api/projects/{projectId}/restore", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(projectId.toString())))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(projectService).restoreProject(projectId, currentUserId, currentUserRole);
    }

    @Test
    @DisplayName("PATCH /api/projects/{projectId}/complete - Should complete project and return 200 OK")
    void completeProject_Success() throws Exception {
        CompleteProjectRequest request = new CompleteProjectRequest(true);

        ProjectResponse completedResponse = new ProjectResponse(
                projectId,
                "Test Project",
                "Project Description",
                ProjectStatus.COMPLETED,
                currentUserId,
                LocalDateTime.now(),
                null,
                null,
                LocalDateTime.now()
        );

        when(projectService.completeProject(eq(projectId), any(CompleteProjectRequest.class), eq(currentUserId), eq(currentUserRole)))
                .thenReturn(completedResponse);

        mockMvc.perform(patch("/api/projects/{projectId}/complete", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(projectId.toString())))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        verify(projectService).completeProject(eq(projectId), any(CompleteProjectRequest.class), eq(currentUserId), eq(currentUserRole));
    }
}