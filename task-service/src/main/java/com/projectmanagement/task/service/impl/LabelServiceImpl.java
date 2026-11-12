package com.projectmanagement.task.service.impl;

import com.projectmanagement.task.dto.request.CreateLabelRequest;
import com.projectmanagement.task.dto.request.UpdateLabelRequest;
import com.projectmanagement.task.dto.response.LabelResponse;
import com.projectmanagement.task.entity.Label;
import com.projectmanagement.task.exception.LabelNotFoundException;
import com.projectmanagement.task.exception.TaskNotFoundException;
import com.projectmanagement.task.mapper.TaskMapper;
import com.projectmanagement.task.repository.LabelRepository;
import com.projectmanagement.task.security.CurrentUserService;
import com.projectmanagement.task.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public LabelResponse createLabel(CreateLabelRequest request) {
        checkAdminAccess();

        Label label = new Label();
        label.setName(request.name());
        label.setColor(request.color());

        return taskMapper.toLabelResponse(labelRepository.save(label));
    }

    @Override
    public List<LabelResponse> getAllLabels() {
        return labelRepository.findAll()
                .stream()
                .map(taskMapper::toLabelResponse)
                .toList();
    }

    @Override
    @Transactional
    public LabelResponse updateLabel(UUID labelId, UpdateLabelRequest request) {
        checkAdminAccess();

        Label label = findLabel(labelId);
        label.setName(request.name());
        label.setColor(request.color());

        return taskMapper.toLabelResponse(labelRepository.save(label));
    }

    @Override
    @Transactional
    public void deleteLabel(UUID labelId) {
        checkAdminAccess();

        Label label = findLabel(labelId);
        labelRepository.delete(label);
    }

    private Label findLabel(UUID labelId) {
        return labelRepository.findById(labelId)
            .orElseThrow(() -> new LabelNotFoundException("Задача с id " + labelId + " не найдена"));
    }

    private void checkAdminAccess() {
        if (!currentUserService.isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }
    }
}