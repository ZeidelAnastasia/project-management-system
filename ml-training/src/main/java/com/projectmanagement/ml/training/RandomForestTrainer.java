package com.projectmanagement.ml.training;

import com.projectmanagement.ml.model.ExtractedSample;
import com.projectmanagement.ml.model.ModelTrainingResult;
import com.projectmanagement.ml.model.RegressionMetrics;
import com.projectmanagement.ml.model.TrainedEffortModel;
import lombok.extern.slf4j.Slf4j;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.vector.DoubleVector;
import smile.regression.RandomForest;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

@Slf4j
public class RandomForestTrainer {

    private static final int NUMBER_OF_TREES = 500;
    private static final int MAX_DEPTH = 20;
    private static final int NODE_SIZE = 5;
    private static final double SUBSAMPLE = 1.0;
    private static final int RANDOM_SEED = 42;

    private static final String TARGET_COLUMN = "target";

    public ModelTrainingResult train(
            List<ExtractedSample> trainingSamples,
            List<ExtractedSample> testSamples
    ) {
        validateSamples(trainingSamples, testSamples);

        int featureCount = trainingSamples.getFirst()
                .features().length;

        validateFeatureDimensions(trainingSamples, featureCount);
        validateFeatureDimensions(testSamples, featureCount);

        String[] featureNames = generateFeatureNames(featureCount);

        DataFrame trainingData = createDataFrame(
                trainingSamples,
                featureNames
        );

        int mtry = Math.max(featureCount / 3, 1);

        RandomForest.Options options = new RandomForest.Options(
                NUMBER_OF_TREES,
                mtry,
                MAX_DEPTH,
                0,
                NODE_SIZE,
                SUBSAMPLE,
                generateSeeds(NUMBER_OF_TREES),
                null
        );

        log.info("Starting Random Forest training");
        log.info("Training samples: {}", trainingSamples.size());
        log.info("Features: {}", featureCount);
        log.info("Trees: {}", NUMBER_OF_TREES);

        long startTime = System.currentTimeMillis();

        RandomForest randomForest = RandomForest.fit(
                Formula.lhs(TARGET_COLUMN),
                trainingData,
                options
        );

        long trainingTime = System.currentTimeMillis() - startTime;

        TrainedEffortModel trainedModel = new TrainedEffortModel(
                randomForest,
                featureNames
        );

        RegressionMetrics metrics = evaluate(
                trainedModel,
                testSamples
        );

        Map<String, Double> importance =
                trainedModel.getFeatureImportance();

        log.info(
                "Random Forest training completed in {} ms",
                trainingTime
        );

        log.info("MAE: {}", metrics.mae());
        log.info("RMSE: {}", metrics.rmse());
        log.info("Median absolute error: {}", metrics.medianAbsoluteError());

        return ModelTrainingResult.builder()
                .model(trainedModel)
                .metrics(metrics)
                .featureImportance(importance)
                .trainingSamples(trainingSamples.size())
                .testSamples(testSamples.size())
                .build();
    }

    private DataFrame createDataFrame(
            List<ExtractedSample> samples,
            String[] featureNames
    ) {
        double[][] features = new double[samples.size()][];
        double[] targets = new double[samples.size()];

        for (int i = 0; i < samples.size(); i++) {
            ExtractedSample sample = samples.get(i);

            features[i] = sample.features().clone();
            targets[i] = transformTarget(sample.actualEffort());
        }

        DataFrame dataFrame = DataFrame.of(
                features,
                featureNames
        );

        return dataFrame.add(
                new DoubleVector(TARGET_COLUMN, targets)
        );
    }

    private RegressionMetrics evaluate(
            TrainedEffortModel model,
            List<ExtractedSample> samples
    ) {
        double[] absoluteErrors = new double[samples.size()];
        double squaredErrorSum = 0.0;

        for (int i = 0; i < samples.size(); i++) {
            ExtractedSample sample = samples.get(i);

            double prediction = model.predict(sample.features());
            double actual = sample.actualEffort();

            double error = Math.abs(actual - prediction);

            absoluteErrors[i] = error;
            squaredErrorSum += error * error;
        }

        double mae = Arrays.stream(absoluteErrors)
                .average()
                .orElse(0.0);

        double rmse = Math.sqrt(
                squaredErrorSum / samples.size()
        );

        double medianAbsoluteError = calculateMedian(absoluteErrors);

        return new RegressionMetrics(
                mae,
                rmse,
                medianAbsoluteError
        );
    }

    private double calculateMedian(double[] values) {
        double[] sorted = values.clone();
        Arrays.sort(sorted);

        int middle = sorted.length / 2;

        if (sorted.length % 2 == 0) {
            return (sorted[middle - 1] + sorted[middle]) / 2.0;
        }

        return sorted[middle];
    }

    private double transformTarget(double actualEffort) {
        if (!Double.isFinite(actualEffort) || actualEffort <= 0) {
            throw new IllegalArgumentException(
                    "Actual effort must be positive and finite"
            );
        }

        return Math.log1p(actualEffort);
    }

    private String[] generateFeatureNames(int featureCount) {
        String[] names = new String[featureCount];

        for (int i = 0; i < featureCount; i++) {
            names[i] = "feature_" + (i + 1);
        }

        return names;
    }

    private long[] generateSeeds(int count) {
        return LongStream.range(0, count)
                .map(index -> RANDOM_SEED + index)
                .toArray();
    }

    private void validateSamples(
            List<ExtractedSample> trainingSamples,
            List<ExtractedSample> testSamples
    ) {
        if (trainingSamples == null || trainingSamples.isEmpty()) {
            throw new IllegalArgumentException(
                    "Training dataset must not be empty"
            );
        }

        if (testSamples == null || testSamples.isEmpty()) {
            throw new IllegalArgumentException(
                    "Test dataset must not be empty"
            );
        }
    }

    private void validateFeatureDimensions(
            List<ExtractedSample> samples,
            int expectedDimensions
    ) {
        for (ExtractedSample sample : samples) {
            if (sample.features() == null
                    || sample.features().length != expectedDimensions) {
                throw new IllegalArgumentException(
                        "Invalid feature dimensions for sample: "
                                + sample.id()
                );
            }

            for (double feature : sample.features()) {
                if (!Double.isFinite(feature)) {
                    throw new IllegalArgumentException(
                            "Invalid feature value for sample: "
                                    + sample.id()
                    );
                }
            }

            if (!Double.isFinite(sample.actualEffort())
                    || sample.actualEffort() <= 0) {
                throw new IllegalArgumentException(
                        "Invalid actual effort for sample: "
                                + sample.id()
                );
            }
        }
    }
}