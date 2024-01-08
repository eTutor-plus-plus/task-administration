package at.jku.dke.etutor.task_administration.dto;

import at.jku.dke.etutor.task_administration.data.entities.User;
import jakarta.validation.constraints.NotNull;
import org.hibernate.LazyInitializationException;

import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * View-DTO for {@link User}
 *
 * @param id                  The user identifier.
 * @param username            The username.
 * @param firstName           The first name.
 * @param lastName            The last name.
 * @param email               The email address.
 * @param enabled             Whether the user is active.
 * @param activated           The timestamp of the user activation.
 * @param fullAdmin           Whether the user is full administrator.
 * @param failedLoginCount    The amount of failed logins since last successful login.
 * @param lockoutEnd          The end of the user lock.
 * @param organizationalUnits The organizational units the user belongs to.
 * @param createdBy           The creation user.
 * @param createdDate         The creation date.
 * @param lastModifiedBy      The modification user.
 * @param lastModifiedDate    The modification date.
 */
public record UserDto(@NotNull Long id, @NotNull String username, @NotNull String firstName, @NotNull String lastName, @NotNull String email, @NotNull boolean enabled,
                      OffsetDateTime activated, @NotNull boolean fullAdmin, @NotNull int failedLoginCount, OffsetDateTime lockoutEnd,
                      @NotNull Set<OrganizationalUnitRoleAssignmentDto> organizationalUnits, String createdBy, Instant createdDate, String lastModifiedBy, Instant lastModifiedDate
) implements Serializable {

    /**
     * Creates a new instance of class {@link UserDto} based on an existing {@link User}.
     *
     * @param user The user for which create the DTO.
     */
    public UserDto(User user) {
        this(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getEnabled(), user.getActivatedDate(), user.isFullAdmin(),
            user.getFailedLoginCount(), user.getLockoutEnd(), MapOrganizationalUnits(user, null), user.getCreatedBy(), user.getCreatedDate(),
            user.getLastModifiedBy(), user.getLastModifiedDate()
        );
    }

    /**
     * Creates a new instance of class {@link UserDto} based on an existing {@link User}.
     *
     * @param user                The user for which create the DTO.
     * @param organizationalUnits The organizational units that should be mapped.
     */
    public UserDto(User user, Set<Long> organizationalUnits) {
        this(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getEnabled(), user.getActivatedDate(), user.isFullAdmin(),
            user.getFailedLoginCount(), user.getLockoutEnd(), MapOrganizationalUnits(user, organizationalUnits), user.getCreatedBy(), user.getCreatedDate(),
            user.getLastModifiedBy(), user.getLastModifiedDate()
        );
    }

    private static Set<OrganizationalUnitRoleAssignmentDto> MapOrganizationalUnits(User user, Set<Long> organizationalUnits) {
        try {
            return user.getOrganizationalUnits().stream()
                .map(x -> new OrganizationalUnitRoleAssignmentDto(x.getOrganizationalUnit().getId(), x.getRole()))
                .filter(x -> organizationalUnits == null || organizationalUnits.contains(x.organizationalUnit()))
                .collect(Collectors.toSet());
        } catch (LazyInitializationException ex) {
            return null;
        }
    }

}
