package at.jku.dke.etutor.task_administration.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * Modification-DTO for {@link at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit}
 *
 * @param name The name.
 */
public record ModifyOrganizationalUnitDto(@NotEmpty @Size(max = 255) String name) implements Serializable {
}
