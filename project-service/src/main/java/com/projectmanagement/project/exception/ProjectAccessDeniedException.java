package com.projectmanagement.project.exception;

public class ProjectAccessDeniedException extends RuntimeException {
    public ProjectAccessDeniedException(String message) {
        super(message);
    }
}
