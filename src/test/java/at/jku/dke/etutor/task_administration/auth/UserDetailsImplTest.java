package at.jku.dke.etutor.task_administration.auth;

import at.jku.dke.etutor.task_administration.data.entities.*;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    @Test
    void testGetAuthoritiesEmpty() {
        // Arrange
        var user = new User();
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.getAuthorities();

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void testGetAuthorities() {
        // Arrange
        var ouUser = new OrganizationalUnitUser();
        ouUser.setRole(UserRole.INSTRUCTOR);
        ouUser.setOrganizationalUnit(new OrganizationalUnit(123L));
        ouUser.setId(new OrganizationalUnitUserId(123L, 1L));

        var user = new User();
        user.getOrganizationalUnits().add(ouUser);
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.getAuthorities();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(x -> x.getAuthority().equals(UserRole.INSTRUCTOR.name())));
    }

    @Test
    void testGetAuthoritiesFullAdmin() {
        // Arrange
        var user = new User();
        user.setFullAdmin(true);
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.getAuthorities();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(x -> x.getAuthority().equals(AuthConstants.ROLE_FULL_ADMIN)));
    }

    @Test
    void testGetPassword() {
        // Arrange
        final String value = "test";
        var user = new User();
        user.setPasswordHash(value);
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.getPassword();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testGetUsername() {
        // Arrange
        final String value = "test";
        var user = new User();
        user.setUsername(value);
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.getUsername();

        // Assert
        assertEquals(value, result);
    }

    @Test
    void testIsAccountNonExpired() {
        // Arrange
        var user = new User();
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.isAccountNonExpired();

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsAccountNonLockedNull() {
        // Arrange
        var user = new User();
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.isAccountNonLocked();

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsAccountNonLockedPast() {
        // Arrange
        var user = new User();
        user.setLockoutEnd(OffsetDateTime.now().minusDays(1));
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.isAccountNonLocked();

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsAccountNonLockedFuture() {
        // Arrange
        var user = new User();
        user.setLockoutEnd(OffsetDateTime.now().plusDays(1));
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.isAccountNonLocked();

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsCredentialsNonExpiredNull() {
        // Arrange
        var user = new User();
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.isCredentialsNonExpired();

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsCredentialsNonExpiredPast() {
        // Arrange
        var user = new User();
        user.setActivatedDate(OffsetDateTime.now().minusDays(1));
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.isCredentialsNonExpired();

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsCredentialsNonExpiredFuture() {
        // Arrange
        var user = new User();
        user.setActivatedDate(OffsetDateTime.now().plusDays(1));
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.isCredentialsNonExpired();

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsEnabled() {
        // Arrange
        var user = new User();
        user.setEnabled(false);
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.isEnabled();

        // Assert
        assertFalse(result);
    }
}
