package com.projectmanagement.intelligence.security.model;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        Set<String> roles
) {
}