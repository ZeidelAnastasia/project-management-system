package com.projectmanagement.intelligence.analysis.service;

import com.projectmanagement.intelligence.analysis.model.TaskFeatures;
import com.projectmanagement.intelligence.dto.request.TaskAnalysisRequest;
import com.projectmanagement.intelligence.entity.TaskSnapshotEntity;

public interface TaskFeatureExtractor {

    TaskFeatures extract(
            TaskAnalysisRequest request,
            TaskSnapshotEntity task
    );
}