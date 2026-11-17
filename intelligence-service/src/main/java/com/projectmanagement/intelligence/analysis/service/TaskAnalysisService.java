package com.projectmanagement.intelligence.analysis.service;

import com.projectmanagement.intelligence.dto.request.TaskAnalysisRequest;
import com.projectmanagement.intelligence.dto.response.TaskAnalysisResponse;

import java.util.UUID;

public interface TaskAnalysisService {

    TaskAnalysisResponse analyze(UUID taskId, TaskAnalysisRequest request);

    TaskAnalysisResponse getAnalysis(UUID taskId);
}