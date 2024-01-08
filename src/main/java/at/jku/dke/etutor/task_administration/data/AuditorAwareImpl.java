package at.jku.dke.etutor.task_administration.data;

import at.jku.dke.etutor.task_administration.data.entities.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * Implementation of {@link AuditorAware} using Spring Security.
 */
public class AuditorAwareImpl implements AuditorAware<String> {

    /**
     * Creates a new instance of class {@link AuditorAwareImpl}.
     */
    public AuditorAwareImpl() {
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        var auditor = Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getPrincipal)
            .map(o -> switch (o) {
                case User u -> u.getUsername();
                case Jwt j -> j.getSubject();
                case UserDetails ud -> ud.getUsername();
                default -> o.toString();
            });

        return Optional.of(auditor.orElse("SYSTEM"));
    }

}
