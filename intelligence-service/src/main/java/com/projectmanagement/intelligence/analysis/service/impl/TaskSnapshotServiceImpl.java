package com.projectmanagement.intelligence.analysis.service.impl;

import com.projectmanagement.intelligence.analysis.service.TaskSnapshotService;
import com.projectmanagement.intelligence.entity.TaskSnapshotEntity;
import com.projectmanagement.intelligence.kafka.event.TaskSnapshotEvent;
import com.projectmanagement.intelligence.mapper.TaskSnapshotMapper;
import com.projectmanagement.intelligence.repository.TaskSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskSnapshotServiceImpl implements TaskSnapshotService {

    private final TaskSnapshotRepository taskSnapshotRepository;
    private final TaskSnapshotMapper taskSnapshotMapper;

    @Override
    @Transactional
    public void saveOrUpdate(TaskSnapshotEvent event) {
        if (event.taskId() == null || event.projectId() == null) {
            throw new IllegalArgumentException("Task ID and project ID are required");
        }

        TaskSnapshotEntity entity = taskSnapshotRepository.findByTaskId(event.taskId())
                .orElseGet(() -> taskSnapshotMapper.toEntity(event));

        if (entity.getId() != null) {
            taskSnapshotMapper.updateEntity(entity, event);
        }

        taskSnapshotRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(UUID taskId) {
        taskSnapshotRepository.deleteByTaskId(taskId);
    }
}