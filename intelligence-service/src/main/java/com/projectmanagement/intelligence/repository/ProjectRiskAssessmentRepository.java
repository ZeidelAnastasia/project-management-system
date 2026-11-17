package com.projectmanagement.intelligence.repository;

import com.projectmanagement.intelligence.entity.ProjectRiskAssessmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRiskAssessmentRepository
        extends JpaRepository<ProjectRiskAssessmentEntity, UUID> {

    Optional<ProjectRiskAssessmentEntity> findFirstByProjectIdOrderByAssessedAtDesc(
            UUID projectId
    );

    List<ProjectRiskAssessmentEntity> findAllByProjectIdOrderByAssessedAtDesc(
            UUID projectId
    );

    boolean existsByProjectId(UUID projectId);

    void deleteAllByProjectId(UUID projectId);
}