package at.jku.dke.etutor.task_administration.auth;

/**
 * Contains some authentication constants used in the application.
 */
public final class AuthConstants {
    private AuthConstants() {
    }

    /**
     * Claim type for user identifier.
     */
    public static final String CLAIM_UID = "uid";

    /**
     * Claim type for token type.
     */
    public static final String CLAIM_TOKEN_TYPE = "token_type";

    /**
     * Claim value for refresh token.
     */
    public static final String CLAIM_REFRESH_TOKEN = "refresh";

    /**
     * Claim type for security value.
     */
    public static final String CLAIM_SEC = "sec";

    /**
     * Claim type for subject identifier.
     */
    public static final String CLAIM_SUB_ID = "sub_id";

    /**
     * Claim type for full admin flag.
     */
    public static final String CLAIM_FULL_ADMIN = "full_admin";

    /**
     * Claim type for roles.
     */
    public static final String CLAIM_ROLES = "roles";

    /**
     * Role: Full Administrator
     */
    public static final String ROLE_FULL_ADMIN = "FULL_ADMIN";

    /**
     * Role: Administrator
     */
    public static final String ROLE_ADMIN = "ADMIN"; // MUST BE SAME AS UserRole#ADMIN

    /**
     * Role: Instructor
     */
    public static final String ROLE_INSTRUCTOR = "INSTRUCTOR"; // MUST BE SAME AS UserRole#INSTRUCTOR

    /**
     * Role: Tutor
     */
    public static final String ROLE_TUTOR = "TUTOR"; // MUST BE SAME AS UserRole#TUTOR

    /**
     * Authority: INSTRUCTOR
     *
     * @see #ROLE_INSTRUCTOR
     * @see #ROLE_ADMIN
     * @see #ROLE_FULL_ADMIN
     */
    public static final String AUTHORITY_INSTRUCTOR_OR_ABOVE = "hasAnyAuthority('" + ROLE_FULL_ADMIN + "','" + ROLE_ADMIN + "','" + ROLE_INSTRUCTOR +"')";

    /**
     * Authority: ADMIN
     *
     * @see #ROLE_ADMIN
     * @see #ROLE_FULL_ADMIN
     */
    public static final String AUTHORITY_ADMIN_OR_ABOVE = "hasAnyAuthority('" + ROLE_FULL_ADMIN + "','" + ROLE_ADMIN + "')";

    /**
     * Authority: FULL ADMIN
     *
     * @see #ROLE_FULL_ADMIN
     */
    public static final String AUTHORITY_FULL_ADMIN = "hasAuthority('" + ROLE_FULL_ADMIN + "')";
}
