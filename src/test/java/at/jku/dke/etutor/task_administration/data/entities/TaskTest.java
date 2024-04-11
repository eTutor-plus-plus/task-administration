package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    @Test
    void constructor1() {
        // Act
        var task = new Task();
        var actual = task.getStatus();

        // Assert
        assertEquals(TaskStatus.DRAFT, actual);
    }

    @Test
    void getSetOrganizationalUnit() {
        // Arrange
        var task = new Task();
        var expected = new OrganizationalUnit();

        // Act
        task.setOrganizationalUnit(expected);
        var actual = task.getOrganizationalUnit();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetTitle() {
        // Arrange
        var task = new Task();
        var expected = "test";

        // Act
        task.setTitle(expected);
        var actual = task.getTitle();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetDescriptionDe() {
        // Arrange
        var task = new Task();
        var expected = "test";

        // Act
        task.setDescriptionDe(expected);
        var actual = task.getDescriptionDe();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetDescriptionEn() {
        // Arrange
        var task = new Task();
        var expected = "test";

        // Act
        task.setDescriptionEn(expected);
        var actual = task.getDescriptionEn();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetDifficulty() {
        // Arrange
        var task = new Task();
        short expected = 1;

        // Act
        task.setDifficulty(expected);
        var actual = task.getDifficulty();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetMaxPoints() {
        // Arrange
        var task = new Task();
        var expected = BigDecimal.TEN;

        // Act
        task.setMaxPoints(expected);
        var actual = task.getMaxPoints();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetTaskType() {
        // Arrange
        var task = new Task();
        var expected = "sql";

        // Act
        task.setTaskType(expected);
        var actual = task.getTaskType();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetStatus() {
        // Arrange
        var task = new Task();
        var expected = TaskStatus.READY_FOR_APPROVAL;

        // Act
        task.setStatus(expected);
        var actual = task.getStatus();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetTaskGroup() {
        // Arrange
        var task = new Task();
        var expected = new TaskGroup();

        // Act
        task.setTaskGroup(expected);
        var actual = task.getTaskGroup();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetApprovedBy() {
        // Arrange
        var task = new Task();
        var expected = "user";

        // Act
        task.setApprovedBy(expected);
        var actual = task.getApprovedBy();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetApprovedDate() {
        // Arrange
        var task = new Task();
        var expected = OffsetDateTime.now();

        // Act
        task.setApprovedDate(expected);
        var actual = task.getApprovedDate();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetIsMoodleSynced() {
        // Arrange
        var task = new Task();
        var expected = true;

        // Act
        task.setMoodleSynced(expected);
        var actual = task.isMoodleSynced();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetTaskCategories() {
        // Arrange
        var task = new Task();
        var expected = new HashSet<TaskCategory>();

        // Act
        task.setTaskCategories(expected);
        var actual = task.getTaskCategories();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void testToString() {
        // Arrange
        var task = new Task();
        task.setId(10L);
        task.setTitle("test");
        task.setTaskType("sql");
        task.setStatus(TaskStatus.READY_FOR_APPROVAL);

        // Act
        var actual = task.toString();

        // Assert
        assertThat(actual).contains("10", "test", "sql", "READY_FOR_APPROVAL");
    }
}
