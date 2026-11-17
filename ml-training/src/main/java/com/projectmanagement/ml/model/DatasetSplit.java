package com.projectmanagement.ml.model;

import java.util.List;
import java.util.Map;

public record DatasetSplit(
        List<PreparedSample> trainingSamples,
        List<PreparedSample> testSamples,
        Map<String, Integer> projectDistribution
) {
}