package at.jku.dke.etutor.task_administration.data.entities;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testGetSetUsername() {
        // Arrange
        var user = new User();
        final String value = "name";

        // Act
        user.setUsername(value);
        var result = user.getUsername();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetFirstName() {
        // Arrange
        var user = new User();
        final String value = "name";

        // Act
        user.setFirstName(value);
        var result = user.getFirstName();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetLastName() {
        // Arrange
        var user = new User();
        final String value = "name";

        // Act
        user.setLastName(value);
        var result = user.getLastName();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetEmail() {
        // Arrange
        var user = new User();
        final String value = "name";

        // Act
        user.setEmail(value);
        var result = user.getEmail();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testIsSetActive() {
        // Arrange
        var user = new User();
        final boolean value = true;

        // Act
        user.setEnabled(value);
        var result = user.getEnabled();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetPasswordHash() {
        // Arrange
        var user = new User();
        final String value = "name";

        // Act
        user.setPasswordHash(value);
        var result = user.getPasswordHash();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testSetPassword() {
        // Arrange
        var user = new User();
        final String value = "name";

        // Act
        user.setPassword(value);

        // Assert
        assertNotNull(user.getPasswordHash());
    }

    @Test
    void testGetSetFailedLoginCount() {
        // Arrange
        var user = new User();
        final int value = 1;

        // Act
        user.setFailedLoginCount(value);
        var result = user.getFailedLoginCount();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testIsSetFullAdmin() {
        // Arrange
        var user = new User();
        final boolean value = true;

        // Act
        user.setFullAdmin(value);
        var result = user.isFullAdmin();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetActivatedDate() {
        // Arrange
        var user = new User();
        final OffsetDateTime value = OffsetDateTime.now();

        // Act
        user.setActivatedDate(value);
        var result = user.getActivatedDate();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetLockoutEnd() {
        // Arrange
        var user = new User();
        final OffsetDateTime value = OffsetDateTime.now();

        // Act
        user.setLockoutEnd(value);
        var result = user.getLockoutEnd();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetOrganizationalUnits() {
        // Arrange
        var user = new User();
        Set<OrganizationalUnitUser> value = Set.of(new OrganizationalUnitUser());

        // Act
        user.setOrganizationalUnits(value);
        var result = user.getOrganizationalUnits();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetSetTokens() {
        // Arrange
        var user = new User();
        Set<UserToken> value = Set.of(new UserToken());

        // Act
        user.setTokens(value);
        var result = user.getTokens();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testToString() {
        // Arrange
        var user = new User();
        user.setId(100L);
        user.setUsername("abcd");

        // Act
        var result = user.toString();

        // Assert
        assertTrue(result.contains("abcd"));
        assertTrue(result.contains("100"));
    }
}
