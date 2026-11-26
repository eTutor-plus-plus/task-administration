ALTER TABLE tasks_task_categories
    ADD submit_feedback_level   SMALLINT NOT NULL DEFAULT 0,
    ADD diagnose_feedback_level SMALLINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT tasks_task_categories_submit_fl_chk CHECK (submit_feedback_level BETWEEN 0 AND 3),
    ADD CONSTRAINT tasks_task_categories_diagnose_fl_chk CHECK (diagnose_feedback_level BETWEEN 0 AND 3);
