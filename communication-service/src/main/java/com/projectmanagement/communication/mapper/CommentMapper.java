package com.projectmanagement.communication.mapper;

import com.projectmanagement.communication.dto.response.CommentResponse;
import com.projectmanagement.communication.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTaskId(),
                comment.getProjectId(),
                comment.getAuthorId(),
                comment.getBody(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}