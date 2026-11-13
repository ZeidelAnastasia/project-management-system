
package com.projectmanagement.task.repository;

import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends
        JpaRepository<Task, UUID>,
        JpaSpecificationExecutor<Task> {

    List<Task> findAllByDueDateBetweenAndAssigneeIdIsNotNullAndStatusNot(
            LocalDate from,
            LocalDate to,
            TaskStatus status
    );
}
