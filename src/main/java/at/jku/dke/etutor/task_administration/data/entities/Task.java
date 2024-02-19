package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Represents a task.
 */
@Entity
@Table(name = "tasks")
public class Task extends AuditedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ou_id", nullable = false)
    private OrganizationalUnit organizationalUnit;

    @Size(max = 100)
    @NotNull
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @NotNull
    @Column(name = "description_de", nullable = false, length = Integer.MAX_VALUE)
    private String descriptionDe;

    @NotNull
    @Column(name = "description_en", nullable = false, length = Integer.MAX_VALUE)
    private String descriptionEn;

    @NotNull
    @Column(name = "difficulty", nullable = false)
    private Short difficulty;

    @NotNull
    @Column(name = "max_points", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxPoints;

    @Size(max = 100)
    @NotNull
    @Column(name = "task_type", nullable = false, length = 100)
    private String taskType;

    @Column(name = "moodle_id")
    private Integer moodleId;

    @NotNull
    @Column(name = "status", columnDefinition = "task_status not null")
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "task_group_id")
    private TaskGroup taskGroup;

    @Size(max = 255)
    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_date")
    private OffsetDateTime approvedDate;


    @ManyToMany
    @JoinTable(name = "tasks_task_categories",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "task_category_id"))
    private Set<TaskCategory> taskCategories = new LinkedHashSet<>();

    /**
     * Creates a new instance of class {@link Task}.
     */
    public Task() {
        this.status = TaskStatus.DRAFT;
    }

    /**
     * Gets the organizational unit.
     *
     * @return The organizational unit.
     */
    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    /**
     * Sets the organizational unit.
     *
     * @param organizationalUnit The organizational unit.
     */
    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    /**
     * Gets the title.
     *
     * @return The title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title The title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the german description.
     *
     * @return The german description.
     */
    public String getDescriptionDe() {
        return descriptionDe;
    }

    /**
     * Sets the german description.
     *
     * @param descriptionDe The german description.
     */
    public void setDescriptionDe(String descriptionDe) {
        this.descriptionDe = descriptionDe;
    }

    /**
     * Gets the english description.
     *
     * @return The english description.
     */
    public String getDescriptionEn() {
        return descriptionEn;
    }

    /**
     * Sets the english description.
     *
     * @param descriptionEn The english description.
     */
    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    /**
     * Gets the difficulty.
     *
     * @return The difficulty.
     */
    public Short getDifficulty() {
        return difficulty;
    }

    /**
     * Sets the difficulty.
     *
     * @param difficulty The difficulty.
     */
    public void setDifficulty(Short difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Gets the max points.
     *
     * @return The max points.
     */
    public BigDecimal getMaxPoints() {
        return maxPoints;
    }

    /**
     * Sets the max points.
     *
     * @param maxPoints The max points.
     */
    public void setMaxPoints(BigDecimal maxPoints) {
        this.maxPoints = maxPoints;
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
     * Gets the moodle identifier (course category).
     *
     * @return The moodle identifier (course category).
     */
    public Integer getMoodleId() {
        return moodleId;
    }

    /**
     * Sets the moodle identifier (course category).
     *
     * @param moodleId The moodle identifier (course category).
     */
    public void setMoodleId(Integer moodleId) {
        this.moodleId = moodleId;
    }

    /**
     * Gets the status.
     *
     * @return The status.
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status The status.
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * Gets the task group.
     *
     * @return The task group.
     */
    public TaskGroup getTaskGroup() {
        return taskGroup;
    }

    /**
     * Sets the task group.
     *
     * @param taskGroup The task group.
     */
    public void setTaskGroup(TaskGroup taskGroup) {
        this.taskGroup = taskGroup;
    }

    /**
     * Gets the user who approved the task.
     *
     * @return The user.
     */
    public String getApprovedBy() {
        return approvedBy;
    }

    /**
     * Sets the user who approved the task.
     *
     * @param approvedBy The user.
     */
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    /**
     * Gets the approved date.
     *
     * @return The approved date.
     */
    public OffsetDateTime getApprovedDate() {
        return approvedDate;
    }

    /**
     * Sets the approved date.
     *
     * @param approvedDate The approved date.
     */
    public void setApprovedDate(OffsetDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    /**
     * Gets the task categories.
     *
     * @return The task categories.
     */
    public Set<TaskCategory> getTaskCategories() {
        return taskCategories;
    }

    /**
     * Sets the task categories.
     *
     * @param taskCategories The task categories.
     */
    public void setTaskCategories(Set<TaskCategory> taskCategories) {
        this.taskCategories = taskCategories;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Task.class.getSimpleName() + "[", "]")
            .add("id='" + this.getId() + "'")
            .add("title='" + title + "'")
            .add("taskType='" + taskType + "'")
            .add("status=" + status)
            .toString();
    }
}
