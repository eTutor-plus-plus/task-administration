package at.jku.dke.etutor.task_administration.dto.auth;

import at.jku.dke.etutor.task_administration.validation.ValuesEquals;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Data required for activating an account/resetting a password.
 *
 * @param token                The activation token.
 * @param password             The password.
 * @param passwordConfirmation The password confirmation.
 */
@ValuesEquals(field1 = "password", field2 = "passwordConfirmation")
public record SetPasswordRequest(@NotEmpty String token,
                                 @Size(min = 6, max = 64) @NotEmpty String password,
                                 @NotEmpty String passwordConfirmation) {
}
