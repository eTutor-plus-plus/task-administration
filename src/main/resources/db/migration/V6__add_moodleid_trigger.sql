ALTER TABLE tasks
    ADD moodle_sync BOOLEAN NOT NULL DEFAULT FALSE;

CREATE OR REPLACE FUNCTION check_task_moodleid_sync()
    RETURNS TRIGGER AS
$$
DECLARE
    all_categories_match BOOLEAN;
BEGIN
    SELECT NOT EXISTS (SELECT 1
                       FROM tasks_task_categories ttc
                       WHERE ttc.task_id = new.task_id
                         AND NOT EXISTS (SELECT 1
                                         FROM task_moodleids tmi
                                         WHERE tmi.task_id = new.task_id
                                           AND tmi.task_category_id = ttc.task_category_id))
    INTO all_categories_match;

    -- Update the 'task' table based on whether all categories match
    UPDATE tasks
    SET moodle_sync = all_categories_match
    WHERE id = new.task_id;

    -- Allow the insert operation
    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER task_moodleid_trigger
    AFTER INSERT OR UPDATE OR DELETE
    ON task_moodleids
    FOR EACH ROW
EXECUTE FUNCTION check_task_moodleid_sync();

