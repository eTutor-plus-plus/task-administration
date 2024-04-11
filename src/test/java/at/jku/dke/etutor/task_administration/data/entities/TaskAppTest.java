package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TaskAppTest {

    @Test
    void getSetTaskType() {
        // Arrange
        var taskType = new TaskApp();
        var expected = "test";

        // Act
        taskType.setTaskType(expected);
        var actual = taskType.getTaskType();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetUrl() {
        // Arrange
        var url = new TaskApp();
        var expected = "test";

        // Act
        url.setUrl(expected);
        var actual = url.getUrl();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetApiKey() {
        // Arrange
        var apiKey = new TaskApp();
        var expected = "test";

        // Act
        apiKey.setApiKey(expected);
        var actual = apiKey.getApiKey();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetSubmissionPrefix() {
        // Arrange
        var submissionPrefix = new TaskApp();
        var expected = "test";

        // Act
        submissionPrefix.setSubmissionPrefix(expected);
        var actual = submissionPrefix.getSubmissionPrefix();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetTaskGroupPrefix() {
        // Arrange
        var taskGroupPrefix = new TaskApp();
        var expected = "test";

        // Act
        taskGroupPrefix.setTaskGroupPrefix(expected);
        var actual = taskGroupPrefix.getTaskGroupPrefix();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetTaskPrefix() {
        // Arrange
        var taskPrefix = new TaskApp();
        var expected = "test";

        // Act
        taskPrefix.setTaskPrefix(expected);
        var actual = taskPrefix.getTaskPrefix();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void testToString() {
        // Arrange
        var taskApp = new TaskApp();
        taskApp.setId(99L);
        taskApp.setTaskType("sql");
        taskApp.setUrl("http://localhost:8080");

        // Act
        var actual = taskApp.toString();

        // Assert
        assertThat(actual).contains("99", "sql", "http://localhost:8080");
    }
}
