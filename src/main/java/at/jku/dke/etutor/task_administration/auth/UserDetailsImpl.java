package at.jku.dke.etutor.task_administration.auth;

import at.jku.dke.etutor.task_administration.data.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;

/**
 * Implementation of {@link UserDetails}.
 */
public class UserDetailsImpl implements UserDetails {

    private final User user;

    /**
     * Creates a new instance of class {@link UserDetailsImpl}.
     *
     * @param user The user.
     */
    public UserDetailsImpl(User user) {
        this.user = user;
    }

    /**
     * Returns the authorities granted to the user. Cannot return <code>null</code>.
     *
     * @return The authorities, sorted by natural key (never <code>null</code>).
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> list = new HashSet<>();

        if (this.user.isFullAdmin())
            list.add(new SimpleGrantedAuthority(AuthConstants.ROLE_FULL_ADMIN));

        this.user.getOrganizationalUnits().stream()
            .map(x -> new SimpleGrantedAuthority(x.getRole().name()))
            .forEach(list::add);

        return list;
    }

    /**
     * Returns the password used to authenticate the user.
     *
     * @return The password.
     */
    @Override
    public String getPassword() {
        return this.user.getPasswordHash();
    }

    /**
     * Returns the username used to authenticate the user.
     * Cannot return <code>null</code>.
     *
     * @return The username (never <code>null</code>).
     */
    @Override
    public String getUsername() {
        return this.user.getUsername();
    }

    /**
     * Indicates whether the user's account has expired.
     * An expired account cannot be authenticated.
     *
     * @return <code>true</code> if the user's account is valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired).
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     * A locked user cannot be authenticated.
     *
     * @return <code>true</code> if the user is not locked, <code>false</code> otherwise.
     */
    @Override
    public boolean isAccountNonLocked() {
        return this.user.getLockoutEnd() == null || this.user.getLockoutEnd().isBefore(OffsetDateTime.now());
    }

    /**
     * Indicates whether the user's credentials (password) has expired.
     * Expired credentials prevent authentication.
     *
     * @return <code>true</code> if the user's credentials are valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired).
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return this.user.getActivatedDate() == null || this.user.getActivatedDate().isBefore(OffsetDateTime.now());
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * A disabled user cannot be authenticated.
     *
     * @return <code>true</code> if the user is enabled, <code>false</code> otherwise.
     */
    @Override
    public boolean isEnabled() {
        return this.user.getEnabled();
    }

}
