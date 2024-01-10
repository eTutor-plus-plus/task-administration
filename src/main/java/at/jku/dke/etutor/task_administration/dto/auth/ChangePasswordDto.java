package at.jku.dke.etutor.task_administration.dto.auth;

import at.jku.dke.etutor.task_administration.validation.ValuesEquals;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * DTO for changing the password.
 */
@ValuesEquals(field1 = "password", field2 = "passwordConfirmation")
public record ChangePasswordDto(@NotEmpty String currentPassword,
                                @NotEmpty @Size(min = 6, max = 64) String password,
                                @NotEmpty String passwordConfirmation) {
}
