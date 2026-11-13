package com.projectmanagement.notification.scheduler;

import com.projectmanagement.notification.client.TaskServiceClient;
import com.projectmanagement.notification.dto.TaskDeadlineReminderResponse;
import com.projectmanagement.notification.entity.enums.NotificationType;
import com.projectmanagement.notification.service.NotificationCreationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineReminderScheduler {

    private final TaskServiceClient taskServiceClient;
    private final NotificationCreationService notificationCreationService;

    @Value("${app.notifications.deadline-reminder-days}")
    private int reminderDays;

    @Value("${app.notifications.time-zone:UTC}")
    private String timeZone;

    @Scheduled(
            cron = "${app.notifications.deadline-reminder-cron}",
            zone = "${app.notifications.time-zone:UTC}"
    )
    public void sendDeadlineReminders() {
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDateTime now = ZonedDateTime.now(zoneId).toLocalDateTime();
        LocalDateTime deadlineLimit = now.plusDays(reminderDays);

        List<TaskDeadlineReminderResponse> tasks;

        try {
            tasks = taskServiceClient.getTasksWithApproachingDeadlines(
                    now,
                    deadlineLimit
            );
        } catch (Exception exception) {
            log.error("Failed to retrieve tasks with approaching deadlines", exception);
            return;
        }

        if (tasks == null || tasks.isEmpty()) {
            log.info("No tasks with approaching deadlines found");
            return;
        }

        tasks.forEach(this::createDeadlineReminder);

        log.info("Processed {} tasks with approaching deadlines", tasks.size());
    }

    private void createDeadlineReminder(TaskDeadlineReminderResponse task) {
        if (task.assigneeId() == null ||
                task.taskId() == null ||
                task.deadline() == null) {
            log.warn("Skipped deadline reminder because task data is incomplete: {}",
                    task.taskId());
            return;
        }

        String sourceEventId = createSourceEventId(task);

        String message = String.format(
                "Приближается срок выполнения задачи \"%s\". Дедлайн: %s",
                task.title(),
                task.deadline().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        );

        try {
            notificationCreationService.createNotification(
                    task.assigneeId(),
                    NotificationType.DEADLINE_APPROACHING,
                    "Приближается срок выполнения задачи",
                    message,
                    task.taskId(),
                    task.projectId(),
                    sourceEventId
            );

            log.info("Deadline reminder processed for task: {}", task.taskId());
        } catch (Exception exception) {
            log.error(
                    "Failed to create deadline reminder for task: {}",
                    task.taskId(),
                    exception
            );
        }
    }

    private String createSourceEventId(TaskDeadlineReminderResponse task) {
        return "deadline:%s:%s:%s".formatted(
                task.taskId(),
                task.deadline(),
                task.assigneeId()
        );
    }
}