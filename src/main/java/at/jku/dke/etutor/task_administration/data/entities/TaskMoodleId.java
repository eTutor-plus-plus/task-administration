package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    public TaskMoodleId(Task task, TaskCategory taskCategory, long questionId) {
        this.id = new TaskMoodleIdId();
        this.id.setTaskId(task.getId());
        this.id.setTaskCategoryId(taskCategory.getId());
        this.task = task;
        this.taskCategory = taskCategory;
        this.moodleId = questionId;
    }

    public TaskMoodleId() {
        this.id = new TaskMoodleIdId();
    }

    public TaskMoodleIdId getId() {
        return id;
    }

    public void setId(TaskMoodleIdId id) {
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

    public Long getMoodleId() {
        return moodleId;
    }

    public void setMoodleId(Long moodleId) {
        this.moodleId = moodleId;
    }

}
