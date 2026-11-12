
package com.projectmanagement.task.security;

import java.util.List;
import java.util.UUID;

public record JwtUser(
        UUID userId,
        List<String> roles
) {
}
