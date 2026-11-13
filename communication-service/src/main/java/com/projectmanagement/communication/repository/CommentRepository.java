package com.projectmanagement.communication.repository;

import com.projectmanagement.communication.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findAllByTaskIdOrderByCreatedAtAsc(UUID taskId, Pageable pageable);

    Optional<Comment> findByIdAndTaskId(UUID id, UUID taskId);

    Optional<Comment> findByIdAndAuthorId(UUID id, UUID authorId);

    boolean existsByTaskId(UUID taskId);

    void deleteByIdAndTaskId(UUID id, UUID taskId);
}