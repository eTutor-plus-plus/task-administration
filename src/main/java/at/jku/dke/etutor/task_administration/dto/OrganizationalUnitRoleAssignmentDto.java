package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.UserRole;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnitUser}.
 *
 * @param organizationalUnit The organizational unit.
 * @param role               The role the user has assigned in the unit.
 */
public record OrganizationalUnitRoleAssignmentDto(@NotNull long organizationalUnit, @NotNull UserRole role) {
}
