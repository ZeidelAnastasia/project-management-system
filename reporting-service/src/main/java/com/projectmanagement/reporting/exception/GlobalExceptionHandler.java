package com.projectmanagement.reporting.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.net.URI;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(
            ResourceNotFoundException exception
    ) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidReportParameterException.class)
    public ProblemDetail handleInvalidReportParameter(
            InvalidReportParameterException exception
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        String detail = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Request validation failed");

        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                detail
        );
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            HandlerMethodValidationException.class
    })
    public ProblemDetail handleConstraintViolation(Exception exception) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Request parameters are invalid"
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        log.error("Data integrity violation", exception);

        return createProblemDetail(
                HttpStatus.CONFLICT,
                "The operation violates data integrity constraints"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception) {
        log.error("Unexpected error occurred", exception);

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