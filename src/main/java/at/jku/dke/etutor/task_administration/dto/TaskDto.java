package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.AuditedEntity;
import at.jku.dke.etutor.task_administration.data.entities.Task;
import at.jku.dke.etutor.task_administration.data.entities.TaskCategory;
import at.jku.dke.etutor.task_administration.data.entities.TaskStatus;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * View-DTO for {@link Task}
 *
 * @param id                   The ID of the task.
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
 * @param createdBy            The creation user.
 * @param createdDate          The creation date.
 * @param lastModifiedBy       The modification user.
 * @param lastModifiedDate     The modification date.
 * @param approvedBy           The user who approved the task.
 * @param approvedDate         The date when the task was approved.
 */
public record TaskDto(@NotNull Long id, @NotNull Long organizationalUnitId, @NotNull String title, @NotNull String descriptionDe, @NotNull String descriptionEn,
                      @NotNull Short difficulty, @NotNull BigDecimal maxPoints, @NotNull String taskType, @NotNull TaskStatus status, Long taskGroupId, Set<Long> taskCategoryIds, boolean moodleSynced,
                      String createdBy, Instant createdDate, String lastModifiedBy, Instant lastModifiedDate, String approvedBy,
                      OffsetDateTime approvedDate) implements Serializable {
    /**
     * Creates a new instance of class {@link TaskDto} based on an existing {@link Task}.
     *
     * @param task The task to copy.
     */
    public TaskDto(Task task) {
        this(task.getId(),
            task.getOrganizationalUnit().getId(),
            task.getTitle(),
            task.getDescriptionDe(),
            task.getDescriptionEn(),
            task.getDifficulty(),
            task.getMaxPoints(),
            task.getTaskType(),
            task.getStatus(),
            task.getTaskGroup() != null ? task.getTaskGroup().getId() : null,
            null, // prevent lazy loading of not required categories
            task.getIsMoodleSynced(),
            task.getCreatedBy(),
            task.getCreatedDate(),
            task.getLastModifiedBy(),
            task.getLastModifiedDate(),
            task.getApprovedBy(),
            task.getApprovedDate());

    }

    /**
     * Creates a new instance of class {@link TaskDto} based on an existing {@link Task}.
     *
     * @param task           The task to copy.
     * @param taskCategories The task categories.
     */
    public TaskDto(Task task, Set<TaskCategory> taskCategories) {
        this(task.getId(),
            task.getOrganizationalUnit().getId(),
            task.getTitle(),
            task.getDescriptionDe(),
            task.getDescriptionEn(),
            task.getDifficulty(),
            task.getMaxPoints(),
            task.getTaskType(),
            task.getStatus(),
            task.getTaskGroup() != null ? task.getTaskGroup().getId() : null,
            taskCategories != null ? taskCategories.stream().map(AuditedEntity::getId).collect(Collectors.toSet()) : null,
            task.getIsMoodleSynced(),
            task.getCreatedBy(),
            task.getCreatedDate(),
            task.getLastModifiedBy(),
            task.getLastModifiedDate(),
            task.getApprovedBy(),
            task.getApprovedDate());
    }
}
