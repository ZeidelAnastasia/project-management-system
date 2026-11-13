package com.projectmanagement.communication.event;

import com.projectmanagement.communication.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommentEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishCommentCreated(Comment comment) {
        CommentCreatedEvent event = new CommentCreatedEvent(
                UUID.randomUUID(),
                "CommentCreated",
                Instant.now(),
                comment.getId(),
                comment.getTaskId(),
                comment.getProjectId(),
                comment.getAuthorId(),
                comment.getBody()
        );

        applicationEventPublisher.publishEvent(event);
    }
}