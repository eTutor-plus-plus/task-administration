package at.jku.dke.etutor.task_administration.auth;

import at.jku.dke.etutor.task_administration.data.entities.*;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    @Test
    void getAuthorities_empty() {
        // Arrange
        var user = new User();
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.getAuthorities();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getAuthorities_roles_containsInstructorAuthority() {
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
        assertThat(result)
            .hasSize(1)
            .anyMatch(x -> x.getAuthority().equals(AuthConstants.ROLE_INSTRUCTOR));
    }

    @Test
    void getAuthorities_fullAdmin_containsFullAdminAuthority() {
        // Arrange
        var user = new User();
        user.setFullAdmin(true);
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.getAuthorities();

        // Assert
        assertThat(result)
            .hasSize(1)
            .anyMatch(x -> x.getAuthority().equals(AuthConstants.ROLE_FULL_ADMIN));
    }

    @Test
    void getPassword_returnPasswordHash() {
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
    void getUsername_returnUsername() {
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
    void isAccountNonExpired_returnTrue() {
        // Arrange
        var user = new User();
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.isAccountNonExpired();

        // Assert
        assertTrue(result);
    }

    @Test
    void isAccountNonLocked_nullLockoutEnd_returnTrue() {
        // Arrange
        var user = new User();
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.isAccountNonLocked();

        // Assert
        assertTrue(result);
    }

    @Test
    void isAccountNonLocked_lockoutEndPast_returnTrue() {
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
    void isAccountNonLocked_lockoutEndFuture_returnFalse() {
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
    void isCredentialsNonExpired_nullActivationDate_returnTrue() {
        // Arrange
        var user = new User();
        var userDetails = new UserDetailsImpl(user);

        // Act
        var result = userDetails.isCredentialsNonExpired();

        // Assert
        assertTrue(result);
    }

    @Test
    void isCredentialsNonExpired_pastActivationDate_returnFalse() {
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
    void isCredentialsNonExpired_futureActivationDate_returnFalse() {
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
    void isEnabled_returnEnabled() {
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
