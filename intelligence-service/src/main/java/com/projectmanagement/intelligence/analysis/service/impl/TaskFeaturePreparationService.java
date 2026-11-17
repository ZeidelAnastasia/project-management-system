
package com.projectmanagement.intelligence.analysis.service.impl;

import com.projectmanagement.intelligence.dto.request.TaskAnalysisRequest;
import com.projectmanagement.intelligence.entity.TaskSnapshotEntity;
import com.projectmanagement.ml.model.ExtractedSample;
import com.projectmanagement.ml.model.ModelBundle;
import com.projectmanagement.ml.model.PreparedSample;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TaskFeaturePreparationService {

    private static final Pattern HTML_TAG_PATTERN =
            Pattern.compile("<[^>]+>");

    private static final Pattern URL_PATTERN =
            Pattern.compile("https?://\\S+|www\\.\\S+");

    private static final Pattern CODE_BLOCK_PATTERN =
            Pattern.compile("\\{code[^}]*}|\\{noformat}|\\{code}");

    private static final Pattern MARKDOWN_PATTERN =
            Pattern.compile("[\\*_~`#>|\\[\\](){}]");

    private static final Pattern SPECIAL_CHARACTERS_PATTERN =
            Pattern.compile("[^\\p{L}\\p{N}\\s]");

    private static final Pattern MULTIPLE_SPACES_PATTERN =
            Pattern.compile("\\s+");

    private final ModelBundle modelBundle;

    public double[] prepare(
            TaskAnalysisRequest request,
            TaskSnapshotEntity task
    ) {
        String title = request.title() != null
                ? request.title()
                : task.getTitle();

        String description = request.description() != null
                ? request.description()
                : task.getDescription();

        List<String> labels = request.labels() != null
                ? request.labels()
                : task.getLabels();

        String corpus = Stream.concat(
                        Stream.of(title, description),
                        labels == null ? Stream.empty() : labels.stream()
                )
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));

        String cleanedText = cleanText(corpus);

        if (cleanedText.isBlank()) {
            throw new IllegalArgumentException(
                    "Task text must not be empty"
            );
        }

        Integer estimatedEffort = request.estimatedEffort() != null
                ? request.estimatedEffort()
                : task.getEstimatedEffort();

        PreparedSample sample = new PreparedSample(
                task.getTaskId().toString(),
                task.getCorpus(),
                cleanText(task.getCorpus()),
                task.getNumComments(),
                task.getNumActivities(),
                estimatedEffort == null
                        ? null
                        : estimatedEffort.doubleValue(),
                0.0,
                null
        );

        List<ExtractedSample> extractedSamples =
                modelBundle.getFeatureExtractor().transform(
                        List.of(sample)
                );

        if (extractedSamples.isEmpty()) {
            throw new IllegalStateException(
                    "Failed to extract task features"
            );
        }

        return extractedSamples.get(0).features();
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
}
