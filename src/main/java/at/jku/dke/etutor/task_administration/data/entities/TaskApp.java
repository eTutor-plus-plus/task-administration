package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.StringJoiner;

/**
 * Represents a task app.
 */
@Entity
@Table(name = "task_apps")
public class TaskApp extends AuditedEntity {
    @Size(max = 100)
    @NotNull
    @Column(name = "task_type", nullable = false, length = 100)
    private String taskType;

    @Size(max = 255)
    @NotNull
    @Column(name = "url", nullable = false)
    private String url;

    @Size(max = 255)
    @Column(name = "api_key")
    private String apiKey;

    /**
     * Creates a new instance of class {@link TaskApp}.
     */
    public TaskApp() {
    }

    /**
     * Gets the task type.
     *
     * @return The task type.
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * Sets the task type.
     *
     * @param taskType The task type.
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    /**
     * Gets the url.
     *
     * @return The url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url.
     *
     * @param url The url.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the api key.
     *
     * @return The api key.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the api key.
     *
     * @param apiKey The api key.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TaskApp.class.getSimpleName() + "[", "]")
            .add("id='" + this.getId() + "'")
            .add("taskType='" + taskType + "'")
            .add("url='" + url + "'")
            .toString();
    }
}
