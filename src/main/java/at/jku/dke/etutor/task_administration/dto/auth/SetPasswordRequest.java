package at.jku.dke.etutor.task_administration.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Data required for activating an account/resetting a password.
 *
 * @param token                The activation token.
 * @param password             The password.
 * @param passwordConfirmation The password confirmation.
 */
public record SetPasswordRequest(@NotEmpty String token,
                                 @Size(min = 6, max = 64) @NotEmpty String password,
                                 @NotEmpty String passwordConfirmation) {
    @AssertTrue(message = "{jakarta.validation.constraints.ValueEquals.message}")
    private boolean isValid() {
        return this.password != null && this.password.equals(this.passwordConfirmation);
    }
}
