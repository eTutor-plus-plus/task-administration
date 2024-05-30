CREATE TABLE task_moodleids
(
    task_category_id BIGINT NOT NULL,
    task_id          BIGINT NOT NULL,
    moodle_id        BIGINT NOT NULL,
    CONSTRAINT task_moodleids_pk PRIMARY KEY (task_category_id, task_id),
    CONSTRAINT task_moodleids_t_fk FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE CASCADE,
    CONSTRAINT task_moodleids_c_fk FOREIGN KEY (task_category_id) REFERENCES task_categories (id)
        ON DELETE CASCADE
);
ALTER TABLE tasks
    DROP COLUMN moodle_id;

