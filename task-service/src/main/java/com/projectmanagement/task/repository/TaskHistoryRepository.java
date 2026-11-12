package com.projectmanagement.task.repository;

import com.projectmanagement.task.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, UUID> {

    List<TaskHistory> findAllByTaskIdOrderByChangedAtDesc(UUID taskId);
}