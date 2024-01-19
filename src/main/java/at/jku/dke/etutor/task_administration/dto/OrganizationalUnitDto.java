package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

/**
 * View-DTO for {@link OrganizationalUnit}
 *
 * @param id               The identifier.
 * @param name             The name.
 * @param moodleSynced     Whether the organizational unit is synced with moodle.
 * @param createdBy        The creation user.
 * @param createdDate      The creation date.
 * @param lastModifiedBy   The modification user.
 * @param lastModifiedDate The modification date.
 */
public record OrganizationalUnitDto(@NotNull Long id,
                                    @NotNull String name,
                                    @NotNull boolean moodleSynced,
                                    String createdBy,
                                    Instant createdDate,
                                    String lastModifiedBy,
                                    Instant lastModifiedDate) implements Serializable {
    /**
     * Creates a new instance of class {@link OrganizationalUnitDto} based on an existing {@link OrganizationalUnit}.
     *
     * @param ou The organizational unit.
     */
    public OrganizationalUnitDto(OrganizationalUnit ou) {
        this(ou.getId(), ou.getName(), ou.getMoodleId() != null, ou.getCreatedBy(), ou.getCreatedDate(), ou.getLastModifiedBy(), ou.getLastModifiedDate());
    }
}
