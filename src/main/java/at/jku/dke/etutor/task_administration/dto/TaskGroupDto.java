package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.TaskGroup;
import at.jku.dke.etutor.task_administration.data.entities.TaskStatus;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * View-DTO for {@link TaskGroup}
 *
 * @param id                   The ID of the task group.
 * @param name                 The name of the task group.
 * @param descriptionDe        The description of the task group in German.
 * @param descriptionEn        The description of the task group in English.
 * @param taskGroupType        The type of the task group.
 * @param status               The status of the task group.
 * @param organizationalUnitId The ID of the organizational unit.
 * @param createdBy            The creation user.
 * @param createdDate          The creation date.
 * @param lastModifiedBy       The modification user.
 * @param lastModifiedDate     The modification date.
 * @param approvedBy           The user who approved the task group.
 * @param approvedDate         The date when the task group was approved.
 */
public record TaskGroupDto(@NotNull Long id, @NotNull String name, @NotNull String descriptionDe, @NotNull String descriptionEn,
                           @NotNull String taskGroupType, @NotNull TaskStatus status, @NotNull Long organizationalUnitId, String createdBy, Instant createdDate,
                           String lastModifiedBy, Instant lastModifiedDate, String approvedBy, OffsetDateTime approvedDate) implements Serializable {
    /**
     * Creates a new instance of class {@link TaskGroupDto} based on an existing {@link TaskGroup}.
     *
     * @param group The group to copy.
     */
    public TaskGroupDto(TaskGroup group) {
        this(group.getId(),
            group.getName(),
            group.getDescriptionDe(),
            group.getDescriptionEn(),
            group.getTaskGroupType(),
            group.getStatus(),
            group.getOrganizationalUnit().getId(),
            group.getCreatedBy(),
            group.getCreatedDate(),
            group.getLastModifiedBy(),
            group.getLastModifiedDate(),
            group.getApprovedBy(),
            group.getApprovedDate());
    }
}
