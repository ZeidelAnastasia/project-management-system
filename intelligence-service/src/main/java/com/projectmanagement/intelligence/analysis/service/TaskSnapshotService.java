package com.projectmanagement.intelligence.analysis.service;

import com.projectmanagement.intelligence.kafka.event.TaskSnapshotEvent;

import java.util.UUID;

public interface TaskSnapshotService {

    void saveOrUpdate(TaskSnapshotEvent event);

    void delete(UUID taskId);
}