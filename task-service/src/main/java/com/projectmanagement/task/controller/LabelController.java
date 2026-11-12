package com.projectmanagement.task.controller;

import com.projectmanagement.task.api.LabelApi;
import com.projectmanagement.task.dto.request.CreateLabelRequest;
import com.projectmanagement.task.dto.request.UpdateLabelRequest;
import com.projectmanagement.task.dto.response.LabelResponse;
import com.projectmanagement.task.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LabelController implements LabelApi {

    private final LabelService labelService;

    @Override
    public LabelResponse createLabel(CreateLabelRequest request) {
        return labelService.createLabel(request);
    }

    @Override
    public List<LabelResponse> getAllLabels() {
        return labelService.getAllLabels();
    }

    @Override
    public LabelResponse updateLabel(UUID labelId, UpdateLabelRequest request) {
        return labelService.updateLabel(labelId, request);
    }

    @Override
    public void deleteLabel(UUID labelId) {
        labelService.deleteLabel(labelId);
    }
}