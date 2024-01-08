package at.jku.dke.etutor.task_administration.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Modification-DTO for the password of entity {@link at.jku.dke.etutor.task_administration.data.entities.User}.
 *
 * @param password The new password.
 */
public record ModifyUserPasswordDto(@NotNull @Size(min = 6, max = 64) String password) {
}
