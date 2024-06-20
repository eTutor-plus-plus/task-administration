package at.jku.dke.etutor.task_administration.auth;

import at.jku.dke.etutor.task_administration.data.repositories.UserRepository;
import at.jku.dke.etutor.task_administration.dto.auth.LoginResponse;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling JWT authentication.
 */
@Service
public class JwtService {
    private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final AuthJWKSource jwkSource;
    private final int tokenLifetime;
    private final int refreshTokenLifetime;
    private JWKSet jwkSet;

    /**
     * Creates a new instance of class {@link JwtService}.
     *
     * @param userRepository       The user repository.
     * @param passwordEncoder      The password encoder.
     * @param jwtEncoder           The JWT encoder.
     * @param jwtDecoder           The JWT decoder.
     * @param jwkSource            The JWK source.
     * @param tokenLifetime        The token lifetime.
     * @param refreshTokenLifetime The refresh token lifetime.
     */
    public JwtService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
                      JwtDecoder jwtDecoder, AuthJWKSource jwkSource, @Value("${jwt.token-lifetime}") int tokenLifetime,
                      @Value("${jwt.refresh-token-lifetime}") int refreshTokenLifetime) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwkSource = jwkSource;
        this.tokenLifetime = tokenLifetime;
        this.refreshTokenLifetime = refreshTokenLifetime;
    }

    /**
     * Creates a new token for the specified user.
     *
     * @param username The username for which to create the token.
     * @return The token data.
     */
    @Transactional
    public LoginResponse createToken(String username, String clientIp) {
        // find user
        var user = this.userRepository.findByUsernameIgnoreCase(username);
        if (user.isEmpty()) {
            LOG.error("User {} not found. This should not have happened.", username);
            throw new UsernameNotFoundException("User " + username + " does not exist.");
        }
        if (user.get().getActivatedDate() == null || user.get().getActivatedDate().isAfter(OffsetDateTime.now())) {
            LOG.error("User {} is not activated.", username);
            throw new LockedException("User " + username + " is locked or not active.");
        }
        if (!user.get().getEnabled()) {
            LOG.error("User {} is not enabled.", username);
            throw new LockedException("User " + username + " is locked or not active.");
        }
        if (user.get().getLockoutEnd() != null && user.get().getLockoutEnd().isAfter(OffsetDateTime.now())) {
            LOG.error("User {} is locked until {}.", username, user.get().getLockoutEnd());
            throw new LockedException("User " + username + " is locked or not active.");
        }

        // create token
        LOG.info("Creating token for user {}", username);
        var claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(this.tokenLifetime))
            .subject(user.get().getUsername())
            .claim(AuthConstants.CLAIM_UID, user.get().getId())
            .claim("given_name", user.get().getFirstName())
            .claim("family_name", user.get().getLastName())
            .claim("email", user.get().getEmail())
            .claim("preferred_username", user.get().getUsername())
            .claim(AuthConstants.CLAIM_FULL_ADMIN, user.get().isFullAdmin())
            .claim(AuthConstants.CLAIM_ROLES, user.get().getOrganizationalUnits().stream().map(x -> new RoleClaimValue(x.getOrganizationalUnit().getId(), x.getRole().name())).toList())
            .build();

        // create refresh token
        LOG.debug("Creating refresh token for user {}", username);
        JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(this.refreshTokenLifetime))
            .claim(AuthConstants.CLAIM_TOKEN_TYPE, AuthConstants.CLAIM_REFRESH_TOKEN)
            .claim(AuthConstants.CLAIM_SUB_ID, username)
            .claim(AuthConstants.CLAIM_SEC, this.passwordEncoder.encode(clientIp))
            .build();

        return new LoginResponse(
            this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(),
            this.jwtEncoder.encode(JwtEncoderParameters.from(refreshClaims)).getTokenValue(),
            "Bearer",
            this.tokenLifetime);
    }

    /**
     * Refreshes a token.
     *
     * @param refreshToken The refresh token.
     * @param clientIp     The IP of the client that sent the request.
     * @return New access token data.
     */
    @Transactional
    public LoginResponse refreshToken(String refreshToken, String clientIp) {
        var decoded = this.jwtDecoder.decode(refreshToken);

        if (decoded.getExpiresAt() == null || decoded.getExpiresAt().isBefore(Instant.now())) {
            LOG.warn("Refresh token for user {} expired", decoded.getClaimAsString("sub_id"));
            throw new BadCredentialsException("Token expired");
        }

        if (decoded.getClaim(AuthConstants.CLAIM_TOKEN_TYPE) == null || !decoded.getClaim(AuthConstants.CLAIM_TOKEN_TYPE).equals(AuthConstants.CLAIM_REFRESH_TOKEN)) {
            LOG.warn("User tried to refresh token without using refresh token {}", refreshToken);
            throw new BadJwtException("Invalid token type");
        }

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(decoded.getClaim(AuthConstants.CLAIM_SUB_ID))) {
            LOG.warn("User {} tried to use refresh token of other user", auth.getName());
            throw new BadCredentialsException("Invalid token");
        }

        if (!this.passwordEncoder.matches(clientIp, decoded.getClaim(AuthConstants.CLAIM_SEC))) {
            LOG.warn("User {} tried to use refresh token from different IP address", auth.getName());
            throw new BadCredentialsException("Invalid token");
        }

        return this.createToken(auth.getName(), clientIp);
    }

    /**
     * Gets the JWK set.
     *
     * @return The JWK set.
     */
    public Map<String, Object> getJWKSet() {
        if (this.jwkSet == null) {
            try {
                LOG.info("Building JWK set");
                JWK key = new RSAKey.Builder(this.jwkSource.getPublicKey())
                    .privateKey(this.jwkSource.getPrivateKey())
                    .keyID(this.jwkSource.getKeyId())
                    .build();
                this.jwkSet = new JWKSet(key);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
                LOG.error("Could not generate JWK set", ex);
                return new HashMap<>();
            }
        }
        return this.jwkSet.toJSONObject();
    }

    /**
     * Represents a role assignment in an organizational unit.
     *
     * @param organizationalUnit The organizational unit.
     * @param role               The role.
     */
    public record RoleClaimValue(long organizationalUnit, String role) {
    }
}
