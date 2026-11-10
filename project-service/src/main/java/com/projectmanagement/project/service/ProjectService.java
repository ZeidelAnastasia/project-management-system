package com.projectmanagement.project.service;

import com.projectmanagement.project.dto.request.CompleteProjectRequest;
import com.projectmanagement.project.dto.request.CreateProjectRequest;
import com.projectmanagement.project.dto.request.UpdateProjectRequest;
import com.projectmanagement.project.dto.response.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectResponse createProject(
            CreateProjectRequest request,
            UUID currentUserId,
            String currentUserRole
    );

    ProjectResponse getProject(
            UUID projectId,
            UUID currentUserId,
            String currentUserRole
    );

    List<ProjectResponse> getProjects(
            UUID currentUserId,
            String currentUserRole
    );

    ProjectResponse updateProject(
            UUID projectId,
            UpdateProjectRequest request,
            UUID currentUserId,
            String currentUserRole
    );

    ProjectResponse archiveProject(
            UUID projectId,
            UUID currentUserId,
            String currentUserRole
    );

    ProjectResponse restoreProject(
            UUID projectId,
            UUID currentUserId,
            String currentUserRole
    );

    ProjectResponse completeProject(
            UUID projectId,
            CompleteProjectRequest request,
            UUID currentUserId,
            String currentUserRole
    );
}