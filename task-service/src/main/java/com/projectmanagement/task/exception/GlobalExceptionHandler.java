package com.projectmanagement.task.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail handleTaskNotFound(TaskNotFoundException exception) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Task not found"
        );
    }

    @ExceptionHandler(LabelNotFoundException.class)
    public ProblemDetail handleLabelNotFound(LabelNotFoundException exception) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Label not found"
        );
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ProblemDetail handleProjectNotFound(ProjectNotFoundException exception) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Project not found"
        );
    }

    @ExceptionHandler(AssigneeNotProjectMemberException.class)
    public ProblemDetail handleAssigneeNotProjectMember(
            AssigneeNotProjectMemberException exception
    ) {
        return createProblemDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "The assigned user is not a member of the project"
        );
    }

    @ExceptionHandler(InvalidTaskStatusTransitionException.class)
    public ProblemDetail handleInvalidTaskStatusTransition(
            InvalidTaskStatusTransitionException exception
    ) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "The requested task status transition is not allowed"
        );
    }

    @ExceptionHandler(ProjectServiceUnavailableException.class)
    public ProblemDetail handleProjectServiceUnavailable(
            ProjectServiceUnavailableException exception
    ) {
        return createProblemDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Project service is temporarily unavailable"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() == null
                                ? "Invalid value"
                                : error.getDefaultMessage(),
                        (first, second) -> first
                ));

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed"
        );

        problemDetail.setProperty("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Request parameters validation failed"
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableMessage(
            HttpMessageNotReadableException exception
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or has an invalid format"
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(
            AuthenticationException exception
    ) {
        return createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(
            AccessDeniedException exception
    ) {
        return createProblemDetail(
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this operation"
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "The operation violates data integrity constraints"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception) {
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    private ProblemDetail createProblemDetail(
            HttpStatus status,
            String detail
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                status,
                detail
        );

        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(status.getReasonPhrase());

        return problemDetail;
    }
}