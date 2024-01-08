package at.jku.dke.etutor.task_administration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Modification-DTO for {@link at.jku.dke.etutor.task_administration.data.entities.User}.
 *
 * @param username            The username.
 * @param firstName           The first name.
 * @param lastName            The last name.
 * @param email               The email address.
 * @param enabled             Whether the user is active.
 * @param activated           The timestamp of the user activation.
 * @param fullAdmin           Whether the user is full administrator.
 * @param lockoutEnd          The end of the user lock.
 * @param organizationalUnits The organizational units the user belongs to.
 */
public record ModifyUserDto(@NotEmpty @Size(max = 50) String username,
                            @NotEmpty @Size(max = 100) String firstName,
                            @NotEmpty @Size(max = 100) String lastName,
                            @NotEmpty @Size(max = 255) @Email String email,
                            @NotNull boolean enabled,
                            OffsetDateTime activated,
                            @NotNull boolean fullAdmin,
                            OffsetDateTime lockoutEnd,
                            @NotNull Set<OrganizationalUnitRoleAssignmentDto> organizationalUnits) implements Serializable {
}
