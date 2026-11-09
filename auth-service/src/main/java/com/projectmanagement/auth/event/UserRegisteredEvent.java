package com.projectmanagement.auth.event;

import com.projectmanagement.auth.kafka.event.UserRegisteredPayload;

public record UserRegisteredEvent(
        UserRegisteredPayload payload
) {
}
