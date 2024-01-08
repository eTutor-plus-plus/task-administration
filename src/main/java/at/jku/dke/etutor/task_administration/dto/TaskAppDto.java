package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.TaskApp;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

/**
 * View-DTO for {@link TaskApp}
 *
 * @param id               The identifier.
 * @param taskType         The supported task type.
 * @param url              The URL of the task app.
 * @param apiKey           The API key of the task app.
 * @param createdBy        The creation user.
 * @param createdDate      The creation date.
 * @param lastModifiedBy   The modification user.
 * @param lastModifiedDate The modification date.
 */
public record TaskAppDto(@NotNull Long id,
                         @NotNull String taskType,
                         @NotNull String url,
                         String apiKey,
                         String createdBy,
                         Instant createdDate,
                         String lastModifiedBy,
                         Instant lastModifiedDate) implements Serializable {
    /**
     * Creates a new instance of class {@link TaskAppDto} based on an existing {@link TaskApp}.
     *
     * @param app The app to copy.
     */
    public TaskAppDto(TaskApp app) {
        this(app.getId(), app.getTaskType(), app.getUrl(), app.getApiKey(), app.getCreatedBy(), app.getCreatedDate(), app.getLastModifiedBy(), app.getLastModifiedDate());
    }
}
