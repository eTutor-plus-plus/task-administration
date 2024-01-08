-- --------------------------------------------------------
CREATE OR REPLACE FUNCTION validate_task_category_loop() RETURNS TRIGGER AS
$loop_trg$
DECLARE
    cnt INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO cnt
    FROM (WITH RECURSIVE parent_category(id) AS (SELECT id, parent_id
                                                 FROM task_categories
                                                 WHERE parent_id IS NULL

                                                 UNION ALL

                                                 SELECT c.id, COALESCE(pc.parent_id, c.id)
                                                 FROM task_categories c
                                                          JOIN task_categories pc ON pc.id = c.parent_id)
          SELECT id, parent_id
          FROM task_categories
          WHERE id = new.id

          EXCEPT

          SELECT id, parent_id
          FROM parent_category
          WHERE id = new.id) s;

    IF cnt > 0 THEN
        RAISE EXCEPTION 'Category cannot be a parent of itself' USING ERRCODE = 'ZCAT1';
    END IF;

    RETURN new;
END;
$loop_trg$ LANGUAGE plpgsql;

-- CREATE CONSTRAINT TRIGGER task_categories_no_loop_trg
--     AFTER INSERT OR UPDATE OF id, parent_id
--     ON task_categories
--     DEFERRABLE INITIALLY DEFERRED
--     FOR EACH ROW
-- EXECUTE FUNCTION validate_task_category_loop(); TODO: funktioniert nicht korrekt

-- --------------------------------------------------------
CREATE OR REPLACE FUNCTION validate_task_category_all_same_ou() RETURNS TRIGGER AS
$loop_trg$
DECLARE
    cnt        INTEGER;
BEGIN
    -- Count task categories where ou_id of parent is different from ou_id of child
    SELECT COUNT(*)
    INTO cnt
    FROM task_categories c
             JOIN task_categories p ON c.parent_id = p.id
    WHERE c.ou_id <> p.ou_id
      AND (c.id = new.id OR p.id = new.id);

    IF cnt > 0 THEN
        RAISE EXCEPTION 'Child and parent category must belong to the same organizational unit' USING ERRCODE = 'ZORG1';
    END IF;

    RETURN new;
END;
$loop_trg$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER task_categories_all_same_ou_trg
    AFTER INSERT OR UPDATE OF parent_id, ou_id
    ON task_categories
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION validate_task_category_all_same_ou();

-- --------------------------------------------------------
CREATE OR REPLACE FUNCTION validate_task_and_category_same_ou() RETURNS TRIGGER AS
$loop_trg$
DECLARE
    cnt INTEGER;
BEGIN
    -- Count tasks where ou_id of the task is different from ou_id of category
    SELECT COUNT(*)
    INTO cnt
    FROM task_categories c
             JOIN tasks_task_categories tc ON c.id = tc.task_category_id
             JOIN tasks t ON tc.task_id = t.id
    WHERE c.ou_id <> t.ou_id;

    IF cnt > 0 THEN
        RAISE EXCEPTION 'Task and task category must belong to the same organizational unit' USING ERRCODE = 'ZORG2';
    END IF;

    RETURN new;
END;
$loop_trg$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER task_categories_tasks_same_ou_trg
    AFTER INSERT OR UPDATE OF ou_id
    ON task_categories
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION validate_task_and_category_same_ou();

CREATE CONSTRAINT TRIGGER task_categories_tasks_same_ou_trg
    AFTER INSERT OR UPDATE OF ou_id
    ON tasks
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION validate_task_and_category_same_ou();

CREATE CONSTRAINT TRIGGER task_categories_tasks_same_ou_trg
    AFTER INSERT OR UPDATE OF task_id, task_category_id
    ON tasks_task_categories
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION validate_task_and_category_same_ou();

-- --------------------------------------------------------
CREATE OR REPLACE FUNCTION validate_task_and_group_same_ou() RETURNS TRIGGER AS
$loop_trg$
DECLARE
    cnt INTEGER;
BEGIN
    -- Count tasks where ou_id of the task is different from ou_id of group
    SELECT COUNT(*)
    INTO cnt
    FROM task_groups g
             JOIN tasks t ON g.id = t.task_group_id
    WHERE g.ou_id <> t.ou_id;

    IF cnt > 0 THEN
        RAISE EXCEPTION 'Task and task group must belong to the same organizational unit' USING ERRCODE = 'ZORG3';
    END IF;

    RETURN new;
END;
$loop_trg$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER task_groups_tasks_same_ou_trg
    AFTER INSERT OR UPDATE OF ou_id
    ON task_groups
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION validate_task_and_group_same_ou();

CREATE CONSTRAINT TRIGGER task_groups_tasks_same_ou_trg
    AFTER INSERT OR UPDATE OF ou_id, task_group_id
    ON tasks
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE FUNCTION validate_task_and_group_same_ou();
