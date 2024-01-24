package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base class for entities with audit properties and auto-incrementing identifier.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditedEntity extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @CreatedBy
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by", nullable = false)
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private Instant lastModifiedDate;

    /**
     * Creates a new instance of class {@link AuditedEntity}.
     */
    protected AuditedEntity() {
    }

    /**
     * Creates a new instance of class {@link AuditedEntity}.
     *
     * @param id The identifier.
     */
    protected AuditedEntity(Long id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user that created the entity.
     *
     * @return The username.
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the user that created the entity.
     *
     * @param createdBy The username.
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Gets the timestamp the entity was created.
     *
     * @return The creation timestamp.
     */
    public Instant getCreatedDate() {
        return createdDate;
    }

    /**
     * Gets the timestamp the entity was created.
     *
     * @param createdDate The creation timestamp.
     */
    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Gets the user who last modified the entity.
     *
     * @return The username.
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * Sets the user who last modified the entity.
     *
     * @param lastModifiedBy The username.
     */
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * Gets the timestamp at which the entry was last edited.
     *
     * @return The modification timestamp.
     */
    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * Sets the timestamp at which the entry was last edited.
     *
     * @param lastModifiedDate The modification timestamp.
     */
    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

}
