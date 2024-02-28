package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TaskMoodleidId implements Serializable {
    private static final long serialVersionUID = -8822024911243038729L;
    @NotNull
    @Column(name = "task_category_id", nullable = false)
    private Long taskCategoryId;

    @NotNull
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    public Long getTaskCategoryId() {
        return taskCategoryId;
    }

    public void setTaskCategoryId(Long taskCategoryId) {
        this.taskCategoryId = taskCategoryId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TaskMoodleidId entity = (TaskMoodleidId) o;
        return Objects.equals(this.taskCategoryId, entity.taskCategoryId) &&
            Objects.equals(this.taskId, entity.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskCategoryId, taskId);
    }

}
