package at.jku.dke.etutor.task_administration.auth;

import at.jku.dke.etutor.task_administration.data.repositories.UserRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UserDetailsService}.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    /**
     * Creates a new instance of class {@link UserDetailsServiceImpl}.
     *
     * @param userRepository      The user repository.
     * @param loginAttemptService The login attempt service.
     */
    public UserDetailsServiceImpl(UserRepository userRepository, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (this.loginAttemptService.isBlocked())
            throw new LockedException("User is blocked because of too many failed login attempts.");

        var user = this.userRepository.findByUsernameIgnoreCaseAndFetchOrganizationalUnits(username)
            .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " does not exist."));
        return new UserDetailsImpl(user);
    }
}
