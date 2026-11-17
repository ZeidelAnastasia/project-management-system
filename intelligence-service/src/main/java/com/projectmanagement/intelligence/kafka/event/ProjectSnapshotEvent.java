package com.projectmanagement.intelligence.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProjectSnapshotEvent(
        UUID projectId,
        String projectName,
        String projectStatus,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}