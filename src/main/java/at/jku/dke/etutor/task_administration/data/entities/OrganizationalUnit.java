package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a organizational unit.
 */
@Entity
@Table(name = "organizational_units")
public class OrganizationalUnit extends AuditedEntity {

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "organizationalUnit")
    private Set<OrganizationalUnitUser> users = new LinkedHashSet<>();

    /**
     * Creates a new instance of class {@link OrganizationalUnit}.
     */
    public OrganizationalUnit() {
    }

    /**
     * Creates a new instance of class {@link OrganizationalUnit}.
     *
     * @param id The identifier.
     */
    public OrganizationalUnit(Long id) {
        super(id);
    }

    /**
     * Creates a new instance of class {@link OrganizationalUnit}.
     *
     * @param name The name.
     */
    public OrganizationalUnit(String name) {
        this.name = name;
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
     * Gets the users associated with the unit.
     *
     * @return The users.
     */
    public Set<OrganizationalUnitUser> getUsers() {
        return users;
    }

    /**
     * Sets the users associated with the unit.
     *
     * @param users The users.
     */
    public void setUsers(Set<OrganizationalUnitUser> users) {
        this.users = users;
    }

}
