package com.projectmanagement.communication.service;

import com.projectmanagement.communication.dto.request.CommentCreateRequest;
import com.projectmanagement.communication.dto.request.CommentUpdateRequest;
import com.projectmanagement.communication.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CommentService {

    CommentResponse createComment(UUID taskId, CommentCreateRequest request);

    Page<CommentResponse> getCommentsByTaskId(UUID taskId, Pageable pageable);

    CommentResponse updateComment(UUID commentId, CommentUpdateRequest request);

    void deleteComment(UUID commentId);
}