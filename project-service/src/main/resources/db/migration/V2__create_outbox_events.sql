CREATE TABLE outbox_events
(
    id           UUID PRIMARY KEY,
    aggregate_id UUID         NOT NULL,
    event_type   VARCHAR(100) NOT NULL,
    topic        VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    attempts     INTEGER      NOT NULL DEFAULT 0,
    last_error   TEXT
);

CREATE INDEX idx_outbox_events_unpublished
    ON outbox_events (published_at, created_at);