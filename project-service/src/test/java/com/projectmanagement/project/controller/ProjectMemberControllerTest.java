package com.projectmanagement.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.project.dto.request.AddProjectMemberRequest;
import com.projectmanagement.project.dto.response.ProjectMemberResponse;
import com.projectmanagement.project.entity.enums.ProjectRole;
import com.projectmanagement.project.security.CurrentUser;
import com.projectmanagement.project.security.CurrentUserProvider;
import com.projectmanagement.project.service.ProjectMemberService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectMemberService projectMemberService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    private UUID projectId;
    private UUID currentUserId;
    private String currentUserRole;
    private UUID targetUserId;
    private ProjectMemberResponse projectMemberResponse;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();
        currentUserRole = "PROJECT_MANAGER";
        targetUserId = UUID.randomUUID();

        CurrentUser currentUser = new CurrentUser(currentUserId, currentUserRole);
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        projectMemberResponse = new ProjectMemberResponse(
                targetUserId,
                ProjectRole.MEMBER,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /api/projects/{projectId}/members - Should add member and return 201 Created")
    void addMember_Success() throws Exception {
        AddProjectMemberRequest request = new AddProjectMemberRequest(targetUserId, ProjectRole.MEMBER);

        when(projectMemberService.addMember(eq(projectId), any(AddProjectMemberRequest.class), eq(currentUserId), eq(currentUserRole)))
                .thenReturn(projectMemberResponse);

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is(targetUserId.toString())))
                .andExpect(jsonPath("$.projectRole", is("MEMBER")));

        verify(projectMemberService).addMember(eq(projectId), any(AddProjectMemberRequest.class), eq(currentUserId), eq(currentUserRole));
    }

    @Test
    @DisplayName("GET /api/projects/{projectId}/members - Should return members list and 200 OK")
    void getMembers_Success() throws Exception {
        when(projectMemberService.getMembers(projectId, currentUserId, currentUserRole))
                .thenReturn(List.of(projectMemberResponse));

        mockMvc.perform(get("/api/projects/{projectId}/members", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(targetUserId.toString())))
                .andExpect(jsonPath("$[0].projectRole", is("MEMBER")));

        verify(projectMemberService).getMembers(projectId, currentUserId, currentUserRole);
    }

    @Test
    @DisplayName("DELETE /api/projects/{projectId}/members/{userId} - Should remove member without reassignment and return 204 No Content")
    void removeMember_WithoutReassign_Success() throws Exception {
        doNothing().when(projectMemberService).removeMember(projectId, targetUserId, null, currentUserId, currentUserRole);

        mockMvc.perform(delete("/api/projects/{projectId}/members/{userId}", projectId, targetUserId))
                .andExpect(status().isNoContent());

        verify(projectMemberService).removeMember(projectId, targetUserId, null, currentUserId, currentUserRole);
    }

    @Test
    @DisplayName("DELETE /api/projects/{projectId}/members/{userId} - Should remove member with reassignment and return 204 No Content")
    void removeMember_WithReassign_Success() throws Exception {
        UUID reassignToUserId = UUID.randomUUID();

        doNothing().when(projectMemberService).removeMember(projectId, targetUserId, reassignToUserId, currentUserId, currentUserRole);

        mockMvc.perform(delete("/api/projects/{projectId}/members/{userId}", projectId, targetUserId)
                        .param("reassignToUserId", reassignToUserId.toString()))
                .andExpect(status().isNoContent());

        verify(projectMemberService).removeMember(projectId, targetUserId, reassignToUserId, currentUserId, currentUserRole);
    }

    @Test
    @DisplayName("PATCH /api/projects/{projectId}/members/{userId}/role - Should update member role and return 200 OK")
    void updateMemberRole_Success() throws Exception {
        ProjectRole newRole = ProjectRole.MANAGER;
        ProjectMemberResponse updatedResponse = new ProjectMemberResponse(
                targetUserId,
                newRole,
                LocalDateTime.now()
        );

        when(projectMemberService.updateMemberRole(projectId, targetUserId, newRole, currentUserId, currentUserRole))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/projects/{projectId}/members/{userId}/role", projectId, targetUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRole)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is(targetUserId.toString())))
                .andExpect(jsonPath("$.projectRole", is("MANAGER")));

        verify(projectMemberService).updateMemberRole(projectId, targetUserId, newRole, currentUserId, currentUserRole);
    }
}