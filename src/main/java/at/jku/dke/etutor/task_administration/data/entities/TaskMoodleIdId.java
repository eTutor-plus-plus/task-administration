package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents the composite primary key for {@link TaskMoodleId}.
 */
@Embeddable
public class TaskMoodleIdId implements Serializable {
    @Serial
    private static final long serialVersionUID = -8822024911243038729L;

    @NotNull
    @Column(name = "task_category_id", nullable = false)
    private Long taskCategoryId;

    @NotNull
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * Creates a new instance of class {@link TaskMoodleIdId}.
     */
    public TaskMoodleIdId() {
    }

    /**
     * Creates a new instance of class {@link TaskMoodleIdId}.
     *
     * @param taskCategoryId The task category id.
     * @param taskId         The task id.
     */
    public TaskMoodleIdId(Long taskCategoryId, Long taskId) {
        this.taskCategoryId = taskCategoryId;
        this.taskId = taskId;
    }

    /**
     * Gets the task category id.
     *
     * @return The task category id.
     */
    public Long getTaskCategoryId() {
        return taskCategoryId;
    }

    /**
     * Sets the task category id.
     *
     * @param taskCategoryId The task category id.
     */
    public void setTaskCategoryId(Long taskCategoryId) {
        this.taskCategoryId = taskCategoryId;
    }

    /**
     * Gets the task id.
     *
     * @return The task id.
     */
    public Long getTaskId() {
        return taskId;
    }

    /**
     * Sets the task id.
     *
     * @param taskId The task id.
     */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TaskMoodleIdId entity = (TaskMoodleIdId) o;
        return Objects.equals(this.taskCategoryId, entity.taskCategoryId) &&
            Objects.equals(this.taskId, entity.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskCategoryId, taskId);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TaskMoodleIdId.class.getSimpleName() + "[", "]")
            .add("taskCategoryId=" + taskCategoryId)
            .add("taskId=" + taskId)
            .toString();
    }
}
