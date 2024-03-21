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

    @Size(max = 50)
    @Column(name = "task_prefix", length = 50)
    private String taskPrefix;

    @Size(max = 50)
    @Column(name = "task_group_prefix", length = 50)
    private String taskGroupPrefix;

    @Size(max = 50)
    @Column(name = "submission_prefix", length = 50)
    private String submissionPrefix;

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

    /**
     * Gets the submission URL prefix.
     * Required if the task app supports more than one task type.
     *
     * @return The submission prefix.
     */
    public String getSubmissionPrefix() {
        return submissionPrefix;
    }

    /**
     * Sets the submission URL prefix.
     *
     * @param submissionPrefix The submission prefix.
     */
    public void setSubmissionPrefix(String submissionPrefix) {
        this.submissionPrefix = submissionPrefix;
    }

    /**
     * Gets the task group URL prefix.
     * Required if the task app supports more than one task type.
     *
     * @return The task group prefix.
     */
    public String getTaskGroupPrefix() {
        return taskGroupPrefix;
    }

    /**
     * Sets the task group URL prefix.
     *
     * @param taskGroupPrefix The task group prefix.
     */
    public void setTaskGroupPrefix(String taskGroupPrefix) {
        this.taskGroupPrefix = taskGroupPrefix;
    }

    /**
     * Gets the task URL prefix.
     * Required if the task app supports more than one task type.
     *
     * @return The task prefix.
     */
    public String getTaskPrefix() {
        return taskPrefix;
    }

    /**
     * Sets the task URL prefix.
     *
     * @param taskPrefix The task prefix.
     */
    public void setTaskPrefix(String taskPrefix) {
        this.taskPrefix = taskPrefix;
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
