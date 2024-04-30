package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.data.entities.TokenType;
import at.jku.dke.etutor.task_administration.data.entities.User;
import at.jku.dke.etutor.task_administration.data.entities.UserToken;
import at.jku.dke.etutor.task_administration.data.repositories.UserRepository;
import at.jku.dke.etutor.task_administration.data.repositories.UserTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Test
    void requestResetPasswordToken_valid() {
        // Arrange
        final String username = "my-user";
        var user = new User();
        user.setUsername(username);
        user.setActivatedDate(OffsetDateTime.now());
        user.setEnabled(true);
        user.setEmail("test@example.com");

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var mailService = mock(MailService.class);
        var ms = mock(MessageSource.class);
        var service = new AccountService(userRepo, tokenRepo, mailService, ms);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mock(HttpServletRequest.class)));

        when(userRepo.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));
        when(ms.getMessage(any(), any(), any())).thenReturn("some string");

        // Act
        service.requestResetPasswordToken(username, Locale.GERMAN);

        // Assert
        verify(tokenRepo, times(1)).save(any(UserToken.class));
        verify(mailService, times(1)).sendMail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void requestResetPasswordToken_notActivatedUser() {
        // Arrange
        final String username = "my-user";
        var user = new User();
        user.setUsername(username);
        user.setActivatedDate(null);
        user.setEnabled(true);
        user.setEmail("test@example.com");

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var mailService = mock(MailService.class);
        var ms = mock(MessageSource.class);
        var service = new AccountService(userRepo, tokenRepo, mailService, ms);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mock(HttpServletRequest.class)));

        when(userRepo.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));
        when(ms.getMessage(any(), any(), any())).thenReturn("some string");

        // Act
        service.requestResetPasswordToken(username, Locale.GERMAN);

        // Assert
        verify(tokenRepo, never()).save(any(UserToken.class));
        verify(mailService, never()).sendMail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void requestResetPasswordToken_disabledUser() {
        // Arrange
        final String username = "my-user";
        var user = new User();
        user.setUsername(username);
        user.setActivatedDate(OffsetDateTime.now());
        user.setEnabled(false);
        user.setEmail("test@example.com");

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var mailService = mock(MailService.class);
        var ms = mock(MessageSource.class);
        var service = new AccountService(userRepo, tokenRepo, mailService, ms);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mock(HttpServletRequest.class)));

        when(userRepo.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));
        when(ms.getMessage(any(), any(), any())).thenReturn("some string");

        // Act
        service.requestResetPasswordToken(username, Locale.GERMAN);

        // Assert
        verify(tokenRepo, never()).save(any(UserToken.class));
        verify(mailService, never()).sendMail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void requestResetPasswordToken_invalidUser() {
        // Arrange
        final String username = "my-user";
        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var mailService = mock(MailService.class);
        var ms = mock(MessageSource.class);
        var service = new AccountService(userRepo, tokenRepo, mailService, ms);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mock(HttpServletRequest.class)));

        when(userRepo.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());
        when(ms.getMessage(any(), any(), any())).thenReturn("some string");

        // Act
        service.requestResetPasswordToken(username, Locale.GERMAN);

        // Assert
        verify(tokenRepo, never()).save(any(UserToken.class));
        verify(mailService, never()).sendMail(anyString(), anyString(), anyString());
    }

    @Test
    void resetPassword_valid() {
        // Arrange
        final String token = "test-token";
        final String pwd = "my-password";
        var user = new User();
        user.setPassword("test");
        var oldPwdHash = user.getPasswordHash();
        var userToken = new UserToken(TokenType.RESET_PASSWORD, user, token, OffsetDateTime.MAX);

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, tokenRepo, null, null);

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(userToken));

        // Act
        service.resetPassword(token, pwd);

        // Assert
        assertNotEquals(oldPwdHash, user.getPasswordHash());
        verify(userRepo, times(1)).save(user);
        verify(tokenRepo, times(1)).delete(userToken);
    }

    @Test
    void resetPassword_invalidToken() {
        // Arrange
        final String token = "test-token";
        final String pwd = "my-password";
        var user = new User();
        user.setPassword("test");

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, tokenRepo, null, null);

        when(tokenRepo.findByToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ValidationException.class, () -> service.resetPassword(token, pwd));
    }

    @Test
    void resetPassword_invalidTokenType() {
        // Arrange
        final String token = "test-token";
        final String pwd = "my-password";
        var user = new User();
        user.setPassword("test");
        var userToken = new UserToken(TokenType.ACTIVATE_ACCOUNT, user, token, OffsetDateTime.MAX);

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, tokenRepo, null, null);

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(userToken));

        // Act & Assert
        assertThrows(ValidationException.class, () -> service.resetPassword(token, pwd));
    }

    @Test
    void resetPassword_expiredToken() {
        // Arrange
        final String token = "test-token";
        final String pwd = "my-password";
        var user = new User();
        user.setPassword("test");
        var userToken = new UserToken(TokenType.RESET_PASSWORD, user, token, OffsetDateTime.MIN);

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, tokenRepo, null, null);

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(userToken));

        // Act & Assert
        assertThrows(ValidationException.class, () -> service.resetPassword(token, pwd));
    }

    @Test
    void changePassword_valid() {
        // Arrange
        final String username = "myuser";
        final String currentPassword = "mypassword";
        final String newPassword = "newpassword";

        var user = new User();
        user.setUsername("myuser");
        user.setPassword(currentPassword);
        var oldPwdHash = user.getPasswordHash();

        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, null, null, null);

        when(userRepo.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));

        // Act
        service.changePassword(username, currentPassword, newPassword);

        // Assert
        assertNotEquals(oldPwdHash, user.getPasswordHash());
        verify(userRepo, times(1)).save(user);
    }

    @Test
    void changePassword_invalidPassword() {
        // Arrange
        final String username = "myuser";
        final String currentPassword = "mypassword";
        final String newPassword = "newpassword";

        var user = new User();
        user.setUsername("myuser");
        user.setPassword(currentPassword);

        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, null, null, null);

        when(userRepo.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(ValidationException.class, () -> service.changePassword(username, "invalid-pwd", newPassword));
    }

    @Test
    void changePassword_invalidUser() {
        // Arrange
        final String username = "myuser";
        final String currentPassword = "mypassword";
        final String newPassword = "newpassword";

        var user = new User();
        user.setUsername("myuser");
        user.setPassword(currentPassword);

        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, null, null, null);

        when(userRepo.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.changePassword("invalid-user", currentPassword, newPassword));
    }

    @Test
    void activateAccount_valid() {
        // Arrange
        final String token = "test-token";
        final String pwd = "my-password";
        var user = new User();
        var userToken = new UserToken(TokenType.ACTIVATE_ACCOUNT, user, token, OffsetDateTime.MAX);

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, tokenRepo, null, null);

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(userToken));

        // Act
        service.activateAccount(token, pwd);

        // Assert
        assertThat(user.getPasswordHash()).isNotEmpty();
        assertNotNull(user.getActivatedDate());
        verify(userRepo, times(1)).save(user);
        verify(tokenRepo, times(1)).delete(userToken);
    }

    @Test
    void activateAccount_invalidToken() {
        // Arrange
        final String token = "test-token";
        final String pwd = "my-password";

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, tokenRepo, null, null);

        when(tokenRepo.findByToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ValidationException.class, () -> service.activateAccount(token, pwd));
    }

    @Test
    void activateAccount_invalidTokenType() {
        // Arrange
        final String token = "test-token";
        final String pwd = "my-password";
        var user = new User();
        var userToken = new UserToken(TokenType.RESET_PASSWORD, user, token, OffsetDateTime.MAX);

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, tokenRepo, null, null);

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(userToken));

        // Act & Assert
        assertThrows(ValidationException.class, () -> service.activateAccount(token, pwd));
    }

    @Test
    void activateAccount_expiredToken() {
        // Arrange
        final String token = "test-token";
        final String pwd = "my-password";
        var user = new User();
        var userToken = new UserToken(TokenType.ACTIVATE_ACCOUNT, user, token, OffsetDateTime.MIN);

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, tokenRepo, null, null);

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(userToken));

        // Act & Assert
        assertThrows(ValidationException.class, () -> service.activateAccount(token, pwd));
    }

    @Test
    void activateAccount_userActivated() {
        // Arrange
        final String token = "test-token";
        final String pwd = "my-password";
        var user = new User();
        user.setActivatedDate(OffsetDateTime.now());
        var userToken = new UserToken(TokenType.ACTIVATE_ACCOUNT, user, token, OffsetDateTime.MAX);

        var tokenRepo = mock(UserTokenRepository.class);
        var userRepo = mock(UserRepository.class);
        var service = new AccountService(userRepo, tokenRepo, null, null);

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(userToken));

        // Act & Assert
        assertThrows(ValidationException.class, () -> service.activateAccount(token, pwd));
    }

    @Test
    void deleteObsoleteTokens() {
        // Arrange
        var repo = mock(UserTokenRepository.class);
        var service = new AccountService(null, repo, null, null);

        // Act
        service.deleteObsoleteTokens();

        // Assert
        verify(repo, times(1)).deleteByExpiresAt(any());
    }
}
