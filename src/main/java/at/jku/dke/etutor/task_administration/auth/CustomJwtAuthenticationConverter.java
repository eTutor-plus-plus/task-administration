package at.jku.dke.etutor.task_administration.auth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Custom JWT authentication converter that adds additional authorities.
 */
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;

    /**
     * Creates a new instance of class {@link CustomJwtAuthenticationConverter}.
     */
    public CustomJwtAuthenticationConverter() {
        this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    /**
     * Convert the JWT to an authentication token.
     *
     * @param source The Json Web Token (never {@code null}).
     * @return The authentication token.
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        String principalClaimValue = source.getClaimAsString(JwtClaimNames.SUB);
        Collection<GrantedAuthority> authorities = this.jwtGrantedAuthoritiesConverter.convert(source);
        if (authorities == null)
            authorities = new ArrayList<>();

        // Full admin
        Boolean isFullAdmin = source.getClaimAsBoolean(AuthConstants.CLAIM_FULL_ADMIN);
        if (isFullAdmin != null && isFullAdmin)
            authorities.add(new SimpleGrantedAuthority(AuthConstants.ROLE_FULL_ADMIN));

        // Roles
        Collection<Map<String, Object>> roles = source.getClaim(AuthConstants.CLAIM_ROLES);
        if (roles != null)
            roles.stream()
                .map(x -> new SimpleGrantedAuthority(String.valueOf(x.get("role"))))
                .forEach(authorities::add);

        return new JwtAuthenticationToken(source, authorities, principalClaimValue);
    }
}
