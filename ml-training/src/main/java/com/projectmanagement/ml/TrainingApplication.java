package com.projectmanagement.ml;

import com.projectmanagement.ml.loader.DatasetLoader;
import com.projectmanagement.ml.model.DatasetSplit;
import com.projectmanagement.ml.model.ExtractedSample;
import com.projectmanagement.ml.model.ModelBundle;
import com.projectmanagement.ml.model.ModelTrainingResult;
import com.projectmanagement.ml.model.PreparedSample;
import com.projectmanagement.ml.preprocessing.DataPreprocessor;
import com.projectmanagement.ml.preprocessing.FeatureExtractor;
import com.projectmanagement.ml.split.ProjectBasedSplitter;
import com.projectmanagement.ml.storage.ModelBundleStore;
import com.projectmanagement.ml.training.RandomForestTrainer;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Slf4j
public class TrainingApplication {

    private static final Path DATASET_PATH =
            Path.of("dataset", "JOSSE_18092020.sqlite3");

    private static final Path MODEL_PATH =
            Path.of("models", "effort-model.bundle");

    private static final double TRAINING_RATIO = 0.8;

    private static final long RANDOM_SEED = 42L;

    private static final String MODEL_VERSION = "effort-rf-1.0";

    private static final String TARGET_NAME = "actual_effort";

    private static final String TARGET_UNIT = "seconds";

    public static void main(String[] args) {
        System.out.println(
                org.slf4j.LoggerFactory.getILoggerFactory().getClass().getName()
        );
        TrainingApplication application = new TrainingApplication();
        application.train();
    }

    public void train() {
        log.info("Starting effort prediction model training");

        System.out.println("Dataset path: " + DATASET_PATH.toAbsolutePath());

        DatasetLoader datasetLoader = new DatasetLoader(DATASET_PATH);
        DataPreprocessor dataPreprocessor = new DataPreprocessor();
        ProjectBasedSplitter projectBasedSplitter =
                new ProjectBasedSplitter();
        FeatureExtractor featureExtractor = new FeatureExtractor();
        RandomForestTrainer randomForestTrainer =
                new RandomForestTrainer();
        ModelBundleStore modelBundleStore = new ModelBundleStore();

        List<PreparedSample> preparedSamples =
                dataPreprocessor.preprocess(datasetLoader.loadAll());

        if (preparedSamples.isEmpty()) {
            throw new IllegalStateException(
                    "Dataset does not contain valid training samples"
            );
        }

        log.info("Prepared samples: {}", preparedSamples.size());

        DatasetSplit datasetSplit = projectBasedSplitter.split(
                preparedSamples,
                TRAINING_RATIO,
                RANDOM_SEED
        );

        log.info(
                "Training samples: {}",
                datasetSplit.trainingSamples().size()
        );

        log.info(
                "Test samples: {}",
                datasetSplit.testSamples().size()
        );

        log.info(
                "Projects in dataset: {}",
                datasetSplit.projectDistribution().size()
        );

        List<ExtractedSample> trainingSamples =
                featureExtractor.fitTransform(
                        datasetSplit.trainingSamples()
                );

        List<ExtractedSample> testSamples =
                featureExtractor.transform(
                        datasetSplit.testSamples()
                );

        ModelTrainingResult trainingResult =
                randomForestTrainer.train(
                        trainingSamples,
                        testSamples
                );

        log.info("Model training completed");
        log.info("MAE: {}", trainingResult.metrics().mae());
        log.info("RMSE: {}", trainingResult.metrics().rmse());
        log.info(
                "Median absolute error: {}",
                trainingResult.metrics().medianAbsoluteError()
        );

        log.info("Feature importance:");

        trainingResult.featureImportance()
                .entrySet()
                .stream()
                .sorted(
                        (first, second) ->
                                Double.compare(
                                        second.getValue(),
                                        first.getValue()
                                )
                )
                .limit(20)
                .forEach(entry ->
                        log.info(
                                "{}: {}",
                                entry.getKey(),
                                entry.getValue()
                        )
                );

        ModelBundle modelBundle = ModelBundle.builder()
                .modelVersion(MODEL_VERSION)
                .trainedAt(Instant.now())
                .algorithm("RandomForest")
                .targetName(TARGET_NAME)
                .targetUnit(TARGET_UNIT)
                .trainingSamples(trainingResult.trainingSamples())
                .testSamples(trainingResult.testSamples())
                .metrics(trainingResult.metrics())
                .effortModel(trainingResult.model())
                .featureExtractor(featureExtractor)
                .build();

        modelBundleStore.save(modelBundle, MODEL_PATH);

        ModelBundle loadedBundle = modelBundleStore.load(MODEL_PATH);

        log.info(
                "Saved model bundle version: {}",
                loadedBundle.getModelVersion()
        );

        log.info(
                "Saved model feature count: {}",
                loadedBundle.getEffortModel()
                        .getFeatureNames()
                        .length
        );

        log.info(
                "Model bundle successfully saved to {}",
                MODEL_PATH.toAbsolutePath()
        );
    }
}