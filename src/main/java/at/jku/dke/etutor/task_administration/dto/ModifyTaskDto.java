package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.TaskStatus;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Modification-DTO for {@link at.jku.dke.etutor.task_administration.data.entities.Task}.
 *
 * @param organizationalUnitId The ID of the organizational unit.
 * @param title                The title of the task.
 * @param descriptionDe        The description of the task in German.
 * @param descriptionEn        The description of the task in English.
 * @param difficulty           The difficulty of the task.
 * @param maxPoints            The maximum points of the task.
 * @param taskType             The type of the task.
 * @param status               The status of the task.
 * @param taskGroupId          The ID of the task group.
 * @param taskCategoryIds      The IDs of the task categories.
 * @param additionalData       The additional data of the task.
 */
public record ModifyTaskDto(@NotNull Long organizationalUnitId,
                            @NotEmpty @Size(max = 100) String title,
                            @NotNull String descriptionDe,
                            @NotNull String descriptionEn,
                            @NotNull @Min(1) @Max(4) Short difficulty,
                            @PositiveOrZero @NotNull BigDecimal maxPoints,
                            @NotEmpty @Size(max = 100) String taskType,
                            @NotNull TaskStatus status,
                            Long taskGroupId,
                            Set<Long> taskCategoryIds,
                            Map<String, Object> additionalData) implements Serializable {
}
