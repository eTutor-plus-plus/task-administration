ALTER TABLE tasks
    ADD moodle_sync boolean;


CREATE OR REPLACE FUNCTION check_task_moodleid_sync()
RETURNS TRIGGER AS $$
DECLARE
    all_categories_match BOOLEAN;
BEGIN
    SELECT NOT EXISTS (
        SELECT 1
        FROM tasks_task_categories ttc
        WHERE ttc.task_id = NEW.task_id
          AND NOT EXISTS (
            SELECT 1
            FROM task_moodleids tmi
            WHERE tmi.task_id = NEW.task_id
              AND tmi.task_category_id = ttc.task_category_id
        )
    ) INTO all_categories_match;

-- Update the 'task' table based on whether all categories match
UPDATE tasks
SET moodle_sync = all_categories_match
WHERE id = NEW.task_id;

-- Allow the insert operation
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER task_moodleId_trigger AFTER INSERT OR UPDATE OR DELETE
    ON task_moodleids
    FOR EACH ROW EXECUTE FUNCTION check_task_moodleid_sync();

