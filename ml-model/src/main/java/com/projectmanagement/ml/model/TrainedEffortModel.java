package com.projectmanagement.ml.model;

import lombok.Getter;
import smile.data.DataFrame;
import smile.regression.RandomForest;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class TrainedEffortModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final RandomForest randomForest;
    private final String[] featureNames;

    public TrainedEffortModel(
            RandomForest randomForest,
            String[] featureNames
    ) {
        this.randomForest = randomForest;
        this.featureNames = featureNames.clone();
    }

    public double predict(double[] features) {
        if (features == null || features.length != featureNames.length) {
            throw new IllegalArgumentException("Invalid feature vector dimensions");
        }

        for (double feature : features) {
            if (!Double.isFinite(feature)) {
                throw new IllegalArgumentException("Feature vector contains an invalid value");
            }
        }

        DataFrame data = DataFrame.of(
                new double[][]{features},
                featureNames
        );

        double transformedPrediction = randomForest.predict(data.get(0));

        return Math.expm1(Math.max(0.0, transformedPrediction));
    }

    public Map<String, Double> getFeatureImportance() {
        double[] importance = randomForest.importance();
        Map<String, Double> result = new LinkedHashMap<>();

        for (int i = 0; i < importance.length; i++) {
            result.put(featureNames[i], importance[i]);
        }

        return result;
    }
}