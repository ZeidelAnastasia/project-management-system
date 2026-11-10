CREATE TABLE projects
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    status       VARCHAR(20) NOT NULL,
    created_by   UUID NOT NULL,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL,
    archived_at  TIMESTAMP,
    completed_at TIMESTAMP,

    CONSTRAINT chk_projects_status
        CHECK (status IN ('ACTIVE', 'ARCHIVED', 'COMPLETED'))
);

CREATE TABLE project_members
(
    project_id   UUID NOT NULL,
    user_id      UUID NOT NULL,
    project_role VARCHAR(20) NOT NULL,
    joined_at    TIMESTAMP NOT NULL,

    CONSTRAINT pk_project_members
        PRIMARY KEY (project_id, user_id),

    CONSTRAINT fk_project_members_project
        FOREIGN KEY (project_id)
            REFERENCES projects (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_project_members_role
        CHECK (project_role IN ('MANAGER', 'MEMBER'))
);

CREATE INDEX idx_projects_created_by
    ON projects (created_by);

CREATE INDEX idx_projects_status
    ON projects (status);

CREATE INDEX idx_project_members_user_id
    ON project_members (user_id);