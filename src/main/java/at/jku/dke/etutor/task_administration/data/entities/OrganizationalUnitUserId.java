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
 * Represents the identifier for {@link OrganizationalUnitUser}.
 */
@Embeddable
public class OrganizationalUnitUserId implements Serializable {
    @Serial
    private static final long serialVersionUID = 525000867289491594L;

    @NotNull
    @Column(name = "organizational_unit_id", nullable = false)
    private Long organizationalUnitId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Creates a new instance of class {@link OrganizationalUnitUserId}.
     */
    public OrganizationalUnitUserId() {
    }

    /**
     * Creates a new instance of class {@link OrganizationalUnitUserId}.
     *
     * @param organizationalUnitId The organizational unit id.
     * @param userId               The user id.
     */
    public OrganizationalUnitUserId(Long organizationalUnitId, Long userId) {
        this.organizationalUnitId = organizationalUnitId;
        this.userId = userId;
    }

    /**
     * Gets the organizational unit id.
     *
     * @return The organizational unit id.
     */
    public Long getOrganizationalUnitId() {
        return organizationalUnitId;
    }

    /**
     * Sets the organizational unit id.
     *
     * @param organizationalUnitId The organizational unit id.
     */
    public void setOrganizationalUnitId(Long organizationalUnitId) {
        this.organizationalUnitId = organizationalUnitId;
    }

    /**
     * Gets the user id.
     *
     * @return The user id.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId The user id.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        OrganizationalUnitUserId entity = (OrganizationalUnitUserId) o;
        return Objects.equals(this.organizationalUnitId, entity.organizationalUnitId) &&
            Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationalUnitId, userId);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OrganizationalUnitUserId.class.getSimpleName() + "[", "]")
            .add("organizationalUnitId=" + organizationalUnitId)
            .add("userId=" + userId)
            .toString();
    }
}
