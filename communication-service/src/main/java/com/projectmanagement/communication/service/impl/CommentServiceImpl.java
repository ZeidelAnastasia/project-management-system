package com.projectmanagement.communication.service.impl;

import com.projectmanagement.communication.dto.request.CommentCreateRequest;
import com.projectmanagement.communication.dto.request.CommentUpdateRequest;
import com.projectmanagement.communication.dto.response.CommentResponse;
import com.projectmanagement.communication.entity.Comment;
import com.projectmanagement.communication.event.CommentEventPublisher;
import com.projectmanagement.communication.exception.ResourceNotFoundException;
import com.projectmanagement.communication.mapper.CommentMapper;
import com.projectmanagement.communication.repository.CommentRepository;
import com.projectmanagement.communication.security.CurrentUser;
import com.projectmanagement.communication.service.CommentAccessService;
import com.projectmanagement.communication.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final CommentAccessService commentAccessService;
    private final CommentEventPublisher commentEventPublisher;
    private final CurrentUser currentUser;

    @Override
    public CommentResponse createComment(UUID taskId, CommentCreateRequest request) {
        var taskAccess = commentAccessService.checkTaskAccess(taskId);

        Comment comment = new Comment();
        comment.setTaskId(taskId);
        comment.setProjectId(taskAccess.projectId());
        comment.setAuthorId(currentUser.getUserId());
        comment.setBody(request.body());

        Comment savedComment = commentRepository.save(comment);

        commentEventPublisher.publishCommentCreated(savedComment);

        return commentMapper.toResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByTaskId(UUID taskId, Pageable pageable) {
        commentAccessService.checkTaskAccess(taskId);

        return commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId, pageable)
                .map(commentMapper::toResponse);
    }

    @Override
    public CommentResponse updateComment(UUID commentId, CommentUpdateRequest request) {
        Comment comment = findCommentById(commentId);

        commentAccessService.checkCommentModification(comment);
        comment.setBody(request.body());

        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(UUID commentId) {
        Comment comment = findCommentById(commentId);

        commentAccessService.checkCommentModification(comment);
        commentRepository.delete(comment);
    }

    private Comment findCommentById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
    }
}