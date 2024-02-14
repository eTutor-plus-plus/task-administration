package at.jku.dke.etutor.task_administration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.Map;

/**
 * Data transfer object for submitting a submission.
 *
 * @param taskId        The task identifier.
 * @param language      The language of the submitters' user interface (either "de" or "en").
 * @param mode          The submission mode.
 * @param feedbackLevel The feedback level.
 * @param submission    The submission.
 */
@Schema(description = "Data for a submission that should be evaluated and graded")
public record SubmitSubmissionDto(
    @NotNull Long taskId,
    @NotNull @Size(min = 2, max = 2) @Pattern(regexp = "de|en") String language,
    @NotNull String mode,
    @NotNull @Min(0) @Max(3) Integer feedbackLevel,
    @NotNull @Valid Map<String, Serializable> submission
) implements Serializable {
}
