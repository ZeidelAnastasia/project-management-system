package com.projectmanagement.project.exception;

public class ProjectMemberNotFoundException extends RuntimeException {
    public ProjectMemberNotFoundException(String message) {
        super(message);
    }
}
