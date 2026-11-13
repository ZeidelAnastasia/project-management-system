CREATE TABLE comments (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    project_id UUID NOT NULL,
    author_id UUID NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_comments_task_id
    ON comments (task_id);

CREATE INDEX idx_comments_author_id
    ON comments (author_id);

CREATE INDEX idx_comments_task_created_at
    ON comments (task_id, created_at);