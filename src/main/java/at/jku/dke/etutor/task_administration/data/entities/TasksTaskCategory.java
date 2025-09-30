package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "tasks_task_categories")
public class TasksTaskCategory {
    @EmbeddedId
    private TasksTaskCategoryId id;

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

    @NotNull
    @ColumnDefault("2")
    @Column(name = "feedbacklevel", nullable = false)
    private Integer feedbacklevel;

    public TasksTaskCategoryId getId() {
        return id;
    }

    public void setId(TasksTaskCategoryId id) {
        this.id = id;
    }

    public TaskCategory getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(TaskCategory taskCategory) {
        this.taskCategory = taskCategory;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Integer getFeedbacklevel() {
        return feedbacklevel;
    }

    public void setFeedbacklevel(Integer feedbacklevel) {
        this.feedbacklevel = feedbacklevel;
    }



}
