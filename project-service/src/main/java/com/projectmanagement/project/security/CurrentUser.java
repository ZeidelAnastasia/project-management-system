package com.projectmanagement.project.security;

import java.util.UUID;

public record CurrentUser(
        UUID id,
        String role
) {
}