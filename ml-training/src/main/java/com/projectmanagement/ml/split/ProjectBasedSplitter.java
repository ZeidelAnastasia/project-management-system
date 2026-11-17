
package com.projectmanagement.ml.split;

import com.projectmanagement.ml.model.DatasetSplit;
import com.projectmanagement.ml.model.PreparedSample;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ProjectBasedSplitter {

    private static final Pattern JIRA_ISSUE_PATTERN =
            Pattern.compile("(?i)\\b([A-Z][A-Z0-9]+-\\d+)\\b");

    public DatasetSplit split(
            List<PreparedSample> samples,
            double trainingRatio,
            long seed
    ) {
        validateInput(samples, trainingRatio);

        Map<String, List<PreparedSample>> projects =
                groupByProject(samples);

        if (projects.size() < 2) {
            throw new IllegalArgumentException(
                    "At least two different Jira projects are required "
                            + "for project-based splitting"
            );
        }

        List<Map.Entry<String, List<PreparedSample>>> groups =
                new ArrayList<>(projects.entrySet());

        Collections.shuffle(groups, new Random(seed));

        List<PreparedSample> trainingSamples = new ArrayList<>();
        List<PreparedSample> testSamples = new ArrayList<>();

        int targetTrainingSize = (int) Math.round(
                samples.size() * trainingRatio
        );

        for (int i = 0; i < groups.size(); i++) {
            Map.Entry<String, List<PreparedSample>> group = groups.get(i);

            int remainingGroups = groups.size() - i;

            if (trainingSamples.size() < targetTrainingSize
                    && remainingGroups > 1) {
                trainingSamples.addAll(group.getValue());
            } else {
                testSamples.addAll(group.getValue());
            }
        }

        if (trainingSamples.isEmpty() || testSamples.isEmpty()) {
            throw new IllegalStateException(
                    "Unable to create non-empty training and test datasets"
            );
        }

        Map<String, Integer> distribution = new LinkedHashMap<>();

        groups.stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry ->
                        distribution.put(
                                entry.getKey(),
                                entry.getValue().size()
                        )
                );

        validateNoProjectLeakage(
                trainingSamples,
                testSamples
        );

        log.info("Projects: {}", projects.size());
        log.info("Training samples: {}", trainingSamples.size());
        log.info("Test samples: {}", testSamples.size());

        return new DatasetSplit(
                List.copyOf(trainingSamples),
                List.copyOf(testSamples),
                Map.copyOf(distribution)
        );
    }

    private Map<String, List<PreparedSample>> groupByProject(
            List<PreparedSample> samples
    ) {
        Map<String, List<PreparedSample>> projects =
                new LinkedHashMap<>();

        for (PreparedSample sample : samples) {
            String projectKey = extractProjectKey(
                    sample.reference(),
                    sample.id()
            );

            projects.computeIfAbsent(
                    projectKey,
                    key -> new ArrayList<>()
            ).add(sample);
        }

        return projects;
    }

    private String extractProjectKey(
            String reference,
            String id
    ) {
        String issueKey = extractIssueKey(reference);

        if (issueKey == null) {
            issueKey = extractIssueKey(id);
        }

        if (issueKey == null) {
            throw new IllegalArgumentException(
                    "Cannot extract Jira project key. Reference: "
                            + reference + ", ID: " + id
            );
        }

        return issueKey.substring(0, issueKey.lastIndexOf('-'));
    }

    private String extractIssueKey(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        Matcher matcher = JIRA_ISSUE_PATTERN.matcher(value);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1).toUpperCase();
    }

    private void validateInput(
            List<PreparedSample> samples,
            double trainingRatio
    ) {
        if (samples == null || samples.size() < 2) {
            throw new IllegalArgumentException(
                    "At least two samples are required"
            );
        }

        if (trainingRatio <= 0 || trainingRatio >= 1) {
            throw new IllegalArgumentException(
                    "Training ratio must be between 0 and 1"
            );
        }
    }

    private void validateNoProjectLeakage(
            List<PreparedSample> trainingSamples,
            List<PreparedSample> testSamples
    ) {
        var trainingProjects = trainingSamples.stream()
                .map(sample -> extractProjectKey(
                        sample.reference(),
                        sample.id()
                ))
                .collect(java.util.stream.Collectors.toSet());

        var testProjects = testSamples.stream()
                .map(sample -> extractProjectKey(
                        sample.reference(),
                        sample.id()
                ))
                .collect(java.util.stream.Collectors.toSet());

        trainingProjects.retainAll(testProjects);

        if (!trainingProjects.isEmpty()) {
            throw new IllegalStateException(
                    "Project leakage detected: " + trainingProjects
            );
        }
    }
}
