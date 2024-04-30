package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TaskMoodleIdIdTest {

    @Test
    void constructor1() {
        // Act
        var id = new TaskMoodleIdId(3L, 4L);

        // Assert
        assertEquals(3L, id.getTaskCategoryId());
        assertEquals(4L, id.getTaskId());
    }

    @Test
    void getSetTaskCategoryId() {
        // Arrange
        var id = new TaskMoodleIdId();
        var expected = 3L;

        // Act
        id.setTaskCategoryId(expected);
        var result = id.getTaskCategoryId();

        // Assert
        assertEquals(expected, result);
    }

    @Test
    void getSetTaskId() {
        // Arrange
        var id = new TaskMoodleIdId();
        var expected = 3L;

        // Act
        id.setTaskId(expected);
        var result = id.getTaskId();

        // Assert
        assertEquals(expected, result);
    }

    @SuppressWarnings({"EqualsWithItself", "ConstantValue"})
    @Test
    void equals_same() {
        // Arrange
        var id = new TaskMoodleIdId(3L, 4L);

        // Act
        var result = id.equals(id);

        // Assert
        assertTrue(result);
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void equals_otherNull() {
        // Arrange
        var id = new TaskMoodleIdId(3L, 4L);

        // Act
        var result = id.equals(null);

        // Assert
        assertFalse(result);
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    void equals_otherClass() {
        // Arrange
        var id = new TaskMoodleIdId(3L, 4L);

        // Act
        var result = id.equals("Test");

        // Assert
        assertFalse(result);
    }

    @Test
    void equals_differentTaskCategoryId() {
        // Arrange
        var id1 = new TaskMoodleIdId(3L, 4L);
        var id2 = new TaskMoodleIdId(5L, 4L);

        // Act
        var result = id1.equals(id2);

        // Assert
        assertFalse(result);
    }

    @Test
    void equals_differentTaskId() {
        // Arrange
        var id1 = new TaskMoodleIdId(3L, 4L);
        var id2 = new TaskMoodleIdId(3L, 5L);

        // Act
        var result = id1.equals(id2);

        // Assert
        assertFalse(result);
    }


    @Test
    void equals_equals() {
        // Arrange
        var id1 = new TaskMoodleIdId(3L, 4L);
        var id2 = new TaskMoodleIdId(3L, 4L);

        // Act
        var result = id1.equals(id2);

        // Assert
        assertTrue(result);
    }

    @Test
    void hashCode_equals() {
        // Arrange
        var id1 = new TaskMoodleIdId(3L, 4L);
        var id2 = new TaskMoodleIdId(3L, 4L);

        // Act
        var result1 = id1.hashCode();
        var result2 = id2.hashCode();

        // Assert
        assertEquals(result1, result2);
    }

    @Test
    void hashCode_notEquals() {
        // Arrange
        var id1 = new TaskMoodleIdId(3L, 4L);
        var id2 = new TaskMoodleIdId(5L, 4L);

        // Act
        var result1 = id1.hashCode();
        var result2 = id2.hashCode();

        // Assert
        assertNotEquals(result1, result2);
    }

    @Test
    void testToString() {
        // Arrange
        var id = new TaskMoodleIdId(3L, 4L);

        // Act
        var result = id.toString();

        // Assert
        assertThat(result).contains("3", "4");
    }
}
