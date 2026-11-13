package com.projectmanagement.task.controller;

import com.projectmanagement.task.api.InternalDeadlineReminderApi;
import com.projectmanagement.task.dto.response.DeadlineReminderTaskResponse;
import com.projectmanagement.task.service.DeadlineReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class InternalDeadlineReminderController implements InternalDeadlineReminderApi {

    private final DeadlineReminderService deadlineReminderService;

    @Override
    public List<DeadlineReminderTaskResponse> getDeadlineReminderTasks(
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        return deadlineReminderService.getTasksForDeadlineReminders(from, to);
    }
}