package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTokenTest {

    @Test
    void testGetSetId() {
        // Arrange
        var userToken = new UserToken();
        final long value = 2;

        // Act
        userToken.setId(value);
        var result = userToken.getId();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetType() {
        // Arrange
        var userToken = new UserToken();
        final TokenType value = TokenType.RESET_PASSWORD;

        // Act
        userToken.setType(value);
        var result = userToken.getType();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetUser() {
        // Arrange
        var userToken = new UserToken();
        final User value = new User();

        // Act
        userToken.setUser(value);
        var result = userToken.getUser();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetToken() {
        // Arrange
        var userToken = new UserToken();
        final String value = "token";

        // Act
        userToken.setToken(value);
        var result = userToken.getToken();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetExpiresAt() {
        // Arrange
        var userToken = new UserToken();
        final OffsetDateTime value = OffsetDateTime.now();

        // Act
        userToken.setExpiresAt(value);
        var result = userToken.getExpiresAt();

        // Assert
        assertEquals(value, result);
    }
}
