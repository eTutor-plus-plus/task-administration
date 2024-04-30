package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskCategoryTest {

    @Test
    void getSetName() {
        // Arrange
        var category = new TaskCategory();
        var expected = "test";

        // Act
        category.setName(expected);
        var actual = category.getName();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetParent() {
        // Arrange
        var category = new TaskCategory();
        var expected = new TaskCategory();
        expected.setId(123L);

        // Act
        category.setParent(expected);
        var actual = category.getParent();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetOrganizationalUnit() {
        // Arrange
        var category = new TaskCategory();
        var expected = new OrganizationalUnit();
        expected.setId(123L);

        // Act
        category.setOrganizationalUnit(expected);
        var actual = category.getOrganizationalUnit();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetMoodleId() {
        // Arrange
        var category = new TaskCategory();
        var expected = 123;

        // Act
        category.setMoodleId(expected);
        var actual = category.getMoodleId();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetChildren() {
        // Arrange
        var category = new TaskCategory();
        var expected = new HashSet<TaskCategory>();

        // Act
        category.setChildren(expected);
        var actual = category.getChildren();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetTasks() {
        // Arrange
        var category = new TaskCategory();
        var expected = new HashSet<Task>();

        // Act
        category.setTasks(expected);
        var actual = category.getTasks();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void testToString() {
        // Arrange
        var category = new TaskCategory();
        category.setId(123L);
        category.setName("test");

        // Act
        var actual = category.toString();

        // Assert
        assertThat(actual).contains("123", "test");
    }

}
