package com.projectmanagement.task.client;

import java.util.UUID;

public interface ProjectAccessClient {

    boolean isProjectManager(UUID projectId, UUID userId);

    boolean isProjectMember(UUID projectId, UUID userId);
}