package com.projectmanagement.communication.service;

import com.projectmanagement.communication.client.TaskServiceClient;
import com.projectmanagement.communication.dto.response.TaskAccessResponse;
import com.projectmanagement.communication.entity.Comment;
import com.projectmanagement.communication.exception.ResourceNotFoundException;
import com.projectmanagement.communication.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentAccessService {

    private final TaskServiceClient taskServiceClient;
    private final CurrentUser currentUser;

    public TaskAccessResponse checkTaskAccess(UUID taskId) {
        UUID userId = currentUser.getUserId();

        TaskAccessResponse access = taskServiceClient.getTaskAccess(
                taskId,
                userId,
                currentUser.getAccessToken()
        );

        if (!access.taskExists()) {
            throw new ResourceNotFoundException("Task not found");
        }

        if (currentUser.hasRole("ADMIN")) {
            return access;
        }

        if (currentUser.hasRole("PROJECT_MANAGER")
                && access.projectManager()) {
            return access;
        }

        if (currentUser.hasRole("EMPLOYEE")
                && access.projectMember()
                && userId.equals(access.assigneeId())) {
            return access;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You do not have access to this task"
        );
    }

    public void checkCommentModification(Comment comment) {
        checkTaskAccess(comment.getTaskId());

        UUID userId = currentUser.getUserId();

        if (currentUser.hasRole("ADMIN")
                || userId.equals(comment.getAuthorId())) {
            return;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You can modify only your own comments"
        );
    }
}