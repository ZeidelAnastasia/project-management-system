package com.projectmanagement.project.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projectmanagement.project.entity.OutboxEvent;
import com.projectmanagement.project.entity.Project;
import com.projectmanagement.project.entity.enums.ProjectStatus;
import com.projectmanagement.project.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaOutboxServiceTest {

    private static final String PROJECT_CREATED_TOPIC = "project-created";
    private static final String PROJECT_UPDATED_TOPIC = "project-updated";
    private static final String PROJECT_ARCHIVED_TOPIC = "project-archived";
    private static final String PROJECT_RESTORED_TOPIC = "project-restored";
    private static final String PROJECT_COMPLETED_TOPIC = "project-completed";

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // <--- Регистрируем модуль для работы с Instant / LocalDateTime
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @InjectMocks
    private ProjectEventOutboxService projectEventOutboxService;

    private Project project;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                projectEventOutboxService,
                "projectCreatedTopic",
                PROJECT_CREATED_TOPIC
        );
        ReflectionTestUtils.setField(
                projectEventOutboxService,
                "projectUpdatedTopic",
                PROJECT_UPDATED_TOPIC
        );
        ReflectionTestUtils.setField(
                projectEventOutboxService,
                "projectArchivedTopic",
                PROJECT_ARCHIVED_TOPIC
        );
        ReflectionTestUtils.setField(
                projectEventOutboxService,
                "projectRestoredTopic",
                PROJECT_RESTORED_TOPIC
        );
        ReflectionTestUtils.setField(
                projectEventOutboxService,
                "projectCompletedTopic",
                PROJECT_COMPLETED_TOPIC
        );

        project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Test project");
        project.setCreatedBy(UUID.randomUUID());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldSaveProjectCreatedEvent() {
        verifyEvent(
                projectEventOutboxService::publishProjectCreated,
                "PROJECT_CREATED",
                PROJECT_CREATED_TOPIC
        );
    }

    @Test
    void shouldSaveProjectUpdatedEvent() {
        verifyEvent(
                projectEventOutboxService::publishProjectUpdated,
                "PROJECT_UPDATED",
                PROJECT_UPDATED_TOPIC
        );
    }

    @Test
    void shouldSaveProjectArchivedEvent() {
        verifyEvent(
                projectEventOutboxService::publishProjectArchived,
                "PROJECT_ARCHIVED",
                PROJECT_ARCHIVED_TOPIC
        );
    }

    @Test
    void shouldSaveProjectRestoredEvent() {
        verifyEvent(
                projectEventOutboxService::publishProjectRestored,
                "PROJECT_RESTORED",
                PROJECT_RESTORED_TOPIC
        );
    }

    @Test
    void shouldSaveProjectCompletedEvent() {
        verifyEvent(
                projectEventOutboxService::publishProjectCompleted,
                "PROJECT_COMPLETED",
                PROJECT_COMPLETED_TOPIC
        );
    }

    private void verifyEvent(
            Consumer<Project> eventPublisher,
            String expectedEventType,
            String expectedTopic
    ) {
        eventPublisher.accept(project);

        ArgumentCaptor<OutboxEvent> captor =
                ArgumentCaptor.forClass(OutboxEvent.class);

        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent savedEvent = captor.getValue();

        assertNotNull(savedEvent.getId());
        assertEquals(project.getId(), savedEvent.getAggregateId());
        assertEquals(expectedEventType, savedEvent.getEventType());
        assertEquals(expectedTopic, savedEvent.getTopic());
        assertNotNull(savedEvent.getCreatedAt());
        assertNotNull(savedEvent.getPayload());
        assertTrue(savedEvent.getPayload().contains(project.getId().toString()));
    }
}