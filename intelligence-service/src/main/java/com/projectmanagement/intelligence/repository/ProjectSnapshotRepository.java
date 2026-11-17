package com.projectmanagement.intelligence.repository;

import com.projectmanagement.intelligence.entity.ProjectSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectSnapshotRepository extends JpaRepository<ProjectSnapshotEntity, UUID> {

    Optional<ProjectSnapshotEntity> findByProjectId(UUID projectId);
}