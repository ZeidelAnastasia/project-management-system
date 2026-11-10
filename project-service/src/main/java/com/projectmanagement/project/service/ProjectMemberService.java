package com.projectmanagement.project.service;

import com.projectmanagement.project.dto.request.AddProjectMemberRequest;
import com.projectmanagement.project.dto.response.ProjectMemberResponse;
import com.projectmanagement.project.entity.enums.ProjectRole;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberService {

    ProjectMemberResponse addMember(
            UUID projectId,
            AddProjectMemberRequest request,
            UUID currentUserId,
            String currentUserRole
    );

    List<ProjectMemberResponse> getMembers(
            UUID projectId,
            UUID currentUserId,
            String currentUserRole
    );

    void removeMember(
            UUID projectId,
            UUID userId,
            UUID reassignToUserId,
            UUID currentUserId,
            String currentUserRole
    );

    ProjectMemberResponse updateMemberRole(
            UUID projectId,
            UUID userId,
            ProjectRole projectRole,
            UUID currentUserId,
            String currentUserRole
    );
}