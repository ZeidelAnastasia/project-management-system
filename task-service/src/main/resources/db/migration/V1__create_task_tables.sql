
CREATE TABLE tasks
(
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    assignee_id UUID,
    created_by UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    due_date DATE,
    estimated_effort INTEGER,
    actual_effort INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_tasks_status
        CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),

    CONSTRAINT chk_tasks_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),

    CONSTRAINT chk_tasks_estimated_effort
        CHECK (estimated_effort IS NULL OR estimated_effort >= 0),

    CONSTRAINT chk_tasks_actual_effort
        CHECK (actual_effort IS NULL OR actual_effort >= 0)
);

CREATE TABLE labels
(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7)
);

CREATE TABLE task_labels
(
    task_id UUID NOT NULL,
    label_id UUID NOT NULL,

    CONSTRAINT pk_task_labels
        PRIMARY KEY (task_id, label_id),

    CONSTRAINT fk_task_labels_task
        FOREIGN KEY (task_id)
            REFERENCES tasks (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_task_labels_label
        FOREIGN KEY (label_id)
            REFERENCES labels (id)
            ON DELETE RESTRICT
);

CREATE TABLE task_history
(
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    changed_by UUID NOT NULL,
    change_type VARCHAR(30) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_task_history_task
        FOREIGN KEY (task_id)
            REFERENCES tasks (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_task_history_change_type
        CHECK (change_type IN ('STATUS', 'ASSIGNEE', 'DEADLINE'))
);

CREATE INDEX idx_tasks_project_id
    ON tasks (project_id);

CREATE INDEX idx_tasks_assignee_id
    ON tasks (assignee_id);

CREATE INDEX idx_tasks_status
    ON tasks (status);

CREATE INDEX idx_tasks_priority
    ON tasks (priority);

CREATE INDEX idx_tasks_due_date
    ON tasks (due_date);

CREATE INDEX idx_task_labels_label_id
    ON task_labels (label_id);

CREATE INDEX idx_task_history_task_id_changed_at
    ON task_history (task_id, changed_at DESC);
