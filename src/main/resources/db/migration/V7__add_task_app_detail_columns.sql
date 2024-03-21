ALTER TABLE task_apps
    ADD COLUMN task_prefix       VARCHAR(50),
    ADD COLUMN task_group_prefix VARCHAR(50),
    ADD COLUMN submission_prefix VARCHAR(50);
