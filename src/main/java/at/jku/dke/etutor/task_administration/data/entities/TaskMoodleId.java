package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.StringJoiner;

/**
 * Represents an assignment of a (task, task category) pair to a moodle question id.
 */
@Entity
@Table(name = "task_moodleids")
public class TaskMoodleId extends BaseEntity<TaskMoodleIdId> {
    @EmbeddedId
    private TaskMoodleIdId id;

    @MapsId("taskCategoryId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "task_category_id", nullable = false)
    private TaskCategory taskCategory;

    @MapsId("taskId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "moodle_id")
    private Long moodleId;

    /**
     * Creates a new instance of class {@link TaskMoodleId}.
     */
    public TaskMoodleId() {
        this.id = new TaskMoodleIdId();
    }

    /**
     * Creates a new instance of class {@link TaskMoodleId}.
     *
     * @param task         The task.
     * @param taskCategory The task category.
     * @param questionId   The moodle question id.
     */
    public TaskMoodleId(Task task, TaskCategory taskCategory, long questionId) {
        this.id = new TaskMoodleIdId();
        this.id.setTaskId(task.getId());
        this.id.setTaskCategoryId(taskCategory.getId());
        this.task = task;
        this.taskCategory = taskCategory;
        this.moodleId = questionId;
    }

    /**
     * Gets the id.
     *
     * @return The id.
     */
    public TaskMoodleIdId getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id The id.
     */
    public void setId(TaskMoodleIdId id) {
        this.id = id;
    }

    /**
     * Gets the task category.
     *
     * @return The task category.
     */
    public TaskCategory getTaskCategory() {
        return taskCategory;
    }

    /**
     * Sets the task category.
     *
     * @param taskCategory The task category.
     */
    public void setTaskCategory(TaskCategory taskCategory) {
        this.taskCategory = taskCategory;
    }

    /**
     * Gets the task.
     *
     * @return The task.
     */
    public Task getTask() {
        return task;
    }

    /**
     * Sets the task.
     *
     * @param task The task.
     */
    public void setTask(Task task) {
        this.task = task;
    }

    /**
     * Gets the moodle id.
     *
     * @return The moodle id.
     */
    public Long getMoodleId() {
        return moodleId;
    }

    /**
     * Sets the moodle id.
     *
     * @param moodleId The moodle id.
     */
    public void setMoodleId(Long moodleId) {
        this.moodleId = moodleId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TaskMoodleId.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("moodleId=" + moodleId)
            .toString();
    }
}
