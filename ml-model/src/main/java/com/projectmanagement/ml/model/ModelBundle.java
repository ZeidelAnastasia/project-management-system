package com.projectmanagement.ml.model;

import com.projectmanagement.ml.preprocessing.FeatureExtractor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Builder
public class ModelBundle implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String modelVersion;
    private final Instant trainedAt;
    private final String algorithm;
    private final String targetName;
    private final String targetUnit;
    private final int trainingSamples;
    private final int testSamples;
    private final RegressionMetrics metrics;
    private final TrainedEffortModel effortModel;
    private final FeatureExtractor featureExtractor;
}