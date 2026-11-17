package com.projectmanagement.intelligence.analysis.service;

public interface MlEffortPredictionService {

    double predict(double[] features);
}