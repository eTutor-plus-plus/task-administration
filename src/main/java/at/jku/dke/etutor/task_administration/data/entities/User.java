package at.jku.dke.etutor.task_administration.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Represents an application user.
 */
@Entity
@Table(name = "users")
public class User extends AuditedEntity {
    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Size(max = 100)
    @NotNull
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Size(max = 100)
    @NotNull
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Email
    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean enabled = false;

    @Size(max = 255)
    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotNull
    @Column(name = "failed_login_count", nullable = false)
    private Integer failedLoginCount = 0;

    @NotNull
    @Column(name = "full_admin", nullable = false)
    private Boolean fullAdmin = false;

    @Column(name = "activated_date")
    private OffsetDateTime activatedDate;

    @Column(name = "lockout_end")
    private OffsetDateTime lockoutEnd;

    @OneToMany(mappedBy = "user")
    private Set<OrganizationalUnitUser> organizationalUnits = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<UserToken> tokens = new LinkedHashSet<>();

    /**
     * Creates a new instance of class {@link User}.
     */
    public User() {
    }

    /**
     * Creates a new instance of class {@link User}.
     *
     * @param username  The username.
     * @param firstName The first name.
     * @param lastName  The last name.
     * @param email     The email address.
     * @param enabled    Whether the user is active.
     * @param password  The clear-text password.
     * @param fullAdmin Whether the user is full administrator.
     */
    public User(String username, String firstName, String lastName, String email, boolean enabled, String password, boolean fullAdmin) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enabled = enabled;
        this.fullAdmin = fullAdmin;
        this.setPassword(password);
    }

    /**
     * Gets the username.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the first name.
     *
     * @return The first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     *
     * @param firstName The first name.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name.
     *
     * @return The last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name.
     *
     * @param lastName The last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the email.
     *
     * @return The email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email The email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets whether the user is active.
     *
     * @return {@code true} if the user is allowed to log in; {@code false} otherwise.
     */
    public boolean getEnabled() {
        return enabled;
    }

    /**
     * Sets whether the user is active.
     *
     * @param enabled {@code true} if the user is allowed to log in; {@code false} otherwise.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the password hash.
     *
     * @return The password hash.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash.
     *
     * @param passwordHash The password hash.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Sets the password in clear text.
     * The method will hash the password.
     *
     * @param password The clear text password.
     */
    public void setPassword(String password) {
        this.passwordHash = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password);
    }

    /**
     * Gets the amount of failed logins after the last successful login.
     *
     * @return The amount of failed logins.
     */
    public int getFailedLoginCount() {
        return failedLoginCount;
    }

    /**
     * Sets the amount of failed logins after the last successful login.
     *
     * @param failedLoginCount The amount of failed logins.
     */
    public void setFailedLoginCount(int failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    /**
     * Gets whether the user is full administrator.
     *
     * @return {@code true} if the user is a full administrator; {@code false} otherwise.
     */
    public boolean isFullAdmin() {
        return fullAdmin;
    }

    /**
     * Sets whether the user is full administrator.
     *
     * @param fullAdmin {@code true} if the user is a full administrator; {@code false} otherwise.
     */
    public void setFullAdmin(boolean fullAdmin) {
        this.fullAdmin = fullAdmin;
    }

    /**
     * Gets the activation date of the user.
     *
     * @return The activation date.
     */
    public OffsetDateTime getActivatedDate() {
        return activatedDate;
    }

    /**
     * Sets the activation date of the user.
     *
     * @param activatedDate The activation date of the user.
     */
    public void setActivatedDate(OffsetDateTime activatedDate) {
        this.activatedDate = activatedDate;
    }

    /**
     * Gets the end of the lockout.
     *
     * @return The timestamp the lockout ends.
     */
    public OffsetDateTime getLockoutEnd() {
        return lockoutEnd;
    }

    /**
     * Gets the end of the lockout.
     *
     * @param lockoutEnd The timestamp the lockout ends.
     */
    public void setLockoutEnd(OffsetDateTime lockoutEnd) {
        this.lockoutEnd = lockoutEnd;
    }

    /**
     * Gets the organizational units the user is associated.
     *
     * @return The organizational units.
     */
    public Set<OrganizationalUnitUser> getOrganizationalUnits() {
        return organizationalUnits;
    }

    /**
     * Sets the organizational units the user is associated.
     *
     * @param organizationalUnits The organizational units.
     */
    public void setOrganizationalUnits(Set<OrganizationalUnitUser> organizationalUnits) {
        this.organizationalUnits = organizationalUnits;
    }

    /**
     * Gets the tokens of the user.
     *
     * @return The user tokens.
     */
    public Set<UserToken> getTokens() {
        return tokens;
    }

    /**
     * Sets the tokens of the user.
     *
     * @param tokens The user tokens.
     */
    public void setTokens(Set<UserToken> tokens) {
        this.tokens = tokens;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
            .add("id='" + this.getId() + "'")
            .add("username='" + this.username + "'")
            .toString();
    }
}
