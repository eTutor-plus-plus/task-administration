package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.MappedSuperclass;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Base class for entities.
 *
 * @param <PK> The type of the primary key.
 */
@MappedSuperclass
public abstract class BaseEntity<PK extends Serializable> {

    /**
     * Creates a new instance of class {@link BaseEntity}.
     */
    protected BaseEntity() {
    }

    /**
     * Gets the id.
     *
     * @return The id.
     */
    public abstract PK getId();

    /**
     * Sets the id.
     *
     * @param id The id.
     */
    public abstract void setId(PK id);

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * Compares the identifier.
     *
     * @param o The reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;

        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
            ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() :
            o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
            ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() :
            this.getClass();
        if (thisEffectiveClass != oEffectiveClass)
            return false;

        BaseEntity<?> task = (BaseEntity<?>) o;
        return getId() != null && Objects.equals(getId(), task.getId());
    }

    /**
     * Returns a hash code value for the object.
     * <p>
     * Always uses the hashcode of the class.
     *
     * @return A hash code value for this object.
     */
    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
            ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() :
            getClass().hashCode();
    }

    /**
     * Returns a string representation of the object containing the class name and the identifier.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
            .add("id=" + this.getId())
            .toString();
    }

}
