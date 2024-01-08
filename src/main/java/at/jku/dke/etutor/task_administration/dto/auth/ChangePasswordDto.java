package at.jku.dke.etutor.task_administration.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * DTO for changing the password.
 */
public record ChangePasswordDto(@NotEmpty String currentPassword,
                                @NotEmpty @Size(min = 6, max = 64) String password,
                                @NotEmpty String passwordConfirmation) {
    @AssertTrue(message = "{jakarta.validation.constraints.ValueEquals.message}")
    private boolean isValid() {
        return this.password != null && this.password.equals(this.passwordConfirmation);
    }
}
