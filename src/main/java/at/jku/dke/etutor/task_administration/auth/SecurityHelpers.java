package at.jku.dke.etutor.task_administration.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides helper methods for security related operations.
 */
public final class SecurityHelpers {
    private SecurityHelpers() {
    }

    /**
     * Returns the identifier of the current user.
     *
     * @return The identifier, if available.
     * @see AuthConstants#CLAIM_UID
     */
    public static Optional<Long> getUserId() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
            Long uid = jwt.getClaim(AuthConstants.CLAIM_UID);
            return Optional.ofNullable(uid);
        }
        return Optional.empty();
    }

    /**
     * Returns the username of the current user.
     *
     * @return The username, if available.
     */
    public static String getUserName() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("sub");
        }
        return null;
    }

    /**
     * Returns whether the current user is full administrator.
     *
     * @return Whether the user is full administrator.
     * @see AuthConstants#CLAIM_FULL_ADMIN
     */
    public static boolean isFullAdmin() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
            var fullAdmin = jwt.getClaimAsBoolean(AuthConstants.CLAIM_FULL_ADMIN);
            return fullAdmin != null && fullAdmin;
        }
        return false;
    }

    /**
     * Returns whether the current user is a tutor in the specified organizational unit.
     *
     * @param orgId The organizational unit id.
     * @return Whether the user is a tutor.
     * @see AuthConstants#ROLE_TUTOR
     */
    public static boolean isTutor(long orgId) {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
            //noinspection unchecked
            var roles = (List<Map<String, Object>>) jwt.getClaim(AuthConstants.CLAIM_ROLES);
            return roles.stream().anyMatch(x -> x.get("organizationalUnit").equals(orgId) && x.get("role").equals(AuthConstants.ROLE_TUTOR));
        }
        return false;
    }

    /**
     * Returns whether the current user is a teacher in the specified organizational unit.
     *
     * @param orgId The organizational unit id.
     * @return Whether the user is a teacher.
     * @see AuthConstants#ROLE_INSTRUCTOR
     */
    public static boolean isInstructor(long orgId) {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
            //noinspection unchecked
            var roles = (List<Map<String, Object>>) jwt.getClaim(AuthConstants.CLAIM_ROLES);
            return roles.stream().anyMatch(x -> x.get("organizationalUnit").equals(orgId) && x.get("role").equals(AuthConstants.ROLE_INSTRUCTOR));
        }
        return false;
    }

    /**
     * Returns whether the current user is an admin in the specified organizational unit.
     *
     * @param orgId The organizational unit id.
     * @return Whether the user is an admin.
     * @see AuthConstants#ROLE_ADMIN
     */
    public static boolean isAdmin(long orgId) {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
            //noinspection unchecked
            var roles = (List<Map<String, Object>>) jwt.getClaim(AuthConstants.CLAIM_ROLES);
            return roles.stream().anyMatch(x -> x.get("organizationalUnit").equals(orgId) && x.get("role").equals(AuthConstants.ROLE_ADMIN));
        }
        return false;
    }

    /**
     * Returns whether the current user is a user in the specified organizational unit.
     *
     * @param orgId The organizational unit id.
     * @return Whether the user is a user in the specified organizational unit.
     */
    public static boolean isUser(long orgId) {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
            //noinspection unchecked
            var roles = (List<Map<String, Object>>) jwt.getClaim(AuthConstants.CLAIM_ROLES);
            return roles.stream().anyMatch(x -> x.get("organizationalUnit").equals(orgId));
        }
        return false;
    }

    /**
     * Returns the identifiers of the organizational units for which the current user is an administrator.
     *
     * @return The identifiers of the administered organizational units.
     */
    public static Set<Long> getOrganizationalUnitsAsAdmin() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
            //noinspection unchecked
            var roles = (List<Map<String, Object>>) jwt.getClaim(AuthConstants.CLAIM_ROLES);
            return roles.stream()
                .filter(x -> x.get("role").equals(AuthConstants.ROLE_ADMIN))
                .map(x -> x.get("organizationalUnit"))
                .map(x -> (Long) x)
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    /**
     * Returns the identifiers of the organizational units for which the current user is an administrator or instructor.
     *
     * @return The identifiers of the administered organizational units.
     */
    public static Set<Long> getOrganizationalUnitsAsAdminOrInstructor() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
            //noinspection unchecked
            var roles = (List<Map<String, Object>>) jwt.getClaim(AuthConstants.CLAIM_ROLES);
            return roles.stream()
                .filter(x -> x.get("role").equals(AuthConstants.ROLE_ADMIN) || x.get("role").equals(AuthConstants.ROLE_INSTRUCTOR))
                .map(x -> x.get("organizationalUnit"))
                .map(x -> (Long) x)
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    /**
     * Returns the identifiers of the organizational units for the current user.
     *
     * @return The identifiers of the organizational units.
     */
    public static Set<Long> getOrganizationalUnits() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
            //noinspection unchecked
            var roles = (List<Map<String, Object>>) jwt.getClaim(AuthConstants.CLAIM_ROLES);
            return roles.stream()
                .map(x -> x.get("organizationalUnit"))
                .map(x -> (Long) x)
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
}
