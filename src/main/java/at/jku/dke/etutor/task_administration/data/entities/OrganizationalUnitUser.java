package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Represents an "organizational unit user" that assigns
 * a user with a specific role to an organizational unit.
 */
@Entity
@Table(name = "organizational_unit_users")
public class OrganizationalUnitUser extends BaseEntity<OrganizationalUnitUserId> {

    @EmbeddedId
    private OrganizationalUnitUserId id;

    @MapsId("organizationalUnitId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "organizational_unit_id", nullable = false)
    private OrganizationalUnit organizationalUnit;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "role", columnDefinition = "roles not null")
    private UserRole role;

    /**
     * Creates a new instance of class {@link OrganizationalUnitUser}.
     */
    public OrganizationalUnitUser() {
        this.id = new OrganizationalUnitUserId();
    }

    /**
     * Creates a new instance of class {@link OrganizationalUnitUser}.
     *
     * @param organizationalUnit The organizational unit.
     * @param user               The user.
     * @param role               The role.
     */
    public OrganizationalUnitUser(OrganizationalUnit organizationalUnit, User user, UserRole role) {
        this.id = new OrganizationalUnitUserId();
        this.id.setUserId(user.getId());
        this.id.setOrganizationalUnitId(organizationalUnit.getId());
        this.organizationalUnit = organizationalUnit;
        this.user = user;
        this.role = role;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrganizationalUnitUserId getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(OrganizationalUnitUserId id) {
        this.id = id;
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
     * Gets the user.
     *
     * @return The user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user The user.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the role.
     *
     * @return The role.
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Sets the role.
     *
     * @param role The role.
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

}
