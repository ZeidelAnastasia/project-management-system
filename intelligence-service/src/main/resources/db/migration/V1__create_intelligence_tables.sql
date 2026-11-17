
CREATE TABLE task_analysis
(
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL UNIQUE,
    complexity_score DOUBLE PRECISION NOT NULL,
    complexity_level VARCHAR(50) NOT NULL,
    predicted_effort DOUBLE PRECISION NOT NULL,
    analysis_details JSONB,
    model_version VARCHAR(100) NOT NULL
);

CREATE INDEX idx_task_analysis_task_id
    ON task_analysis (task_id);

CREATE TABLE project_risk_assessments
(
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    risk_score DOUBLE PRECISION NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    delay_probability DOUBLE PRECISION NOT NULL,
    predicted_completion_date DATE,
    assessment_details JSONB,
    assessed_at TIMESTAMP NOT NULL,
    model_version VARCHAR(100) NOT NULL,

    CONSTRAINT chk_project_risk_level
        CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH')),

    CONSTRAINT chk_risk_score
        CHECK (risk_score BETWEEN 0 AND 100),

    CONSTRAINT chk_delay_probability
        CHECK (delay_probability BETWEEN 0 AND 1)
);

CREATE INDEX idx_project_risk_assessments_project_id
    ON project_risk_assessments (project_id);

CREATE INDEX idx_project_risk_assessments_assessed_at
    ON project_risk_assessments (project_id, assessed_at DESC);

CREATE TABLE risk_factors
(
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL,
    factor_type VARCHAR(100) NOT NULL,
    factor_value DOUBLE PRECISION NOT NULL,
    impact_score DOUBLE PRECISION NOT NULL,
    description TEXT,

    CONSTRAINT fk_risk_factors_assessment
        FOREIGN KEY (assessment_id)
        REFERENCES project_risk_assessments (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_risk_factors_assessment_id
    ON risk_factors (assessment_id);

CREATE TABLE task_snapshots
(
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL UNIQUE,
    project_id UUID NOT NULL,
    assignee_id UUID,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    corpus TEXT,
    priority VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    due_date DATE,
    estimated_effort INTEGER,
    actual_effort INTEGER,
    num_comments INTEGER NOT NULL DEFAULT 0,
    num_activities INTEGER NOT NULL DEFAULT 0,
    labels JSONB,
    status_change_count INTEGER NOT NULL DEFAULT 0,
    deadline_change_count INTEGER NOT NULL DEFAULT 0,
    reassignment_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

CREATE INDEX idx_task_snapshots_project_id
    ON task_snapshots (project_id);

CREATE INDEX idx_task_snapshots_assignee_id
    ON task_snapshots (assignee_id);

CREATE INDEX idx_task_snapshots_status
    ON task_snapshots (status);

CREATE INDEX idx_task_snapshots_priority_status
    ON task_snapshots (priority, status);

CREATE TABLE project_snapshots
(
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL UNIQUE,
    project_name VARCHAR(255),
    project_status VARCHAR(30) NOT NULL,
    planned_start_date DATE,
    planned_end_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);