package at.jku.dke.etutor.task_administration.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * Modification-DTO for {@link at.jku.dke.etutor.task_administration.data.entities.TaskCategory}
 *
 * @param name                 The Name of the category.
 * @param parentId             The ID of the parent category.
 * @param organizationalUnitId The ID of the organizational unit.
 */
public record ModifyTaskCategoryDto(@NotEmpty @Size(max = 100) String name,
                                    Long parentId,
                                    @NotNull Long organizationalUnitId) implements Serializable {
}
