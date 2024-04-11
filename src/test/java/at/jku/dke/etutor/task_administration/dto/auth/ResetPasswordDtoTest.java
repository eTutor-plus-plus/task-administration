package at.jku.dke.etutor.task_administration.dto.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResetPasswordDtoTest {

    @Test
    void constructor1() {
        // Arrange
        var expected = "test";

        // Act
        var dto = new ResetPasswordDto(expected);
        var actual = dto.getToken();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void constructor2() {
        // Arrange
        var token = "test";
        var password = "password";
        var passwordRepeat = "password";

        // Act
        var dto = new ResetPasswordDto(token, password, passwordRepeat);

        // Assert
        assertEquals(token, dto.getToken());
        assertEquals(password, dto.getPassword());
        assertEquals(passwordRepeat, dto.getPasswordRepeat());
    }

    @Test
    void getSetToken() {
        // Arrange
        var dto = new ResetPasswordDto();
        var expected = "test";

        // Act
        dto.setToken(expected);
        var actual = dto.getToken();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetPassword() {
        // Arrange
        var dto = new ResetPasswordDto();
        var expected = "test";

        // Act
        dto.setPassword(expected);
        var actual = dto.getPassword();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getSetPasswordRepeat() {
        // Arrange
        var dto = new ResetPasswordDto();
        var expected = "test";

        // Act
        dto.setPasswordRepeat(expected);
        var actual = dto.getPasswordRepeat();

        // Assert
        assertEquals(expected, actual);
    }
}
