package at.jku.dke.etutor.task_administration.auth;

import at.jku.dke.etutor.task_administration.SpringTaskAdministrationTest;
import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnit;
import at.jku.dke.etutor.task_administration.data.entities.OrganizationalUnitUser;
import at.jku.dke.etutor.task_administration.data.entities.User;
import at.jku.dke.etutor.task_administration.data.entities.UserRole;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitRepository;
import at.jku.dke.etutor.task_administration.data.repositories.OrganizationalUnitUserRepository;
import at.jku.dke.etutor.task_administration.data.repositories.UserRepository;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringTaskAdministrationTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationalUnitRepository organizationalUnitRepository;

    @Autowired
    private OrganizationalUnitUserRepository organizationalUnitUserRepository;

    //#region --- CREATE TOKEN ---
    @Test
    void createToken_userNotExists_throwsException() {
        // Arrange
        String username = "createToken_userNotExists_throwsException";

        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.createToken(username, "127.0.0.1"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void createToken_userNotActivated_throwsException() {
        // Arrange
        var user = new User("createToken_userNotActivated_throwsException", "First", "Last", "createToken_userNotActivated_throwsException@local", true, "pwd", false);
        this.userRepository.save(user);

        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.createToken(user.getUsername(), "127.0.0.2"))
            .isInstanceOf(LockedException.class);
    }

    @Test
    void createToken_userNotYetActive_throwsException() {
        // Arrange
        var user = new User("createToken_userNotYetActive_throwsException", "First", "Last", "createToken_userNotYetActive_throwsException@local", true, "pwd", false);
        user.setActivatedDate(OffsetDateTime.now().plusDays(1));
        this.userRepository.save(user);

        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.createToken(user.getUsername(), "127.0.0.3"))
            .isInstanceOf(LockedException.class);
    }

    @Test
    void createToken_userLocked_throwsException() {
        // Arrange
        var user = new User("createToken_userLocked_throwsException", "First", "Last", "createToken_userLocked_throwsException@local", true, "pwd", false);
        user.setActivatedDate(OffsetDateTime.now().minusDays(1));
        user.setLockoutEnd(OffsetDateTime.now().plusDays(1));
        this.userRepository.save(user);

        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.createToken(user.getUsername(), "127.0.0.4"))
            .isInstanceOf(LockedException.class);
    }

    @Test
    void createToken_userNotEnabled_throwsException() {
        // Arrange
        var user = new User("createToken_userNotEnabled_throwsException", "First", "Last", "createToken_userNotEnabled_throwsException@local", false, "pwd", false);
        user.setActivatedDate(OffsetDateTime.now().minusDays(1));
        user.setLockoutEnd(OffsetDateTime.now().minusDays(1));
        this.userRepository.save(user);

        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.createToken(user.getUsername(), "127.0.0.5"))
            .isInstanceOf(LockedException.class);
    }

    @Test
    void createToken_userExists_returnToken() {
        // Arrange
        var ou = this.organizationalUnitRepository.save(new OrganizationalUnit("OU1"));
        var user = new User("createToken_userExists_returnToken", "First", "Last", "createToken_userExists_returnToken@local", true, "pwd", false);
        user.setActivatedDate(OffsetDateTime.now().minusDays(1));
        user.setLockoutEnd(OffsetDateTime.now().minusMinutes(1));
        this.userRepository.save(user);
        this.organizationalUnitUserRepository.save(new OrganizationalUnitUser(ou, user, UserRole.INSTRUCTOR));

        // Act & Assert
        var result = this.jwtService.createToken(user.getUsername(), "127.0.0.6");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.refreshToken()).isNotEmpty();
        assertThat(result.accessToken()).isNotEmpty();
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresIn()).isEqualTo(900);

        var accessToken = this.jwtDecoder.decode(result.accessToken());
        assertThat(accessToken).isNotNull();
        assertThat(accessToken.getSubject()).isEqualTo(user.getUsername());
        assertThat(accessToken.getClaimAsString(AuthConstants.CLAIM_UID)).isEqualTo(user.getId().toString());
        assertThat(accessToken.getClaimAsString("given_name")).isEqualTo(user.getFirstName());
        assertThat(accessToken.getClaimAsString("family_name")).isEqualTo(user.getLastName());
        assertThat(accessToken.getClaimAsString("email")).isEqualTo(user.getEmail());
        assertThat(accessToken.getClaimAsString("preferred_username")).isEqualTo(user.getUsername());
        assertThat(accessToken.getClaimAsString(AuthConstants.CLAIM_FULL_ADMIN)).isEqualTo(Boolean.toString(user.isFullAdmin()));
        assertThat(accessToken.getClaimAsString(AuthConstants.CLAIM_ROLES)).isEqualTo("[{organizationalUnit=1, role=INSTRUCTOR}]");

        var refreshToken = this.jwtDecoder.decode(result.refreshToken());
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getClaimAsString(AuthConstants.CLAIM_TOKEN_TYPE)).isEqualTo(AuthConstants.CLAIM_REFRESH_TOKEN);
        assertThat(refreshToken.getClaimAsString(AuthConstants.CLAIM_SUB_ID)).isEqualTo(user.getUsername());
        assertThat(refreshToken.getClaimAsString(AuthConstants.CLAIM_SEC)).isNotNull();
    }
    //#endregion

    //#region --- CREATE TOKEN ---
    @Test
    @WithMockUser(username = "refreshToken_validRefresh_returnToken")
    void refreshToken_validRefresh_returnToken() {
        // Arrange
        var ou = this.organizationalUnitRepository.save(new OrganizationalUnit("OU2"));
        var user = new User("refreshToken_validRefresh_returnToken", "First", "Last", "refreshToken_validRefresh_returnToken@local", true, "pwd", false);
        user.setActivatedDate(OffsetDateTime.now().minusDays(1));
        this.userRepository.save(user);
        this.organizationalUnitUserRepository.save(new OrganizationalUnitUser(ou, user, UserRole.ADMIN));
        var token = this.jwtService.createToken(user.getUsername(), "127.0.0.7");

        // Act
        var result = this.jwtService.refreshToken(token.refreshToken(), "127.0.0.7");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.refreshToken()).isNotEmpty();
        assertThat(result.accessToken()).isNotEmpty();
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresIn()).isEqualTo(900);
    }

    @Test
    void refreshToken_invalidToken_throwsException() {
        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.refreshToken("", "127.0.0.8"))
            .isInstanceOf(BadJwtException.class);
    }

    @Test
    void refreshToken_noExpiration_throwsException() {
        // Arrange
        var claims = JwtClaimsSet.builder()
            .issuer("self")
            .build();

        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.refreshToken(this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(), "127.0.0.9"))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshToken_expired_throwsException() {
        // Arrange
        var claims = JwtClaimsSet.builder()
            .issuer("self")
            .expiresAt(Instant.now().minusSeconds(1))
            .build();

        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.refreshToken(this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(), "127.0.0.10"))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshToken_noTokenType_throwsException() {
        // Arrange
        var claims = JwtClaimsSet.builder()
            .issuer("self")
            .expiresAt(Instant.now().plusSeconds(60))
            .build();

        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.refreshToken(this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(), "127.0.0.11"))
            .isInstanceOf(BadJwtException.class);
    }

    @Test
    void refreshToken_invalidTokenType_throwsException() {
        // Arrange
        var claims = JwtClaimsSet.builder()
            .issuer("self")
            .expiresAt(Instant.now().plusSeconds(60))
            .claim(AuthConstants.CLAIM_TOKEN_TYPE, "access")
            .build();

        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.refreshToken(this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(), "127.0.0.12"))
            .isInstanceOf(BadJwtException.class);
    }

    @Test
    @WithMockUser(username = "refreshToken_otherUser_throwsException")
    void refreshToken_otherUser_throwsException() {
        // Arrange
        var claims = JwtClaimsSet.builder()
            .issuer("self")
            .expiresAt(Instant.now().plusSeconds(60))
            .claim(AuthConstants.CLAIM_TOKEN_TYPE, AuthConstants.CLAIM_REFRESH_TOKEN)
            .claim(AuthConstants.CLAIM_SUB_ID, "otherUser")
            .build();

        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.refreshToken(this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(), "127.0.0.13"))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @WithMockUser(username = "refreshToken_otherIp_throwsException")
    void refreshToken_otherIp_throwsException() {
        // Arrange
        var claims = JwtClaimsSet.builder()
            .issuer("self")
            .expiresAt(Instant.now().plusSeconds(60))
            .claim(AuthConstants.CLAIM_TOKEN_TYPE, AuthConstants.CLAIM_REFRESH_TOKEN)
            .claim(AuthConstants.CLAIM_SEC, this.passwordEncoder.encode("127.0.0.1"))
            .claim(AuthConstants.CLAIM_SUB_ID, "refreshToken_otherIp_throwsException")
            .build();

        // Act & Assert
        assertThatThrownBy(() -> this.jwtService.refreshToken(this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(), "127.0.0.14"))
            .isInstanceOf(BadCredentialsException.class);
    }
    //#endregion

    @Test
    void getJWKSet_available_returnSet() {
        // Act
        var result = this.jwtService.getJWKSet();

        // Assert
        assertThat(result)
            .isNotNull()
            .isNotEmpty()
            .containsKey("keys");

        var keys = result.get("keys");
        assertThat(keys)
            .isNotNull()
            .isInstanceOf(Iterable.class)
            .asInstanceOf(InstanceOfAssertFactories.ITERABLE)
            .hasSize(1);

        var key = ((Iterable<?>) keys).iterator().next();
        assertThat(key)
            .isNotNull()
            .isInstanceOf(Map.class)
            .asInstanceOf(InstanceOfAssertFactories.MAP)
            .containsKey("e")
            .containsKey("kid")
            .containsKey("kty")
            .containsKey("n");
    }
}
