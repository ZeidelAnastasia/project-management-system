package com.projectmanagement.intelligence.repository;

import com.projectmanagement.intelligence.entity.TaskAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskAnalysisRepository extends JpaRepository<TaskAnalysisEntity, UUID> {

    Optional<TaskAnalysisEntity> findByTaskId(UUID taskId);

    List<TaskAnalysisEntity> findAllByTaskId(UUID taskId);

    boolean existsByTaskId(UUID taskId);

    void deleteAllByTaskId(UUID taskId);
}