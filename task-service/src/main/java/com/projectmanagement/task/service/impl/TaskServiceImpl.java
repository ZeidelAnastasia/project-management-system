package com.projectmanagement.task.service.impl;

import com.projectmanagement.task.dto.request.CreateTaskRequest;
import com.projectmanagement.task.dto.request.TaskFilterRequest;
import com.projectmanagement.task.dto.request.UpdateTaskRequest;
import com.projectmanagement.task.dto.response.TaskResponse;
import com.projectmanagement.task.entity.Label;
import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.entity.TaskLabel;
import com.projectmanagement.task.entity.TaskLabelId;
import com.projectmanagement.task.exception.AssigneeNotProjectMemberException;
import com.projectmanagement.task.exception.LabelNotFoundException;
import com.projectmanagement.task.exception.TaskNotFoundException;
import com.projectmanagement.task.mapper.TaskMapper;
import com.projectmanagement.task.query.TaskQueryBuilder;
import com.projectmanagement.task.repository.LabelRepository;
import com.projectmanagement.task.repository.TaskRepository;
import com.projectmanagement.task.security.CurrentUserService;
import com.projectmanagement.task.client.ProjectAccessClient;
import com.projectmanagement.task.security.TaskAuthorizationService;
import com.projectmanagement.task.service.TaskEventPublisher;
import com.projectmanagement.task.service.TaskService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "dueDate",
            "priority",
            "status",
            "title"
    );

    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserService currentUserService;
    private final TaskAuthorizationService taskAuthorizationService;
    private final ProjectAccessClient projectAccessClient;
    private final TaskEventPublisher taskEventPublisher;

    @Override
    @Transactional
    public TaskResponse createTask(UUID projectId, CreateTaskRequest request) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        taskAuthorizationService.checkCanAccessProject(projectId);
        checkCanCreateTask(projectId, currentUserId);

        Task task = taskMapper.toEntity(request, projectId, currentUserId);

        if (request.assigneeId() != null) {
            checkAssigneeMembership(projectId, request.assigneeId());
        }

        taskRepository.save(task);
        updateLabels(task, request.labelIds());

        Task savedTask = taskRepository.save(task);
        taskEventPublisher.publishTaskCreated(savedTask);

        return taskMapper.toResponse(savedTask);
    }

    @Override
    public TaskResponse getTask(UUID taskId) {
        Task task = findTaskById(taskId);

        taskAuthorizationService.checkCanViewTask(task);

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request) {
        Task task = findTaskById(taskId);

        taskAuthorizationService.checkCanManageTask(task);

        taskMapper.updateEntity(task, request);
        updateLabels(task, request.labelIds());

        Task updatedTask = taskRepository.save(task);
        taskEventPublisher.publishTaskUpdated(updatedTask);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(UUID taskId) {
        Task task = findTaskById(taskId);

        taskAuthorizationService.checkCanManageTask(task);

        taskEventPublisher.publishTaskDeleted(task);
        taskRepository.delete(task);
    }

    @Override
    public Page<TaskResponse> getProjectTasks(
            UUID projectId,
            TaskFilterRequest filter
    ) {
        taskAuthorizationService.checkCanAccessProject(projectId);

        UUID assigneeId = getEffectiveAssigneeId(filter.assigneeId());
        Pageable pageable = createPageable(filter);

        Specification<Task> specification = createSpecification(
                projectId,
                filter,
                assigneeId
        );

        return taskRepository.findAll(specification, pageable)
                .map(taskMapper::toResponse);
    }

    @Override
    public Page<TaskResponse> searchTasks(TaskFilterRequest filter) {
        UUID projectId = null;

        if (!currentUserService.isAdmin()
                && !currentUserService.hasRole("EMPLOYEE")) {
            throw new AccessDeniedException(
                    "Only administrators and employees can search tasks without project ID"
            );
        }

        UUID assigneeId = getEffectiveAssigneeId(filter.assigneeId());
        Pageable pageable = createPageable(filter);

        Specification<Task> specification = createSpecification(
                projectId,
                filter,
                assigneeId
        );

        return taskRepository.findAll(specification, pageable)
                .map(taskMapper::toResponse);
    }

    private Task findTaskById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача с id " + taskId + " не найдена"));
    }

    private void checkCanCreateTask(UUID projectId, UUID userId) {
        if (currentUserService.isAdmin()) {
            return;
        }

        if (!currentUserService.hasRole("PROJECT_MANAGER")
                || !projectAccessClient.isProjectManager(projectId, userId)) {
            throw new AccessDeniedException(
                    "Only project managers and administrators can create tasks"
            );
        }
    }

    private void checkAssigneeMembership(UUID projectId, UUID assigneeId) {
        if (!projectAccessClient.isProjectMember(projectId, assigneeId)) {
            throw new AssigneeNotProjectMemberException("Assignee not project member");
        }
    }

    private void updateLabels(Task task, Set<UUID> labelIds) {
        task.getTaskLabels().clear();

        if (labelIds == null || labelIds.isEmpty()) {
            return;
        }

        List<Label> labels = labelRepository.findAllById(labelIds);

        if (labels.size() != labelIds.size()) {
            throw new LabelNotFoundException("Label not found");
        }

        for (Label label : labels) {
            TaskLabel taskLabel = new TaskLabel();
            taskLabel.setId(new TaskLabelId(task.getId(), label.getId()));
            taskLabel.setTask(task);
            taskLabel.setLabel(label);

            task.getTaskLabels().add(taskLabel);
        }
    }

    private UUID getEffectiveAssigneeId(UUID requestedAssigneeId) {
        if (currentUserService.hasRole("EMPLOYEE")) {
            return currentUserService.getCurrentUserId();
        }

        return requestedAssigneeId;
    }

    private Specification<Task> createSpecification(
            UUID projectId,
            TaskFilterRequest filter,
            UUID assigneeId
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = TaskQueryBuilder.buildTaskFilterPredicates(
                    criteriaBuilder,
                    root,
                    query,
                    projectId,
                    filter.status(),
                    filter.priority(),
                    assigneeId,
                    filter.dueDateFrom(),
                    filter.dueDateTo(),
                    filter.labelId()
            );

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable createPageable(TaskFilterRequest filter) {
        int page = filter.page() == null
                ? DEFAULT_PAGE
                : Math.max(filter.page(), DEFAULT_PAGE);

        int size = filter.size() == null
                ? DEFAULT_SIZE
                : Math.min(Math.max(filter.size(), 1), MAX_PAGE_SIZE);

        String sortBy = filter.sortBy();

        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "createdAt";
        }

        Sort.Direction direction = Sort.Direction.DESC;

        if (filter.sortDirection() != null) {
            direction = Sort.Direction.fromOptionalString(filter.sortDirection())
                    .orElse(Sort.Direction.DESC);
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, sortBy)
        );
    }
}