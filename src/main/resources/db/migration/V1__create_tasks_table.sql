CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    text TEXT NOT NULL,
    language VARCHAR(2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    corrected_text TEXT,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_tasks_status ON tasks (status);
