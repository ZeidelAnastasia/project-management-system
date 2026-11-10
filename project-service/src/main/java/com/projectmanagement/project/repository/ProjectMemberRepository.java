
package com.projectmanagement.project.repository;

import com.projectmanagement.project.entity.ProjectMember;
import com.projectmanagement.project.entity.ProjectMemberId;
import com.projectmanagement.project.entity.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository
        extends JpaRepository<ProjectMember, ProjectMemberId> {

    List<ProjectMember> findAllByProject_Id(UUID projectId);

    List<ProjectMember> findAllByUserId(UUID userId);

    Optional<ProjectMember> findByProject_IdAndUserId(
            UUID projectId,
            UUID userId
    );

    boolean existsByProject_IdAndUserId(UUID projectId, UUID userId);

    boolean existsByProject_IdAndUserIdAndProjectRole(
            UUID projectId,
            UUID userId,
            ProjectRole projectRole
    );

    long countByProject_Id(UUID projectId);

    void deleteByProject_IdAndUserId(UUID projectId, UUID userId);
}
