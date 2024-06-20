package at.jku.dke.etutor.task_administration.services;

import at.jku.dke.etutor.task_administration.data.entities.TokenType;
import at.jku.dke.etutor.task_administration.data.entities.User;
import at.jku.dke.etutor.task_administration.data.entities.UserToken;
import at.jku.dke.etutor.task_administration.data.repositories.UserRepository;
import at.jku.dke.etutor.task_administration.data.repositories.UserTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Service for user account self-service.
 */
@Service
public class AccountService {
    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final MailService mailService;
    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link AccountService}.
     *
     * @param userRepository      The user repository.
     * @param userTokenRepository The user token repository.
     * @param mailService         The mail service.
     * @param messageSource       The message source.
     */

    public AccountService(UserRepository userRepository, UserTokenRepository userTokenRepository, MailService mailService, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.userTokenRepository = userTokenRepository;
        this.mailService = mailService;
        this.messageSource = messageSource;
    }

    //#region --- Reset Password ---

    /**
     * Requests a token for resetting the password.
     * If no user exists, this method will do nothing.
     *
     * @param username The username of the user.
     * @param locale   The locale of the user.
     */
    @Transactional
    public void requestResetPasswordToken(String username, Locale locale) {
        User user = this.userRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (user == null) {
            LOG.warn("Someone tried to reset password for not existing user '{}'", username);
            return;
        }
        if (!user.getEnabled()) {
            LOG.warn("Someone tried to reset password for disabled user '{}'", username);
            return;
        }
        if (user.getActivatedDate() == null || user.getActivatedDate().isAfter(OffsetDateTime.now())) {
            LOG.warn("Someone tried to reset password for not activated user '{}'", username);
            return;
        }

        // Get token count
        var tokenCount = userTokenRepository.countActivePasswordResetTokensForUser(user.getId());
        if (tokenCount > 5) {
            LOG.warn("User {} requested password reset mor than 5 times in a short time", username);
            return;
        }

        // Generate token
        LOG.info("Generating token for user '{}'.", user.getUsername());
        String token = RandomService.INSTANCE.randomString(50);
        var expiration = OffsetDateTime.now().plusMinutes(15);
        var userToken = new UserToken(TokenType.RESET_PASSWORD, user, token, expiration);

        // Save token
        this.userTokenRepository.save(userToken);

        // Send token mail
        LOG.debug("Sending reset password email to user '{}'.", user.getUsername());
        this.mailService.sendMail(user.getEmail(),
            this.messageSource.getMessage("forgotPassword.mail.subject", null, locale),
            this.messageSource.getMessage("forgotPassword.mail.text", new Object[]{
                String.format("%s %s", user.getFirstName(), user.getLastName()),
                user.getUsername(),
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(),
                token
            }, locale));
    }

    /**
     * Resets the password for the user with the given token.
     *
     * @param token    The token.
     * @param password The new password.
     */
    @Transactional
    public void resetPassword(String token, String password) {
        // find token
        var userToken = this.userTokenRepository.findByToken(token).orElseThrow(() -> new ValidationException("Invalid token."));

        // check if token is expired
        if (userToken.getType() != TokenType.RESET_PASSWORD) {
            LOG.warn("User {} tried to use token of type {} to reset password.", userToken.getUser().getId(), userToken.getType());
            throw new ValidationException("Invalid token.");
        }
        if (userToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            LOG.warn("User {} tried to reset password with expired token.", userToken.getUser().getId());
            throw new ValidationException("Token expired.");
        }

        // update password
        var user = userToken.getUser();
        LOG.info("Resetting password for user {}", user.getUsername());
        user.setPassword(password);
        this.userRepository.save(user);

        // delete token
        LOG.debug("Deleting used reset token for user {}", user.getUsername());
        this.userTokenRepository.delete(userToken);
    }

    //#endregion

    //#region --- Change Password ---

    /**
     * Changes the password for the given user.
     *
     * @param username        The username.
     * @param currentPassword The current password.
     * @param password        The new password.
     */
    @Transactional
    public void changePassword(String username, String currentPassword, String password) {
        User user = this.userRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!PasswordEncoderFactories.createDelegatingPasswordEncoder().matches(currentPassword, user.getPasswordHash()))
            throw new ValidationException("Invalid current password.");

        LOG.info("Changing password for user {}", user.getUsername());
        user.setPassword(password);
        this.userRepository.save(user);
    }

    //#endregion

    //#region --- Activate ---

    /**
     * Activates the account for the given token.
     *
     * @param token    The token.
     * @param password The password.
     */
    @Transactional
    public void activateAccount(String token, String password) {
        // find token
        var userToken = this.userTokenRepository.findByToken(token).orElseThrow(() -> new ValidationException("Invalid token."));

        // check if token is expired
        if (userToken.getType() != TokenType.ACTIVATE_ACCOUNT) {
            LOG.warn("User {} tried to use token of type {} to activate account.", userToken.getUser().getId(), userToken.getType());
            throw new ValidationException("Invalid token.");
        }
        if (userToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            LOG.warn("User {} tried to activate account with expired token.", userToken.getUser().getId());
            throw new ValidationException("Token expired.");
        }

        var user = userToken.getUser();
        if (user.getActivatedDate() != null)
            throw new ValidationException("Account already activated.");

        // update password
        LOG.info("Activating account for user {}", user.getUsername());
        user.setPassword(password);
        user.setActivatedDate(OffsetDateTime.now());
        this.userRepository.save(user);

        // delete token
        LOG.debug("Deleting used activation token for user {}", user.getUsername());
        this.userTokenRepository.delete(userToken);
    }

    //#endregion

    /**
     * Deletes all obsolete reset password tokens.
     */
    @Transactional
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void deleteObsoleteTokens() {
        LOG.info("Deleting obsolete tokens");
        this.userTokenRepository.deleteByExpiresAt(OffsetDateTime.now());
    }
}

