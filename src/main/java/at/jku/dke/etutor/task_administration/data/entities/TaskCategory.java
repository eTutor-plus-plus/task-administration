package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Represents a task category (a.k.a. "Moodle Question Bank").
 */
@Entity
@Table(name = "task_categories")
public class TaskCategory extends AuditedEntity {
    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "parent_id")
    private TaskCategory parent;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ou_id", nullable = false)
    private OrganizationalUnit organizationalUnit;

    @Column(name = "moodle_id")
    private Integer moodleId;

    @OneToMany(mappedBy = "parent")
    private Set<TaskCategory> children = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "taskCategories")
    private Set<Task> tasks = new LinkedHashSet<>();

    /**
     * Creates a new instance of class {@link TaskCategory}.
     */
    public TaskCategory() {
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
     * Gets the parent.
     *
     * @return The parent.
     */
    public TaskCategory getParent() {
        return parent;
    }

    /**
     * Sets the parent.
     *
     * @param parent The parent.
     */
    public void setParent(TaskCategory parent) {
        this.parent = parent;
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
     * Gets the moodle identifier (question category).
     *
     * @return The moodle identifier (question category).
     */
    public Integer getMoodleId() {
        return moodleId;
    }

    /**
     * Sets the moodle identifier (question category).
     *
     * @param moodleId The moodle identifier (question category).
     */
    public void setMoodleId(Integer moodleId) {
        this.moodleId = moodleId;
    }

    /**
     * Gets the children.
     *
     * @return The children.
     */
    public Set<TaskCategory> getChildren() {
        return children;
    }

    /**
     * Sets the children.
     *
     * @param children The children.
     */
    public void setChildren(Set<TaskCategory> children) {
        this.children = children;
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
        return new StringJoiner(", ", TaskCategory.class.getSimpleName() + "[", "]")
            .add("id='" + this.getId() + "'")
            .add("name='" + name + "'")
            .toString();
    }
}
