package at.jku.dke.etutor.task_administration.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

/**
 * Modification-DTO for {@link at.jku.dke.etutor.task_administration.data.entities.TaskApp}
 *
 * @param taskType The supported task type.
 * @param url      The URL of the task app.
 * @param apiKey   The API key of the task app.
 */
public record ModifyTaskAppDto(@NotEmpty @Size(max = 100) String taskType,
                               @NotEmpty @Size(max = 255) @URL() String url,
                               @Size(max = 255) String apiKey) implements Serializable {
}
