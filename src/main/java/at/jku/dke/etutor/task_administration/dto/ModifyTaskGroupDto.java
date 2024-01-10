package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.TaskStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Map;

/**
 * Modification-DTO for {@link at.jku.dke.etutor.task_administration.data.entities.TaskGroup}
 *
 * @param name                 The name of the task group.
 * @param descriptionDe        The description of the task group in German.
 * @param descriptionEn        The description of the task group in English.
 * @param taskGroupType        The type of the task group.
 * @param status               The status of the task group.
 * @param organizationalUnitId The ID of the organizational unit.
 * @param additionalData       The additional data of the task group.
 */
public record ModifyTaskGroupDto(@NotEmpty @Size(max = 100) String name,
                                 @NotNull String descriptionDe,
                                 @NotNull String descriptionEn,
                                 @NotEmpty @Size(max = 100) String taskGroupType,
                                 @NotNull TaskStatus status,
                                 @NotNull Long organizationalUnitId,
                                 @Valid Map<String, Object> additionalData) implements Serializable {
}
