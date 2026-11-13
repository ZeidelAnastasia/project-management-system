package com.projectmanagement.communication.controller;

import com.projectmanagement.communication.controller.api.CommentApi;
import com.projectmanagement.communication.dto.request.CommentCreateRequest;
import com.projectmanagement.communication.dto.request.CommentUpdateRequest;
import com.projectmanagement.communication.dto.response.CommentResponse;
import com.projectmanagement.communication.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentService commentService;

    @Override
    public ResponseEntity<CommentResponse> createComment(
            UUID taskId,
            CommentCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentService.createComment(taskId, request));
    }

    @Override
    public ResponseEntity<Page<CommentResponse>> getCommentsByTaskId(
            UUID taskId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                commentService.getCommentsByTaskId(taskId, pageable)
        );
    }

    @Override
    public ResponseEntity<CommentResponse> updateComment(
            UUID id,
            CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(
                commentService.updateComment(id, request)
        );
    }

    @Override
    public ResponseEntity<Void> deleteComment(UUID id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}