package com.projectmanagement.task.service.impl;

import com.projectmanagement.task.dto.response.DeadlineReminderTaskResponse;
import com.projectmanagement.task.entity.enums.TaskStatus;
import com.projectmanagement.task.mapper.TaskMapper;
import com.projectmanagement.task.repository.TaskRepository;
import com.projectmanagement.task.service.DeadlineReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeadlineReminderServiceImpl implements DeadlineReminderService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Value("${app.time-zone:Europe/Moscow}")
    private String timeZone;

    @Override
    public List<DeadlineReminderTaskResponse> getTasksForDeadlineReminders(
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid date interval");
        }

        ZoneId zoneId = ZoneId.of(timeZone);

        LocalDate fromDate = from.atZoneSameInstant(zoneId).toLocalDate();
        LocalDate toDate = to.atZoneSameInstant(zoneId).toLocalDate();

        return taskRepository
                .findAllByDueDateBetweenAndAssigneeIdIsNotNullAndStatusNot(
                        fromDate,
                        toDate,
                        TaskStatus.DONE
                )
                .stream()
                .map(taskMapper::toDeadlineReminderResponse)
                .toList();
    }
}