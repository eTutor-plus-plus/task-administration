package at.jku.dke.etutor.task_administration.auth;

import at.jku.dke.etutor.task_administration.data.entities.User;
import at.jku.dke.etutor.task_administration.data.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    //#region --- loginFailed ---
    @Test
    void loginFailed_notExistingUser_increaseOnlyIpAttempt() {
        // Arrange
        final String username = "loginFailed_notExistingUser_increaseOnlyIpAttempt";
        var userRepository = Mockito.mock(UserRepository.class);
        Mockito.when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());

        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.0.2");

        var service = new LoginAttemptService(userRepository, request);

        // Act
        service.loginFailed(username);

        // Assert
        assertEquals(1, service.getFailedLoginCount());
    }

    @Test
    void loginFailed_existingUser_increaseUserAndIpAttempt() {
        // Arrange
        final String username = "loginFailed_existingUser_increaseUserAndIpAttempt";
        var user = new User();
        user.setUsername(username);

        var userRepository = Mockito.mock(UserRepository.class);
        Mockito.when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));

        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.0.3");

        var service = new LoginAttemptService(userRepository, request);

        // Act
        service.loginFailed(username);

        // Assert
        assertEquals(1, service.getFailedLoginCount());
        assertEquals(1, user.getFailedLoginCount());
    }

    @Test
    void loginFailed_tooManyAttempts_blockUser() {
        // Arrange
        final String username = "testLoginFailedBlockUser";
        var user = new User();
        user.setUsername(username);
        user.setFailedLoginCount(LoginAttemptService.USER_MAX_ATTEMPTS);

        var userRepository = Mockito.mock(UserRepository.class);
        Mockito.when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));

        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.0.4");

        var service = new LoginAttemptService(userRepository, request);

        // Act
        service.loginFailed(username);

        // Assert
        assertEquals(1, service.getFailedLoginCount());
        assertEquals(LoginAttemptService.USER_MAX_ATTEMPTS + 1, user.getFailedLoginCount());
        assertNotNull(user.getLockoutEnd());
    }

    @Test
    void loginFailed_tooManyAttempts_blockIp() {
        // Arrange
        final String username = "loginFailed_notExistingUser_increaseOnlyIpAttempt";
        var userRepository = Mockito.mock(UserRepository.class);
        Mockito.when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());

        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.0.11");

        var service = new LoginAttemptService(userRepository, request);
        for (int i = 0; i < LoginAttemptService.IP_MAX_ATTEMPTS; i++) {
            service.loginFailed(username);
        }

        // Assert
        assertEquals(LoginAttemptService.IP_MAX_ATTEMPTS, service.getFailedLoginCount());
        assertFalse(service.isBlocked());

        // Act
        service.loginFailed(username);

        // Assert
        assertEquals(LoginAttemptService.IP_MAX_ATTEMPTS + 1, service.getFailedLoginCount());
        assertTrue(service.isBlocked());
    }
    //#endregion

    //#region --- loginSucceeded ---
    @Test
    void loginSucceeded_notExistingUser_resetOnlyIpAttempts() {
        // Arrange
        final String username = "loginSucceeded_notExistingUser_resetOnlyIpAttempts";
        var userRepository = Mockito.mock(UserRepository.class);
        Mockito.when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());

        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.0.5");

        var service = new LoginAttemptService(userRepository, request);
        service.loginFailed(username);
        service.loginFailed(username);
        assertEquals(2, service.getFailedLoginCount());

        // Act
        service.loginSucceeded(username);

        // Assert
        assertEquals(0, service.getFailedLoginCount());
    }

    @Test
    void loginSucceeded_existingUser_resetUserAndIpAttempts() {
        // Arrange
        final String username = "loginSucceeded_existingUser_resetUserAndIpAttempts";
        var user = new User();
        user.setUsername(username);
        user.setLockoutEnd(OffsetDateTime.now());
        user.setFailedLoginCount(LoginAttemptService.USER_MAX_ATTEMPTS + 1);

        var userRepository = Mockito.mock(UserRepository.class);
        Mockito.when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));

        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.0.6");

        var service = new LoginAttemptService(userRepository, request);
        service.loginFailed(username);
        assertEquals(1, service.getFailedLoginCount());

        // Act
        service.loginSucceeded(username);

        // Assert
        assertEquals(0, service.getFailedLoginCount());
        assertEquals(0, user.getFailedLoginCount());
        assertNull(user.getLockoutEnd());
    }
    //#endregion

    //#region --- isBlocked ---
    @Test
    void isBlocked_maxAttemptsReached_true() {
        // Arrange
        final String username = "testIsBlocked";
        var userRepository = Mockito.mock(UserRepository.class);
        Mockito.when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());

        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.0.10");

        var service = new LoginAttemptService(userRepository, request);

        // Act & Assert
        for (int i = 0; i <= LoginAttemptService.IP_MAX_ATTEMPTS; i++) {
            assertFalse(service.isBlocked());

            service.loginFailed(username);

            assertEquals(i + 1, service.getFailedLoginCount());
        }

        assertTrue(service.isBlocked());
    }
    //#endregion

    //#region --- getClientIP ---
    @Test
    void getClientIp_fromHeader() {
        // Arrange
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.0.1");
        request.addHeader("X-Forwarded-For", "192.168.0.1,10.10.0.1");
        var service = new LoginAttemptService(Mockito.mock(UserRepository.class), request);

        // Act
        var result = service.getClientIP();

        // Assert
        assertEquals("192.168.0.1", result);
    }

    @Test
    void getClientIp_fromRemoteAddr() {
        // Arrange
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.0.1");
        var service = new LoginAttemptService(Mockito.mock(UserRepository.class), request);

        // Act
        var result = service.getClientIP();

        // Assert
        assertEquals("10.10.0.1", result);
    }
    //#endregion
}
