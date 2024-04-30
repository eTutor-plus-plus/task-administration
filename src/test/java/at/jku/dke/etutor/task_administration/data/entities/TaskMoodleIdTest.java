package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskMoodleIdTest {

    @Test
    void constructor1() {
        // Act
        var entity = new TaskMoodleId();

        // Assert
        assertNotNull(entity.getId());
    }

    @Test
    void constructor2() {
        // Arrange
        var task = new Task();
        var taskCategory = new TaskCategory();
        var moodleId = 1L;
        task.setId(10L);
        taskCategory.setId(20L);

        // Act
        var entity = new TaskMoodleId(task, taskCategory, moodleId);

        // Assert
        assertEquals(task.getId(), entity.getId().getTaskId());
        assertEquals(taskCategory.getId(), entity.getId().getTaskCategoryId());
        assertEquals(task, entity.getTask());
        assertEquals(taskCategory, entity.getTaskCategory());
        assertEquals(moodleId, entity.getMoodleId());
    }

    @Test
    void getSetId() {
        // Arrange
        var entity = new TaskMoodleId();
        var expected = new TaskMoodleIdId(1L, 2L);

        // Act
        entity.setId(expected);
        var actual = entity.getId();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetTaskCategory() {
        // Arrange
        var entity = new TaskMoodleId();
        var expected = new TaskCategory();

        // Act
        entity.setTaskCategory(expected);
        var actual = entity.getTaskCategory();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetTask() {
        // Arrange
        var entity = new TaskMoodleId();
        var expected = new Task();

        // Act
        entity.setTask(expected);
        var actual = entity.getTask();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetMoodleId() {
        // Arrange
        var entity = new TaskMoodleId();
        var expected = 1L;

        // Act
        entity.setMoodleId(expected);
        var actual = entity.getMoodleId();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void testToString() {
        // Arrange
        var entity = new TaskMoodleId();
        entity.setMoodleId(1L);
        entity.setId(new TaskMoodleIdId(3L, 2L));

        // Act
        var actual = entity.toString();

        // Assert
        assertThat(actual).contains("1", "2", "3");
    }
}
