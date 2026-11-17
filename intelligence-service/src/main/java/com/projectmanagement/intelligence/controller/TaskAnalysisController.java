package com.projectmanagement.intelligence.controller.impl;

import com.projectmanagement.intelligence.analysis.service.TaskAnalysisService;
import com.projectmanagement.intelligence.controller.api.TaskAnalysisApi;
import com.projectmanagement.intelligence.dto.request.TaskAnalysisRequest;
import com.projectmanagement.intelligence.dto.response.TaskAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskAnalysisController implements TaskAnalysisApi {

    private final TaskAnalysisService taskAnalysisService;

    @Override
    public ResponseEntity<TaskAnalysisResponse> getAnalysis(UUID taskId) {
        return ResponseEntity.ok(taskAnalysisService.getAnalysis(taskId));
    }

    @Override
    public ResponseEntity<TaskAnalysisResponse> analyze(
            UUID taskId,
            TaskAnalysisRequest request
    ) {
        return ResponseEntity.ok(taskAnalysisService.analyze(taskId, request));
    }
}