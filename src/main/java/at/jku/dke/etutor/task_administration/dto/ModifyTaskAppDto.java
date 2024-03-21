package at.jku.dke.etutor.task_administration.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

/**
 * Modification-DTO for {@link at.jku.dke.etutor.task_administration.data.entities.TaskApp}
 *
 * @param taskType         The supported task type.
 * @param url              The URL of the task app.
 * @param apiKey           The API key of the task app.
 * @param submissionPrefix The prefix for the submission URL.
 * @param taskGroupPrefix  The prefix for the task group URL.
 * @param taskPrefix       The prefix for the task URL.
 */
public record ModifyTaskAppDto(@NotEmpty @Size(max = 100) String taskType,
                               @NotEmpty @Size(max = 255) @URL() String url,
                               @Size(max = 255) String apiKey,
                               @Size(max = 50) String taskPrefix,
                               @Size(max = 50) String taskGroupPrefix,
                               @Size(max = 50) String submissionPrefix) implements Serializable {
}
