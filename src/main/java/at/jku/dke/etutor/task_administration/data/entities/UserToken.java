package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

/**
 * Represents a user token.
 */
@Entity
@Table(name = "user_tokens")
public class UserToken extends BaseEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "type", columnDefinition = "token_type not null")
    private TokenType type;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 255)
    @NotNull
    @Column(name = "token", nullable = false)
    private String token;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    /**
     * Creates a new instance of class {@link UserToken}.
     */
    public UserToken() {
    }

    /**
     * Creates a new instance of class {@link UserToken}.
     *
     * @param type      The token type.
     * @param user      The user.
     * @param token     The token.
     * @param expiresAt The token expiration.
     */
    public UserToken(TokenType type, User user, String token, OffsetDateTime expiresAt) {
        this.type = type;
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the token type.
     *
     * @return The type.
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Sets the token type.
     *
     * @param type The type.
     */
    public void setType(TokenType type) {
        this.type = type;
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
     * Gets the token.
     *
     * @return The token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the token.
     *
     * @param token The token.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets the expiration of the token.
     *
     * @return The expiration timestamp.
     */
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the expiration of the token.
     *
     * @param expiresAt The expiration timestamp.
     */
    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

}
