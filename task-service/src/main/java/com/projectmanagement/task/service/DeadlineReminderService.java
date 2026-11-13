package com.projectmanagement.task.service;

import com.projectmanagement.task.dto.response.DeadlineReminderTaskResponse;

import java.time.OffsetDateTime;
import java.util.List;

public interface DeadlineReminderService {

    List<DeadlineReminderTaskResponse> getTasksForDeadlineReminders(
            OffsetDateTime from,
            OffsetDateTime to
    );
}