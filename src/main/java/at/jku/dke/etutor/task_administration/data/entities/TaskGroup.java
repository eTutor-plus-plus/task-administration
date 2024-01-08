package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Represents a task group.
 */
@Entity
@Table(name = "task_groups")
public class TaskGroup extends AuditedEntity {
    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Column(name = "description_de", nullable = false, length = Integer.MAX_VALUE)
    private String descriptionDe;

    @NotNull
    @Column(name = "description_en", nullable = false, length = Integer.MAX_VALUE)
    private String descriptionEn;

    @Size(max = 100)
    @NotNull
    @Column(name = "task_group_type", nullable = false, length = 100)
    private String taskGroupType;

    @NotNull
    @Column(name = "status", columnDefinition = "task_status not null")
    private TaskStatus status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ou_id", nullable = false)
    private OrganizationalUnit organizationalUnit;

    @Size(max = 255)
    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_date")
    private OffsetDateTime approvedDate;

    @OneToMany(mappedBy = "taskGroup")
    private Set<Task> tasks = new LinkedHashSet<>();

    /**
     * Creates a new instance of class {@link TaskGroup}.
     */
    public TaskGroup() {
        this.status = TaskStatus.DRAFT;
    }

    /**
     * Gets the name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
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
     * Gets the task group type.
     *
     * @return The task group type.
     */
    public String getTaskGroupType() {
        return taskGroupType;
    }

    /**
     * Sets the task group type.
     *
     * @param taskGroupType The task group type.
     */
    public void setTaskGroupType(String taskGroupType) {
        this.taskGroupType = taskGroupType;
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
     * Gets the user who approved the task group.
     *
     * @return The user.
     */
    public String getApprovedBy() {
        return approvedBy;
    }

    /**
     * Sets the user who approved the task group.
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
     * Gets the tasks.
     *
     * @return The tasks.
     */
    public Set<Task> getTasks() {
        return tasks;
    }

    /**
     * Sets the tasks.
     *
     * @param tasks The tasks.
     */
    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TaskGroup.class.getSimpleName() + "[", "]")
            .add("id='" + this.getId() + "'")
            .add("name='" + name + "'")
            .add("status=" + status)
            .toString();
    }
}
