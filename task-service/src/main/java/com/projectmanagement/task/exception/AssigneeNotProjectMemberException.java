package com.projectmanagement.task.exception;

public class AssigneeNotProjectMemberException extends RuntimeException {
    public AssigneeNotProjectMemberException(String message) {
        super(message);
    }
}
