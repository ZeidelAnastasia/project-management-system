package com.projectmanagement.intelligence.analysis.service.impl;

import com.projectmanagement.intelligence.analysis.service.MlEffortPredictionService;
import com.projectmanagement.ml.model.ModelBundle;
import com.projectmanagement.ml.model.TrainedEffortModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MlEffortPredictionServiceImpl implements MlEffortPredictionService {

    private final ModelBundle modelBundle;

    @Override
    public double predict(double[] features) {
        if (features == null || features.length == 0) {
            throw new IllegalArgumentException("Features must not be empty");
        }

        TrainedEffortModel model = modelBundle.getEffortModel();
        double predictedEffort = model.predict(features);

        if (!Double.isFinite(predictedEffort) || predictedEffort < 0) {
            throw new IllegalStateException("ML model returned an invalid prediction");
        }

        return predictedEffort;
    }
}