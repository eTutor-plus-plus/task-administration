package at.jku.dke.etutor.task_administration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * The feedback levels of a task in a specific task category.
 * <p>
 * The levels are sent to Moodle for the question created for the task in the category.
 *
 * @param taskCategoryId        The identifier of the task category.
 * @param submitFeedbackLevel   The feedback level for submissions in mode {@code submit} (0-3).
 * @param diagnoseFeedbackLevel The feedback level for submissions in mode {@code diagnose} (0-3).
 */
@Schema(description = "Feedback levels of a task in a task category")
public record TaskCategoryFeedbackLevelDto(@NotNull Long taskCategoryId,
                                           @NotNull @Min(0) @Max(3) Short submitFeedbackLevel,
                                           @NotNull @Min(0) @Max(3) Short diagnoseFeedbackLevel) implements Serializable {
    /**
     * The default feedback level.
     */
    public static final short DEFAULT_LEVEL = 0;

    /**
     * Creates the default feedback levels for the given category.
     *
     * @param taskCategoryId The identifier of the task category.
     * @return The default feedback levels.
     */
    public static TaskCategoryFeedbackLevelDto defaults(Long taskCategoryId) {
        return new TaskCategoryFeedbackLevelDto(taskCategoryId, DEFAULT_LEVEL, DEFAULT_LEVEL);
    }
}
