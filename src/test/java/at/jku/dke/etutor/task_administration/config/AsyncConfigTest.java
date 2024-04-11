package at.jku.dke.etutor.task_administration.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AsyncConfigTest {

    @Test
    void taskExecutor() {
        // Arrange
        var asyncConfig = new AsyncConfig();

        // Act
        var result = asyncConfig.taskExecutor();

        // Assert
        assertNotNull(result);
    }

}
