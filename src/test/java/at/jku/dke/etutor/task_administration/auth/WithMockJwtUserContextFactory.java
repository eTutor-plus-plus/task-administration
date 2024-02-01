package at.jku.dke.etutor.task_administration.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WithMockJwtUserContextFactory implements WithSecurityContextFactory<WithMockJwtUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockJwtUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        var principal = Jwt.withTokenValue("token")
            .header("alg", "none");
        if (!annotation.uid().isBlank())
            principal.claim(AuthConstants.CLAIM_UID, Long.parseLong(annotation.uid()));
        if (!annotation.sub().isBlank())
            principal.claim("sub", annotation.sub());
        if (!annotation.fullAdmin().isBlank())
            principal.claim(AuthConstants.CLAIM_FULL_ADMIN, Boolean.parseBoolean(annotation.fullAdmin()));
        if (annotation.roles().length > 0) {
            principal.claim(AuthConstants.CLAIM_ROLES, Arrays.stream(annotation.roles()).map(x -> {
                var split = x.split(";");
                return Map.of("organizationalUnit", Long.parseLong(split[0]), "role", split[1]);
            }).toList());
        }
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal.build(), "", List.of()));
        return context;
    }
}
