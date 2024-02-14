package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.TaskCategory;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link at.jku.dke.etutor.task_administration.data.entities.TaskCategory}.
 *
 * @param id                   The id of the category.
 * @param name                 The name of the category.
 * @param parentId             The id of the parent category.
 * @param organizationalUnitId The id of the organizational unit.
 * @param moodleSynced         Whether the task category is synced with moodle.
 * @param createdBy            The creation user.
 * @param createdDate          The creation date.
 * @param lastModifiedBy       The modification user.
 * @param lastModifiedDate     The modification date.
 */
public record TaskCategoryDto(@NotNull Long id,
                              @NotNull String name,
                              Long parentId,
                              @NotNull Long organizationalUnitId,
                              @NotNull boolean moodleSynced,
                              String createdBy,
                              Instant createdDate,
                              String lastModifiedBy,
                              Instant lastModifiedDate) implements Serializable {
    /**
     * Creates a new instance of class {@link TaskCategoryDto} based on an existing {@link TaskCategory}.
     *
     * @param category The category to copy.
     */
    public TaskCategoryDto(TaskCategory category) {
        this(category.getId(),
            category.getName(),
            category.getParent() == null ? null : category.getParent().getId(),
            category.getOrganizationalUnit().getId(),
            category.getMoodleId() != null,
            category.getCreatedBy(),
            category.getCreatedDate(),
            category.getLastModifiedBy(),
            category.getLastModifiedDate());
    }
}
