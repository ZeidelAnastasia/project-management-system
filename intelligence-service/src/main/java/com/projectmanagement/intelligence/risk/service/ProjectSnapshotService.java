package com.projectmanagement.intelligence.risk.service;

import com.projectmanagement.intelligence.kafka.event.ProjectSnapshotEvent;

import java.util.UUID;

public interface ProjectSnapshotService {

    void saveOrUpdate(ProjectSnapshotEvent event);

    void updateStatus(UUID projectId, String status);
}