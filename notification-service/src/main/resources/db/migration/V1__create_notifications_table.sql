CREATE TABLE notifications
(
    id              UUID PRIMARY KEY,
    recipient_id    UUID         NOT NULL,
    type            VARCHAR(50)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    message         TEXT         NOT NULL,
    task_id         UUID,
    project_id      UUID,
    source_event_id VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL,
    read_at         TIMESTAMP,

    CONSTRAINT uk_notifications_recipient_source_event
        UNIQUE (recipient_id, source_event_id)
);

CREATE INDEX idx_notifications_recipient_created
    ON notifications (recipient_id, created_at);

CREATE INDEX idx_notifications_recipient_read
    ON notifications (recipient_id, read_at);