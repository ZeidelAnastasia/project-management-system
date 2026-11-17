
package com.projectmanagement.intelligence.config;

import com.projectmanagement.ml.model.ModelBundle;
import com.projectmanagement.ml.storage.ModelBundleStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class MlModelConfiguration {

    @Bean
    public ModelBundleStore modelBundleStore() {
        return new ModelBundleStore();
    }

    @Bean
    public ModelBundle modelBundle(
            ModelBundleStore modelBundleStore,
            @Value("${app.ml.model-path:./models/effort-model.bundle}")
            String modelPath
    ) {
        Path path = Paths.get(modelPath).toAbsolutePath().normalize();

        ModelBundle bundle = modelBundleStore.load(path);

        if (bundle.getEffortModel() == null
                || bundle.getFeatureExtractor() == null) {
            throw new IllegalStateException(
                    "Model bundle does not contain a trained model or feature extractor"
            );
        }

        log.info(
                "ML model initialized. Version: {}, algorithm: {}, training samples: {}",
                bundle.getModelVersion(),
                bundle.getAlgorithm(),
                bundle.getTrainingSamples()
        );

        return bundle;
    }
}
