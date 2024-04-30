package at.jku.dke.etutor.task_administration.moodle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MoodleConfigTest {

    @Test
    void getSetToken() {
        // Arrange
        var config = new MoodleConfig();
        var expected = "test";

        // Act
        config.setToken(expected);
        var actual = config.getToken();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetUrl() {
        // Arrange
        var config = new MoodleConfig();
        var expected = "test";

        // Act
        config.setUrl(expected);
        var actual = config.getUrl();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetEnabled() {
        // Arrange
        var config = new MoodleConfig();
        var expected = true;

        // Act
        config.setEnabled(expected);
        var actual = config.isEnabled();

        // Assert
        assertEquals(expected, actual);
        assertFalse(config.isDisabled());
    }

}
