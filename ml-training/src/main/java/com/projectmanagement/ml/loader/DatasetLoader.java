package com.projectmanagement.ml.loader;

import com.projectmanagement.ml.model.TrainingSample;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class DatasetLoader {

    private static final String TABLE_NAME = "Case";

    private final Path databasePath;

    public List<TrainingSample> loadAll() {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + databasePath.toAbsolutePath()
        )) {
            validateColumns(connection);

            String sql = """
                    SELECT
                        id,
                        corpus,
                        num_comment,
                        num_activities,
                        expert_estimated_effort,
                        actual_effort,
                        reference
                    FROM "Case"
                    WHERE actual_effort IS NOT NULL
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                List<TrainingSample> samples = new ArrayList<>();

                while (resultSet.next()) {
                    samples.add(new TrainingSample(
                            resultSet.getString("id"),
                            resultSet.getString("corpus"),
                            getNullableInteger(resultSet, "num_comment"),
                            getNullableInteger(resultSet, "num_activities"),
                            getNullableDouble(
                                    resultSet,
                                    "expert_estimated_effort"
                            ),
                            getNullableDouble(resultSet, "actual_effort"),
                            resultSet.getString("reference")
                    ));
                }

                return samples;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to load training dataset",
                    exception
            );
        }
    }

    private void validateColumns(Connection connection)
            throws SQLException {

        Set<String> columns = new HashSet<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "PRAGMA table_info(\"" + TABLE_NAME + "\")"
             )) {

            while (resultSet.next()) {
                columns.add(resultSet.getString("name"));
            }
        }

        Set<String> requiredColumns = Set.of(
                "id",
                "corpus",
                "num_comment",
                "num_activities",
                "actual_effort"
        );

        if (!columns.containsAll(requiredColumns)) {
            Set<String> missingColumns = new HashSet<>(requiredColumns);
            missingColumns.removeAll(columns);

            throw new IllegalStateException(
                    "Dataset is missing required columns: " + missingColumns
            );
        }
    }

    private Integer getNullableInteger(
            ResultSet resultSet,
            String column
    ) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    private Double getNullableDouble(
            ResultSet resultSet,
            String column
    ) throws SQLException {
        double value = resultSet.getDouble(column);
        return resultSet.wasNull() ? null : value;
    }
}