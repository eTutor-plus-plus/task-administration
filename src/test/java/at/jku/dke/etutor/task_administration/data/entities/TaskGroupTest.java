package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskGroupTest {

    @Test
    void constructor1() {
        // Act
        var group = new TaskGroup();
        var actual = group.getStatus();

        // Assert
        assertEquals(TaskStatus.DRAFT, actual);
    }

    @Test
    void getName() {
        // Arrange
        var group = new TaskGroup();
        var expected = "test";

        // Act
        group.setName(expected);
        var actual = group.getName();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getDescriptionDe() {
        // Arrange
        var group = new TaskGroup();
        var expected = "test";

        // Act
        group.setDescriptionDe(expected);
        var actual = group.getDescriptionDe();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getDescriptionEn() {
        // Arrange
        var group = new TaskGroup();
        var expected = "test";

        // Act
        group.setDescriptionEn(expected);
        var actual = group.getDescriptionEn();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getTaskGroupType() {
        // Arrange
        var group = new TaskGroup();
        var expected = "sql";

        // Act
        group.setTaskGroupType(expected);
        var actual = group.getTaskGroupType();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getStatus() {
        // Arrange
        var group = new TaskGroup();
        var expected = TaskStatus.APPROVED;

        // Act
        group.setStatus(expected);
        var actual = group.getStatus();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getOrganizationalUnit() {
        // Arrange
        var group = new TaskGroup();
        var expected = new OrganizationalUnit();

        // Act
        group.setOrganizationalUnit(expected);
        var actual = group.getOrganizationalUnit();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getApprovedBy() {
        // Arrange
        var group = new TaskGroup();
        var expected = "user";

        // Act
        group.setApprovedBy(expected);
        var actual = group.getApprovedBy();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getApprovedDate() {
        // Arrange
        var group = new TaskGroup();
        var expected = OffsetDateTime.now();

        // Act
        group.setApprovedDate(expected);
        var actual = group.getApprovedDate();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getTasks() {
        // Arrange
        var group = new TaskGroup();
        var expected = new HashSet<Task>();

        // Act
        group.setTasks(expected);
        var actual = group.getTasks();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void testToString() {
        // Arrange
        var group = new TaskGroup();
        group.setId(10L);
        group.setName("test");
        group.setStatus(TaskStatus.READY_FOR_APPROVAL);

        // Act
        var actual = group.toString();

        // Assert
        assertThat(actual).contains("10", "test", "READY_FOR_APPROVAL");
    }
}
