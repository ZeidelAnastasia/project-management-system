package com.projectmanagement.ml.model;

import java.io.Serializable;

public record RegressionMetrics(
        double mae,
        double rmse,
        double medianAbsoluteError
) implements Serializable {
}