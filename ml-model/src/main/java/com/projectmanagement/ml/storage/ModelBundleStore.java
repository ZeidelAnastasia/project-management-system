package com.projectmanagement.ml.storage;

import com.projectmanagement.ml.model.ModelBundle;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
public class ModelBundleStore {

    public void save(ModelBundle bundle, Path destination) {
        if (bundle == null) {
            throw new IllegalArgumentException("Model bundle must not be null");
        }

        Path absolutePath = destination.toAbsolutePath();
        Path parent = absolutePath.getParent();

        try {
            Files.createDirectories(parent);

            Path temporaryFile = Files.createTempFile(
                    parent,
                    "model-bundle-",
                    ".tmp"
            );

            try {
                try (ObjectOutputStream output = new ObjectOutputStream(
                        Files.newOutputStream(temporaryFile)
                )) {
                    output.writeObject(bundle);
                    output.flush();
                }

                try {
                    Files.move(
                            temporaryFile,
                            absolutePath,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE
                    );
                } catch (AtomicMoveNotSupportedException exception) {
                    Files.move(
                            temporaryFile,
                            absolutePath,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            } finally {
                Files.deleteIfExists(temporaryFile);
            }

            log.info("Model bundle saved to {}", absolutePath);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to save model bundle",
                    exception
            );
        }
    }

    public ModelBundle load(Path source) {
        if (!Files.exists(source)) {
            throw new IllegalStateException(
                    "Model bundle does not exist: " + source
            );
        }

        try (ObjectInputStream input = new ObjectInputStream(
                Files.newInputStream(source)
        )) {
            Object object = input.readObject();

            if (!(object instanceof ModelBundle bundle)) {
                throw new IllegalStateException(
                        "File does not contain a valid ModelBundle"
                );
            }

            log.info(
                    "Model bundle loaded. Version: {}",
                    bundle.getModelVersion()
            );

            return bundle;
        } catch (IOException | ClassNotFoundException exception) {
            throw new IllegalStateException(
                    "Failed to load model bundle",
                    exception
            );
        }
    }
}