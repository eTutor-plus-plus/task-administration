CREATE TABLE task_moodleIDs
    (
        task_category_id BIGINT,
        task_id          BIGINT,
        moodle_id        BIGINT,
        CONSTRAINT task_moodleIDs_pk PRIMARY KEY (task_category_id, task_id),
        CONSTRAINT task_moodleIds_t_fk FOREIGN KEY (task_id) REFERENCES tasks (id)
            ON DELETE CASCADE,
        CONSTRAINT task_moodleIDs_c_fk FOREIGN KEY (task_category_id) REFERENCES task_categories (id)
            ON DELETE CASCADE
);
ALTER TABLE tasks DROP COLUMN moodle_id;

