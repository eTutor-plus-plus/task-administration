package at.jku.dke.etutor.task_administration.controllers;

import at.jku.dke.etutor.task_administration.auth.JwtService;
import at.jku.dke.etutor.task_administration.auth.LoginAttemptService;
import at.jku.dke.etutor.task_administration.auth.SecurityHelpers;
import at.jku.dke.etutor.task_administration.dto.auth.ChangePasswordDto;
import at.jku.dke.etutor.task_administration.dto.auth.LoginRequest;
import at.jku.dke.etutor.task_administration.dto.auth.LoginResponse;
import at.jku.dke.etutor.task_administration.dto.auth.SetPasswordRequest;
import at.jku.dke.etutor.task_administration.services.AccountService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

/**
 * Controller for user authentication.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication")
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final JwtService jwtService;
    private final AccountService accountService;

    /**
     * Creates a new instance of class {@link AuthController}.
     *
     * @param authenticationManager The authentication manager.
     * @param loginAttemptService   The login attempt service.
     * @param jwtService            The JWT service.
     * @param accountService        The account service.
     */
    public AuthController(AuthenticationManager authenticationManager, LoginAttemptService loginAttemptService, JwtService jwtService, AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
        this.jwtService = jwtService;
        this.accountService = accountService;
    }

    /**
     * Login a user.
     *
     * @param loginRequest The login data.
     * @return The access token.
     */
    @PostMapping(value = "login", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful login", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Invalid login data", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            if (this.loginAttemptService.isBlocked())
                throw new LockedException("Client is locked");

            var authRequest = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.username(), loginRequest.password());
            var authResponse = this.authenticationManager.authenticate(authRequest);

            LOG.info("Authentication of user '{}' succeeded", loginRequest.username());
            this.loginAttemptService.loginSucceeded(loginRequest.username());
            return this.jwtService.createToken(authResponse.getName(), this.loginAttemptService.getClientIP());
        } catch (DisabledException | LockedException | BadCredentialsException ex) {
            LOG.warn("Authentication of user '{}' failed", loginRequest.username());
            this.loginAttemptService.loginFailed(loginRequest.username());
            throw ex;
        }
    }

    /**
     * Login a user.
     *
     * @param loginRequest The login data.
     * @return The access token.
     */
    @Hidden
    @Profile("dev")
    @PostMapping(value = "login-swagger", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public LoginResponse loginSwaggerUI(@Valid LoginRequest loginRequest) {
        return this.login(loginRequest);
    }

    /**
     * Creates a new token for an already logged-in user.
     *
     * @param refreshToken The refresh token.
     * @return The new access token.
     */
    @PostMapping(value = "refresh", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.TEXT_PLAIN_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful refresh", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public LoginResponse refresh(@RequestBody String refreshToken) {
        return this.jwtService.refreshToken(refreshToken, this.loginAttemptService.getClientIP());
    }

    /**
     * Activates an account.
     *
     * @param data The activation data.
     */
    @PostMapping(value = "activate", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successful account activation"),
        @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public void activateAccount(@Valid @RequestBody SetPasswordRequest data) {
        this.accountService.activateAccount(data.token(), data.password());
    }

    /**
     * Requests a reset password token.
     *
     * @param username The username of the user for which request a token.
     */
    @GetMapping(value = "reset-password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Reset request successful"),
        @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public void requestResetToken(@NotEmpty @RequestParam String username, @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") String language) {
        this.accountService.requestResetPasswordToken(username, Locale.of(language)); // TODO: add request throttling
    }

    /**
     * Resets the password.
     *
     * @param data The reset data.
     */
    @PostMapping(value = "reset-password", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successful password reset"),
        @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public void resetPassword(@Valid @RequestBody SetPasswordRequest data) {
        this.accountService.resetPassword(data.token(), data.password());
    }

    /**
     * Changes the password.
     *
     * @param data The password data.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "change-password", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successful password change"),
        @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content(schema = @Schema(implementation = ProblemDetail.class), mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    public void changePassword(@Valid @RequestBody ChangePasswordDto data) {
        this.accountService.changePassword(SecurityHelpers.getUserName(), data.currentPassword(), data.password());
    }

    /**
     * Gets the JWK set.
     *
     * @return The JWK set.
     */
    @GetMapping(value = "jwk", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<String, Object> getJwkKeySet() {
        return this.jwtService.getJWKSet();
    }
}
