package com.projectmanagement.ml.preprocessing;

import com.projectmanagement.ml.model.PreparedSample;
import com.projectmanagement.ml.model.TrainingSample;
import lombok.extern.slf4j.Slf4j;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
public class DataPreprocessor {

    private static final Pattern HTML_TAG_PATTERN =
            Pattern.compile("<[^>]+>");

    private static final Pattern URL_PATTERN =
            Pattern.compile("https?://\\S+|www\\.\\S+");

    private static final Pattern CODE_BLOCK_PATTERN =
            Pattern.compile("\\{code[^}]*}|\\{noformat}|\\{code}|\\{noformat}");

    private static final Pattern MARKDOWN_PATTERN =
            Pattern.compile("[*_~`#>|\\[\\](){}]");

    private static final Pattern SPECIAL_CHARACTERS_PATTERN =
            Pattern.compile("[^\\p{L}\\p{N}\\s]");

    private static final Pattern MULTIPLE_SPACES_PATTERN =
            Pattern.compile("\\s+");

    private static final Pattern NUMBER_PATTERN =
            Pattern.compile("\\b\\d+(?:\\.\\d+)?\\b");

    public List<PreparedSample> preprocess(List<TrainingSample> samples) {
        List<PreparedSample> preparedSamples = new ArrayList<>();

        int rejectedCount = 0;

        for (TrainingSample sample : samples) {
            if (!isValid(sample)) {
                rejectedCount++;
                continue;
            }

            String cleanedText = cleanText(sample.corpus());

            if (cleanedText.isBlank()) {
                rejectedCount++;
                continue;
            }

            preparedSamples.add(new PreparedSample(
                    sample.id(),
                    sample.corpus(),
                    cleanText(sample.corpus()),
                    sample.numComment(),
                    sample.numActivities(),
                    sample.expertEstimatedEffort(),
                    sample.actualEffort(),
                    sample.reference()
            ));
        }

        log.info(
                "Preprocessing completed. Accepted: {}, rejected: {}",
                preparedSamples.size(),
                rejectedCount
        );

        return preparedSamples;
    }

    private boolean isValid(TrainingSample sample) {
        if (sample == null) {
            return false;
        }

        if (sample.id() == null || sample.id().isBlank()) {
            return false;
        }

        if (sample.actualEffort() == null) {
            return false;
        }

        if (!Double.isFinite(sample.actualEffort())
                || sample.actualEffort() <= 0) {
            return false;
        }

        if (sample.corpus() == null) {
            return false;
        }

        return true;
    }

    private String cleanText(String corpus) {
        String text = corpus == null ? "" : corpus;

        text = Normalizer.normalize(text, Normalizer.Form.NFKC);
        text = text.toLowerCase(Locale.ROOT);
        text = HTML_TAG_PATTERN.matcher(text).replaceAll(" ");
        text = URL_PATTERN.matcher(text).replaceAll(" ");
        text = CODE_BLOCK_PATTERN.matcher(text).replaceAll(" ");
        text = MARKDOWN_PATTERN.matcher(text).replaceAll(" ");
        text = SPECIAL_CHARACTERS_PATTERN.matcher(text).replaceAll(" ");
        text = MULTIPLE_SPACES_PATTERN.matcher(text).replaceAll(" ");

        return text.trim();
    }

    private Double normalizeExpertEffort(Double effort) {
        if (effort == null || !Double.isFinite(effort) || effort <= 0) {
            return null;
        }

        return effort;
    }

    public List<PreparedSample> removeOutliers(
            List<PreparedSample> samples,
            double lowerQuantile,
            double upperQuantile
    ) {
        if (samples.isEmpty()) {
            return samples;
        }

        if (lowerQuantile < 0
                || upperQuantile > 1
                || lowerQuantile >= upperQuantile) {
            throw new IllegalArgumentException(
                    "Invalid quantile boundaries"
            );
        }

        double[] efforts = samples.stream()
                .mapToDouble(PreparedSample::actualEffort)
                .sorted()
                .toArray();

        double lowerBound = percentile(efforts, lowerQuantile);
        double upperBound = percentile(efforts, upperQuantile);

        List<PreparedSample> filteredSamples = samples.stream()
                .filter(sample ->
                        sample.actualEffort() >= lowerBound
                                && sample.actualEffort() <= upperBound
                )
                .toList();

        log.info(
                "Outlier removal completed. Before: {}, after: {}",
                samples.size(),
                filteredSamples.size()
        );

        return filteredSamples;
    }

    private double percentile(double[] sortedValues, double quantile) {
        double index = quantile * (sortedValues.length - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);

        if (lowerIndex == upperIndex) {
            return sortedValues[lowerIndex];
        }

        double fraction = index - lowerIndex;

        return sortedValues[lowerIndex]
                + fraction * (sortedValues[upperIndex]
                - sortedValues[lowerIndex]);
    }

    public double transformTarget(double actualEffort) {
        if (!Double.isFinite(actualEffort) || actualEffort <= 0) {
            throw new IllegalArgumentException(
                    "Actual effort must be a positive finite number"
            );
        }

        return Math.log1p(actualEffort);
    }

    public double inverseTransformTarget(double transformedEffort) {
        return Math.expm1(transformedEffort);
    }
}