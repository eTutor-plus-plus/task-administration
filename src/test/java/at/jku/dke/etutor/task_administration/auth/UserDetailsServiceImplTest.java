package at.jku.dke.etutor.task_administration.auth;

import at.jku.dke.etutor.task_administration.data.entities.User;
import at.jku.dke.etutor.task_administration.data.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest {

    @Test
    void loadUserByUsername_notExistingUser_throwsException() {
        // Arrange
        final String username = "username";
        var repository = mock(UserRepository.class);
        var loginAttemptService = mock(LoginAttemptService.class);

        when(repository.findByUsernameIgnoreCaseAndFetchOrganizationalUnits(username)).thenReturn(Optional.empty());
        when(loginAttemptService.isBlocked()).thenReturn(false);

        var service = new UserDetailsServiceImpl(repository, loginAttemptService);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(username));
    }

    @Test
    void loadUserByUsername_notExistingUserAndIpBlocked_throwsException() {
        // Arrange
        final String username = "username";
        var repository = mock(UserRepository.class);
        var loginAttemptService = mock(LoginAttemptService.class);

        when(repository.findByUsernameIgnoreCaseAndFetchOrganizationalUnits(username)).thenReturn(Optional.empty());
        when(loginAttemptService.isBlocked()).thenReturn(true);

        var service = new UserDetailsServiceImpl(repository, loginAttemptService);

        // Act & Assert
        assertThrows(LockedException.class, () -> service.loadUserByUsername(username));
    }

    @Test
    void loadUserByUsername_existingUserAndIpBlocked_throwsException() {
        // Arrange
        final String username = "username";
        var repository = mock(UserRepository.class);
        var loginAttemptService = mock(LoginAttemptService.class);

        when(repository.findByUsernameIgnoreCaseAndFetchOrganizationalUnits(username)).thenReturn(Optional.of(new User()));
        when(loginAttemptService.isBlocked()).thenReturn(true);

        var service = new UserDetailsServiceImpl(repository, loginAttemptService);

        // Act & Assert
        assertThrows(LockedException.class, () -> service.loadUserByUsername(username));
    }

    @Test
    void loadUserByUsername_existing_returnUserDetails() {
        // Arrange
        final String username = "username";
        var user = new User();
        user.setUsername(username);

        var repository = mock(UserRepository.class);
        var loginAttemptService = mock(LoginAttemptService.class);

        when(repository.findByUsernameIgnoreCaseAndFetchOrganizationalUnits(username)).thenReturn(Optional.of(user));
        when(loginAttemptService.isBlocked()).thenReturn(false);

        var service = new UserDetailsServiceImpl(repository, loginAttemptService);

        // Act
        var result = service.loadUserByUsername(username);

        // Assert
        assertEquals(username, result.getUsername());
    }
}
