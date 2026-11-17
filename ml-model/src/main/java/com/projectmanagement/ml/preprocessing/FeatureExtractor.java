package com.projectmanagement.ml.preprocessing;

import com.projectmanagement.ml.model.ExtractedSample;
import com.projectmanagement.ml.model.PreparedSample;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
public class FeatureExtractor implements Serializable {

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("[\\p{L}\\p{N}]+");

    private static final int MIN_DOCUMENT_FREQUENCY = 3;
    private static final int MAX_VOCABULARY_SIZE = 5000;
    private static final int SVD_COMPONENTS = 100;
    private static final int OVERSAMPLING = 10;
    private static final int POWER_ITERATIONS = 2;

    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, Integer> vocabulary = new HashMap<>();
    private double[] idf;
    private double[][] svdComponents;

    private double[] numericMeans;
    private double[] numericStandardDeviations;

    private boolean fitted;

    public List<ExtractedSample> fitTransform(
            List<PreparedSample> samples
    ) {
        validateSamples(samples);

        List<List<String>> tokenizedDocuments = samples.stream()
                .map(sample -> tokenize(sample.cleanedText()))
                .toList();

        buildVocabulary(tokenizedDocuments);

        List<Map<Integer, Double>> tfidfVectors =
                buildTfidfVectors(tokenizedDocuments);

        fitSvd(tfidfVectors);

        List<double[]> numericFeatures = samples.stream()
                .map(this::extractNumericFeatures)
                .toList();

        fitNumericScaling(numericFeatures);

        fitted = true;

        return transform(samples);
    }

    public List<ExtractedSample> transform(
            List<PreparedSample> samples
    ) {
        if (!fitted || svdComponents == null) {
            throw new IllegalStateException(
                    "FeatureExtractor has not been fitted"
            );
        }

        List<ExtractedSample> result = new ArrayList<>();

        for (PreparedSample sample : samples) {
            List<String> tokens = tokenize(sample.cleanedText());

            Map<Integer, Double> tfidfVector =
                    buildTfidfVector(tokens);

            double[] textFeatures = projectText(tfidfVector);
            double[] numericFeatures =
                    normalizeNumericFeatures(
                            extractNumericFeatures(sample)
                    );

            double[] features = new double[
                    textFeatures.length + numericFeatures.length
                    ];

            System.arraycopy(
                    textFeatures,
                    0,
                    features,
                    0,
                    textFeatures.length
            );

            System.arraycopy(
                    numericFeatures,
                    0,
                    features,
                    textFeatures.length,
                    numericFeatures.length
            );

            result.add(new ExtractedSample(
                    sample.id(),
                    features,
                    sample.actualEffort()
            ));
        }

        return result;
    }

    private void validateSamples(List<PreparedSample> samples) {
        if (samples == null || samples.size() < 2) {
            throw new IllegalArgumentException(
                    "At least two samples are required"
            );
        }
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();

        Matcher matcher = TOKEN_PATTERN.matcher(
                text.toLowerCase(Locale.ROOT)
        );

        while (matcher.find()) {
            String token = matcher.group();

            if (token.length() >= 2) {
                tokens.add(token);
            }
        }

        return tokens;
    }

    private void buildVocabulary(
            List<List<String>> documents
    ) {
        Map<String, Integer> documentFrequency = new HashMap<>();

        for (List<String> document : documents) {
            Set<String> uniqueTokens = new HashSet<>(document);

            for (String token : uniqueTokens) {
                documentFrequency.merge(token, 1, Integer::sum);
            }
        }

        List<String> terms = documentFrequency.entrySet()
                .stream()
                .filter(entry ->
                        entry.getValue() >= MIN_DOCUMENT_FREQUENCY
                )
                .sorted(
                        Comparator.<Map.Entry<String, Integer>>
                                        comparingInt(Map.Entry::getValue)
                                .reversed()
                )
                .limit(MAX_VOCABULARY_SIZE)
                .map(Map.Entry::getKey)
                .toList();

        vocabulary = new HashMap<>();

        for (int i = 0; i < terms.size(); i++) {
            vocabulary.put(terms.get(i), i);
        }

        if (vocabulary.isEmpty()) {
            throw new IllegalStateException(
                    "Vocabulary is empty"
            );
        }

        idf = new double[vocabulary.size()];

        for (Map.Entry<String, Integer> entry : vocabulary.entrySet()) {
            idf[entry.getValue()] = Math.log(
                    (double) documents.size()
                            / (1 + documentFrequency.get(entry.getKey()))
            ) + 1.0;
        }

        log.info("Vocabulary size: {}", vocabulary.size());
    }

    private List<Map<Integer, Double>> buildTfidfVectors(
            List<List<String>> documents
    ) {
        return documents.stream()
                .map(this::buildTfidfVector)
                .toList();
    }

    private Map<Integer, Double> buildTfidfVector(
            List<String> tokens
    ) {
        Map<Integer, Integer> termFrequencies = new HashMap<>();

        for (String token : tokens) {
            Integer index = vocabulary.get(token);

            if (index != null) {
                termFrequencies.merge(index, 1, Integer::sum);
            }
        }

        Map<Integer, Double> vector = new HashMap<>();

        double norm = 0.0;

        for (Map.Entry<Integer, Integer> entry
                : termFrequencies.entrySet()) {

            double value = (1.0 + Math.log(entry.getValue()))
                    * idf[entry.getKey()];

            vector.put(entry.getKey(), value);
            norm += value * value;
        }

        norm = Math.sqrt(norm);

        if (norm > 0.0) {
            double finalNorm = norm;

            vector.replaceAll(
                    (index, value) -> value / finalNorm
            );
        }

        return vector;
    }

    private void fitSvd(
            List<Map<Integer, Double>> matrix
    ) {
        int rows = matrix.size();
        int columns = vocabulary.size();

        int components = Math.min(
                SVD_COMPONENTS,
                Math.min(rows - 1, columns)
        );

        if (components < 1) {
            throw new IllegalStateException(
                    "Not enough data for SVD"
            );
        }

        int rank = Math.min(
                components + OVERSAMPLING,
                Math.min(rows, columns)
        );

        double[][] omega = randomMatrix(columns, rank);
        double[][] q = orthonormalize(multiply(matrix, omega));

        for (int iteration = 0;
             iteration < POWER_ITERATIONS;
             iteration++) {

            double[][] z = orthonormalize(
                    transposeMultiply(matrix, q)
            );

            q = orthonormalize(multiply(matrix, z));
        }

        double[][] b = multiplyTransposeLeft(q, matrix);

        double[][] gram = multiplyByTranspose(b);

        EigenDecomposition eigen = decomposeSymmetric(gram);

        svdComponents = new double[columns][components];

        for (int component = 0;
             component < components;
             component++) {

            int eigenIndex = eigen.order()[component];
            double singularValue = Math.sqrt(
                    Math.max(0.0, eigen.values()[eigenIndex])
            );

            if (singularValue < 1.0E-10) {
                continue;
            }

            for (int term = 0; term < columns; term++) {
                double value = 0.0;

                for (int j = 0; j < rank; j++) {
                    value += b[j][term]
                            * eigen.vectors()[j][eigenIndex];
                }

                svdComponents[term][component] =
                        value / singularValue;
            }
        }

        log.info(
                "SVD fitted. Input dimensions: {}, output dimensions: {}",
                columns,
                components
        );
    }

    private double[] projectText(
            Map<Integer, Double> vector
    ) {
        double[] result = new double[svdComponents[0].length];

        for (Map.Entry<Integer, Double> entry : vector.entrySet()) {
            int termIndex = entry.getKey();
            double value = entry.getValue();

            for (int component = 0;
                 component < result.length;
                 component++) {

                result[component] += value
                        * svdComponents[termIndex][component];
            }
        }

        return result;
    }

    private double[] extractNumericFeatures(PreparedSample sample) {
        double numComment = sample.numComment() == null
                ? 0.0
                : sample.numComment();

        double numActivities = sample.numActivities() == null
                ? 0.0
                : sample.numActivities();

        double textLength = sample.cleanedText().length();

        String[] tokens = sample.cleanedText()
                .split("\\s+");

        long uniqueTokens = Arrays.stream(tokens)
                .filter(token -> !token.isBlank())
                .distinct()
                .count();

        double tokenCount = Arrays.stream(tokens)
                .filter(token -> !token.isBlank())
                .count();

        double uniqueTokenRatio = tokenCount == 0
                ? 0.0
                : uniqueTokens / tokenCount;

        return new double[]{
                Math.log1p(numComment),
                Math.log1p(numActivities),
                textLength,
                tokenCount,
                uniqueTokenRatio
        };
    }

    private void fitNumericScaling(List<double[]> features) {
        int dimensions = features.getFirst().length;

        numericMeans = new double[dimensions];
        numericStandardDeviations = new double[dimensions];

        for (double[] feature : features) {
            for (int i = 0; i < dimensions; i++) {
                numericMeans[i] += feature[i];
            }
        }

        for (int i = 0; i < dimensions; i++) {
            numericMeans[i] /= features.size();
        }

        for (double[] feature : features) {
            for (int i = 0; i < dimensions; i++) {
                double difference = feature[i] - numericMeans[i];
                numericStandardDeviations[i] += difference * difference;
            }
        }

        for (int i = 0; i < dimensions; i++) {
            numericStandardDeviations[i] = Math.sqrt(
                    numericStandardDeviations[i] / features.size()
            );

            if (numericStandardDeviations[i] < 1.0E-10) {
                numericStandardDeviations[i] = 1.0;
            }
        }
    }

    private double[] normalizeNumericFeatures(double[] features) {
        double[] normalized = new double[features.length];

        for (int i = 0; i < features.length; i++) {
            normalized[i] = (features[i] - numericMeans[i])
                    / numericStandardDeviations[i];
        }

        return normalized;
    }

    private double[][] randomMatrix(int rows, int columns) {
        java.util.Random random = new java.util.Random(42);
        double[][] matrix = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = random.nextGaussian();
            }
        }

        return matrix;
    }

    private double[][] multiply(
            List<Map<Integer, Double>> matrix,
            double[][] right
    ) {
        double[][] result = new double[matrix.size()][right[0].length];

        for (int row = 0; row < matrix.size(); row++) {
            for (Map.Entry<Integer, Double> entry
                    : matrix.get(row).entrySet()) {

                int column = entry.getKey();
                double value = entry.getValue();

                for (int j = 0; j < right[0].length; j++) {
                    result[row][j] += value * right[column][j];
                }
            }
        }

        return result;
    }

    private double[][] transposeMultiply(
            List<Map<Integer, Double>> matrix,
            double[][] right
    ) {
        double[][] result = new double[vocabulary.size()][right[0].length];

        for (int row = 0; row < matrix.size(); row++) {
            for (Map.Entry<Integer, Double> entry
                    : matrix.get(row).entrySet()) {

                int column = entry.getKey();
                double value = entry.getValue();

                for (int j = 0; j < right[0].length; j++) {
                    result[column][j] += value * right[row][j];
                }
            }
        }

        return result;
    }

    private double[][] multiplyByTranspose(double[][] matrix) {
        int rows = matrix.length;
        int columns = matrix[0].length;

        double[][] result = new double[rows][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = i; j < rows; j++) {
                double sum = 0.0;

                for (int k = 0; k < columns; k++) {
                    sum += matrix[i][k] * matrix[j][k];
                }

                result[i][j] = sum;
                result[j][i] = sum;
            }
        }

        return result;
    }

    private double[][] orthonormalize(double[][] matrix) {
        int rows = matrix.length;
        int columns = matrix[0].length;

        double[][] result = new double[rows][columns];

        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                result[row][column] = matrix[row][column];
            }

            for (int previous = 0; previous < column; previous++) {
                double dot = 0.0;

                for (int row = 0; row < rows; row++) {
                    dot += result[row][column]
                            * result[row][previous];
                }

                for (int row = 0; row < rows; row++) {
                    result[row][column] -= dot
                            * result[row][previous];
                }
            }

            double norm = 0.0;

            for (int row = 0; row < rows; row++) {
                norm += result[row][column]
                        * result[row][column];
            }

            norm = Math.sqrt(norm);

            if (norm < 1.0E-12) {
                continue;
            }

            for (int row = 0; row < rows; row++) {
                result[row][column] /= norm;
            }
        }

        return result;
    }

    private EigenDecomposition decomposeSymmetric(double[][] matrix) {
        int size = matrix.length;
        double[][] a = Arrays.stream(matrix)
                .map(double[]::clone)
                .toArray(double[][]::new);

        double[][] vectors = new double[size][size];

        for (int i = 0; i < size; i++) {
            vectors[i][i] = 1.0;
        }

        for (int iteration = 0; iteration < size * size * 20; iteration++) {
            int p = 0;
            int q = 0;
            double maximum = 0.0;

            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    if (Math.abs(a[i][j]) > maximum) {
                        maximum = Math.abs(a[i][j]);
                        p = i;
                        q = j;
                    }
                }
            }

            if (maximum < 1.0E-10) {
                break;
            }

            double angle = 0.5 * Math.atan2(
                    2.0 * a[p][q],
                    a[q][q] - a[p][p]
            );

            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);

            double app = a[p][p];
            double aqq = a[q][q];
            double apq = a[p][q];

            a[p][p] = cosine * cosine * app
                    - 2.0 * sine * cosine * apq
                    + sine * sine * aqq;

            a[q][q] = sine * sine * app
                    + 2.0 * sine * cosine * apq
                    + cosine * cosine * aqq;

            a[p][q] = 0.0;
            a[q][p] = 0.0;

            for (int i = 0; i < size; i++) {
                if (i != p && i != q) {
                    double aip = a[i][p];
                    double aiq = a[i][q];

                    a[i][p] = cosine * aip - sine * aiq;
                    a[p][i] = a[i][p];

                    a[i][q] = sine * aip + cosine * aiq;
                    a[q][i] = a[i][q];
                }

                double vip = vectors[i][p];
                double viq = vectors[i][q];

                vectors[i][p] = cosine * vip - sine * viq;
                vectors[i][q] = sine * vip + cosine * viq;
            }
        }

        double[] values = new double[size];
        Integer[] order = new Integer[size];

        for (int i = 0; i < size; i++) {
            values[i] = a[i][i];
            order[i] = i;
        }

        Arrays.sort(order, Comparator.comparingDouble(
                (Integer index) -> values[index]
        ).reversed());

        return new EigenDecomposition(
                values,
                vectors,
                Arrays.stream(order).mapToInt(Integer::intValue).toArray()
        );
    }

    private double[][] multiplyTransposeLeft(
            double[][] left,
            List<Map<Integer, Double>> matrix
    ) {
        int rank = left[0].length;
        int columns = vocabulary.size();
        double[][] result = new double[rank][columns];

        for (int row = 0; row < matrix.size(); row++) {
            for (Map.Entry<Integer, Double> entry : matrix.get(row).entrySet()) {
                int column = entry.getKey();
                double value = entry.getValue();

                for (int j = 0; j < rank; j++) {
                    result[j][column] += left[row][j] * value;
                }
            }
        }

        return result;
    }

    private record EigenDecomposition(
            double[] values,
            double[][] vectors,
            int[] order
    ) {
    }
}