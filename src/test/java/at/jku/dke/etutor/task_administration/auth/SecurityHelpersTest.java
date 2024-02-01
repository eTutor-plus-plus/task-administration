package at.jku.dke.etutor.task_administration.auth;

import at.jku.dke.etutor.task_administration.SpringTaskAdministrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

@SpringTaskAdministrationTest
class SecurityHelpersTest {

    //#region --- getUserId ---
    @Test
    @WithMockJwtUser(uid = "10")
    void getUserId_jwtWithUid_returnId() {
        // Act
        var result = SecurityHelpers.getUserId();

        // Assert
        assertThat(result).isPresent().hasValue(10L);
    }

    @Test
    @WithMockJwtUser(uid = "")
    void getUserId_jwtWithoutUid_returnEmpty() {
        // Act
        var result = SecurityHelpers.getUserId();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @WithMockUser
    void getUserId_notJwt_returnEmpty() {
        // Act
        var result = SecurityHelpers.getUserId();

        // Assert
        assertThat(result).isEmpty();
    }
    //#endregion

    //#region --- getUserName ---
    @Test
    @WithMockJwtUser(sub = "getUserName_jwtWithSub_returnSub")
    void getUserName_jwtWithSub_returnSub() {
        // Act
        var result = SecurityHelpers.getUserName();

        // Assert
        assertThat(result).isEqualTo("getUserName_jwtWithSub_returnSub");
    }

    @Test
    @WithMockJwtUser(sub = "")
    void getUserName_jwtWithoutSub_returnSub() {
        // Act
        var result = SecurityHelpers.getUserName();

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @WithMockUser
    void getUserName_notJwt_returnSub() {
        // Act
        var result = SecurityHelpers.getUserName();

        // Assert
        assertThat(result).isNull();
    }
    //#endregion

    //#region --- isFullAdmin ---
    @Test
    @WithMockJwtUser(fullAdmin = "true")
    void isFullAdmin_jwtWithClaim_returnTrue() {
        // Act
        var result = SecurityHelpers.isFullAdmin();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @WithMockJwtUser(fullAdmin = "false")
    void isFullAdmin_jwtWithClaim_returnFalse() {
        // Act
        var result = SecurityHelpers.isFullAdmin();

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockJwtUser(fullAdmin = "")
    void isFullAdmin_jwtWithoutClaim_returnFalse() {
        // Act
        var result = SecurityHelpers.isFullAdmin();

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockUser
    void isFullAdmin_noJwt_returnFalse() {
        // Act
        var result = SecurityHelpers.isFullAdmin();

        // Assert
        assertThat(result).isFalse();
    }
    //#endregion

    //#region --- isTutor ---
    @Test
    @WithMockJwtUser(roles = {"1;" + AuthConstants.ROLE_TUTOR})
    void isTutor_jwtWithClaimOrgAndRole_returnTrue() {
        // Act
        var result = SecurityHelpers.isTutor(1);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @WithMockJwtUser(roles = {"1;" + AuthConstants.ROLE_INSTRUCTOR})
    void isTutor_jwtWithClaimOrgAndOtherRole_returnFalse() {
        // Act
        var result = SecurityHelpers.isTutor(1);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockJwtUser(roles = {"2;" + AuthConstants.ROLE_TUTOR})
    void isTutor_jwtWithClaimOtherOrg_returnFalse() {
        // Act
        var result = SecurityHelpers.isTutor(1);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockJwtUser(roles = {})
    void isTutor_jwtWithoutClaim_returnFalse() {
        // Act
        var result = SecurityHelpers.isTutor(1);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockUser
    void isTutor_noJwt_returnFalse() {
        // Act
        var result = SecurityHelpers.isTutor(1);

        // Assert
        assertThat(result).isFalse();
    }
    //#endregion

    //#region --- isInstructor ---
    @Test
    @WithMockJwtUser(roles = {"1;" + AuthConstants.ROLE_INSTRUCTOR})
    void isInstructor_jwtWithClaimOrgAndRole_returnTrue() {
        // Act
        var result = SecurityHelpers.isInstructor(1);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @WithMockJwtUser(roles = {"1;" + AuthConstants.ROLE_ADMIN})
    void isInstructor_jwtWithClaimOrgAndOtherRole_returnFalse() {
        // Act
        var result = SecurityHelpers.isInstructor(1);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockJwtUser(roles = {"2;" + AuthConstants.ROLE_INSTRUCTOR})
    void isInstructor_jwtWithClaimOtherOrg_returnFalse() {
        // Act
        var result = SecurityHelpers.isInstructor(1);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockJwtUser(roles = {})
    void isInstructor_jwtWithoutClaim_returnFalse() {
        // Act
        var result = SecurityHelpers.isInstructor(1);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockUser
    void isInstructor_noJwt_returnFalse() {
        // Act
        var result = SecurityHelpers.isInstructor(1);

        // Assert
        assertThat(result).isFalse();
    }
    //#endregion

    //#region --- isAdmin ---
    @Test
    @WithMockJwtUser(roles = {"1;" + AuthConstants.ROLE_ADMIN})
    void isAdmin_jwtWithClaimOrgAndRole_returnTrue() {
        // Act
        var result = SecurityHelpers.isAdmin(1);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @WithMockJwtUser(roles = {"1;" + AuthConstants.ROLE_INSTRUCTOR})
    void isAdmin_jwtWithClaimOrgAndOtherRole_returnFalse() {
        // Act
        var result = SecurityHelpers.isAdmin(1);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockJwtUser(roles = {"2;" + AuthConstants.ROLE_ADMIN})
    void isAdmin_jwtWithClaimOtherOrg_returnFalse() {
        // Act
        var result = SecurityHelpers.isAdmin(1);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockJwtUser(roles = {})
    void isAdmin_jwtWithoutClaim_returnFalse() {
        // Act
        var result = SecurityHelpers.isAdmin(1);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockUser
    void isAdmin_noJwt_returnFalse() {
        // Act
        var result = SecurityHelpers.isAdmin(1);

        // Assert
        assertThat(result).isFalse();
    }
    //#endregion

    //#region --- isUser ---
    @Test
    @WithMockJwtUser(roles = {"1;" + AuthConstants.ROLE_ADMIN, "2;" + AuthConstants.ROLE_INSTRUCTOR})
    void isUser_jwtWithClaimOrg_returnTrue() {
        // Act
        var result = SecurityHelpers.isUser(1);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @WithMockJwtUser(roles = {"1;" + AuthConstants.ROLE_INSTRUCTOR})
    void isUser_jwtWithClaimOtherOrg_returnFalse() {
        // Act
        var result = SecurityHelpers.isUser(2);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockJwtUser(roles = {})
    void isUser_jwtWithoutClaim_returnFalse() {
        // Act
        var result = SecurityHelpers.isUser(1);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @WithMockUser
    void isUser_noJwt_returnFalse() {
        // Act
        var result = SecurityHelpers.isUser(1);

        // Assert
        assertThat(result).isFalse();
    }
    //#endregion

    //#region --- getOrganizationalUnitsAsAdmin ---
    @Test
    @WithMockJwtUser(roles = {"1;" + AuthConstants.ROLE_ADMIN, "2;" + AuthConstants.ROLE_INSTRUCTOR, "3;" + AuthConstants.ROLE_ADMIN})
    void getOrganizationalUnitsAsAdmin_jwtWithClaim_returnSet() {
        // Act
        var result = SecurityHelpers.getOrganizationalUnitsAsAdmin();

        // Assert
        assertThat(result).containsExactlyInAnyOrder(1L, 3L).doesNotContain(2L);
    }

    @Test
    @WithMockJwtUser(roles = {})
    void getOrganizationalUnitsAsAdmin_jwtWithoutClaim_returnEmptySet() {
        // Act
        var result = SecurityHelpers.getOrganizationalUnitsAsAdmin();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @WithMockUser
    void getOrganizationalUnitsAsAdmin_noJwt_returnEmptySet() {
        // Act
        var result = SecurityHelpers.getOrganizationalUnitsAsAdmin();

        // Assert
        assertThat(result).isEmpty();
    }
    //#endregion

    //#region --- getOrganizationalUnitsAsAdminOrInstructor ---
    @Test
    @WithMockJwtUser(roles = {"1;" + AuthConstants.ROLE_TUTOR, "2;" + AuthConstants.ROLE_INSTRUCTOR, "3;" + AuthConstants.ROLE_ADMIN})
    void getOrganizationalUnitsAsAdminOrInstructor_jwtWithClaim_returnSet() {
        // Act
        var result = SecurityHelpers.getOrganizationalUnitsAsAdminOrInstructor();

        // Assert
        assertThat(result).containsExactlyInAnyOrder(2L, 3L).doesNotContain(1L);
    }

    @Test
    @WithMockJwtUser(roles = {})
    void getOrganizationalUnitsAsAdminOrInstructor_jwtWithoutClaim_returnEmptySet() {
        // Act
        var result = SecurityHelpers.getOrganizationalUnitsAsAdminOrInstructor();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @WithMockUser
    void getOrganizationalUnitsAsAdminOrInstructor_noJwt_returnEmptySet() {
        // Act
        var result = SecurityHelpers.getOrganizationalUnitsAsAdminOrInstructor();

        // Assert
        assertThat(result).isEmpty();
    }
    //#endregion

    //#region --- getOrganizationalUnits ---
    @Test
    @WithMockJwtUser(roles = {"1;" + AuthConstants.ROLE_TUTOR, "2;" + AuthConstants.ROLE_INSTRUCTOR, "3;" + AuthConstants.ROLE_ADMIN})
    void getOrganizationalUnits_jwtWithClaim_returnSet() {
        // Act
        var result = SecurityHelpers.getOrganizationalUnits();

        // Assert
        assertThat(result).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    @WithMockJwtUser(roles = {})
    void getOrganizationalUnits_jwtWithoutClaim_returnEmptySet() {
        // Act
        var result = SecurityHelpers.getOrganizationalUnits();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @WithMockUser
    void getOrganizationalUnits_noJwt_returnEmptySet() {
        // Act
        var result = SecurityHelpers.getOrganizationalUnits();

        // Assert
        assertThat(result).isEmpty();
    }
    //#endregion

}
