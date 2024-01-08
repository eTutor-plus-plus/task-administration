package at.jku.dke.etutor.task_administration.dto.auth;

import jakarta.validation.constraints.NotEmpty;

/**
 * Data required for user authentication.
 *
 * @param username The username.
 * @param password The password.
 */
public record LoginRequest(@NotEmpty String username, @NotEmpty String password) {
}
