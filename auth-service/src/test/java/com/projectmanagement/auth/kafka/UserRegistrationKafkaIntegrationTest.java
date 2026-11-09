package com.projectmanagement.auth.kafka;

import com.projectmanagement.auth.dto.request.RegisterRequest;
import com.projectmanagement.auth.entity.Role;
import com.projectmanagement.auth.entity.enums.RoleCode;
import com.projectmanagement.auth.kafka.event.DomainEvent;
import com.projectmanagement.auth.kafka.event.UserRegisteredPayload;
import com.projectmanagement.auth.repository.RoleRepository;
import com.projectmanagement.auth.repository.UserRepository;
import com.projectmanagement.auth.service.AuthService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class UserRegistrationKafkaIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @MockitoBean
    private KafkaTemplate<String, DomainEvent<UserRegisteredPayload>> kafkaTemplate;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final List<Object> sentPayloads = new CopyOnWriteArrayList<>();

    @BeforeEach
    void setUp() {
        sentPayloads.clear();

        when(kafkaTemplate.send(any(), any())).thenAnswer(invocation -> {
            sentPayloads.add(invocation.getArgument(1));
            return CompletableFuture.completedFuture(null);
        });

        when(kafkaTemplate.send(any(), any(), any())).thenAnswer(invocation -> {
            sentPayloads.add(invocation.getArgument(2));
            return CompletableFuture.completedFuture(null);
        });

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenAnswer(invocation -> {
            ProducerRecord<?, ?> record = invocation.getArgument(0);
            sentPayloads.add(record.value());
            return CompletableFuture.completedFuture(null);
        });

        if (roleRepository.findByCode(RoleCode.EMPLOYEE).isEmpty()) {
            Role role = new Role();
            role.setCode(RoleCode.EMPLOYEE);
            roleRepository.save(role);
        }
    }

    @Test
    @DisplayName("Отправка события в Kafka после фиксации успешной регистрации")
    void register_CommitTransaction_PublishesKafkaEvent() {
        String email = "success-kafka@example.com";
        RegisterRequest request = new RegisterRequest(email, "Password123!");

        authService.register(request);

        assertThat(userRepository.existsByEmailIgnoreCase(email)).isTrue();
        assertThat(sentPayloads).hasSize(1);

        Object payloadObject = sentPayloads.get(0);

        assertThat(payloadObject).isInstanceOf(DomainEvent.class);
        DomainEvent<?> domainEvent = (DomainEvent<?>) payloadObject;

        assertThat(domainEvent.payload()).isInstanceOf(UserRegisteredPayload.class);
        UserRegisteredPayload payload = (UserRegisteredPayload) domainEvent.payload();

        assertThat(payload.email()).isEqualTo(email.toLowerCase());
        assertThat(payload.userId()).isNotNull();
    }

    @Test
    @DisplayName("Событие не отправляется в Kafka при откате транзакции")
    void register_RollbackTransaction_DoesNotPublishKafkaEvent() {
        String email = "rollback-kafka@example.com";
        RegisterRequest request = new RegisterRequest(email, "Password123!");
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        assertThrows(RuntimeException.class, () ->
                transactionTemplate.execute(status -> {
                    authService.register(request);
                    throw new RuntimeException("Forced transaction rollback");
                })
        );

        assertThat(userRepository.existsByEmailIgnoreCase(email)).isFalse();
        assertThat(sentPayloads).isEmpty();
    }
}