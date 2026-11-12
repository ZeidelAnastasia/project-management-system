
package com.projectmanagement.task.mapper;

import com.projectmanagement.task.dto.request.CreateTaskRequest;
import com.projectmanagement.task.dto.request.UpdateTaskRequest;
import com.projectmanagement.task.dto.response.LabelResponse;
import com.projectmanagement.task.dto.response.TaskDetailsResponse;
import com.projectmanagement.task.dto.response.TaskHistoryResponse;
import com.projectmanagement.task.dto.response.TaskResponse;
import com.projectmanagement.task.entity.Label;
import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.entity.TaskHistory;
import com.projectmanagement.task.entity.TaskLabel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    public Task toEntity(
            CreateTaskRequest request,
            UUID projectId,
            UUID createdBy
    ) {
        Task task = new Task();

        task.setProjectId(projectId);
        task.setCreatedBy(createdBy);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setAssigneeId(request.assigneeId());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setEstimatedEffort(request.estimatedEffort());

        return task;
    }

    public void updateEntity(
            Task task,
            UpdateTaskRequest request
    ) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
    }

    public TaskResponse toResponse(Task task) {
        Set<LabelResponse> labels = task.getTaskLabels()
                .stream()
                .map(TaskLabel::getLabel)
                .map(this::toLabelResponse)
                .collect(Collectors.toSet());

        return new TaskResponse(
                task.getId(),
                task.getProjectId(),
                task.getAssigneeId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getEstimatedEffort(),
                task.getActualEffort(),
                labels,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    public LabelResponse toLabelResponse(Label label) {
        return new LabelResponse(
                label.getId(),
                label.getName(),
                label.getColor()
        );
    }

    public TaskHistoryResponse toHistoryResponse(TaskHistory history) {
        return new TaskHistoryResponse(
                history.getId(),
                history.getChangedBy(),
                history.getChangeType(),
                history.getOldValue(),
                history.getNewValue(),
                history.getChangedAt()
        );
    }

    public List<TaskHistoryResponse> toHistoryResponseList(
            List<TaskHistory> history
    ) {
        return history.stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    public TaskDetailsResponse toDetailsResponse(
            Task task,
            List<TaskHistory> history
    ) {
        return new TaskDetailsResponse(
                toResponse(task),
                task.getCreatedBy(),
                toHistoryResponseList(history)
        );
    }
}
