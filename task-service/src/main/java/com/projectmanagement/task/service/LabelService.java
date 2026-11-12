package com.projectmanagement.task.service;

import com.projectmanagement.task.dto.request.CreateLabelRequest;
import com.projectmanagement.task.dto.request.UpdateLabelRequest;
import com.projectmanagement.task.dto.response.LabelResponse;

import java.util.List;
import java.util.UUID;

public interface LabelService {

    LabelResponse createLabel(CreateLabelRequest request);

    List<LabelResponse> getAllLabels();

    LabelResponse updateLabel(UUID labelId, UpdateLabelRequest request);

    void deleteLabel(UUID labelId);
}