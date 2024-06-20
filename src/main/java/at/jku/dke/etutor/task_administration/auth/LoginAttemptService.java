package at.jku.dke.etutor.task_administration.auth;

import at.jku.dke.etutor.task_administration.data.repositories.UserRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Manages login attempts and locks users in case of too many failed login attempts on the same IP.
 */
@Service
public class LoginAttemptService {
    private static final Logger LOG = LoggerFactory.getLogger(LoginAttemptService.class);

    /**
     * The maximum amount of login attempts of the same IP before lockout.
     */
    public static final int IP_MAX_ATTEMPTS = 10;

    /**
     * The maximum amount of login attempts of the same username before lockout.
     */
    public static final int USER_MAX_ATTEMPTS = 5;

    private static final LoadingCache<String, Integer> attemptsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(4, TimeUnit.HOURS)
        .build(new CacheLoader<>() {
            @Override
            public Integer load(String key) {
                return 0;
            }
        });

    private final UserRepository userRepository;
    private final HttpServletRequest request;

    /**
     * Creates a new instance of class {@link LoginAttemptService}.
     *
     * @param userRepository The user repository.
     * @param request        The HTTP request.
     */
    public LoginAttemptService(UserRepository userRepository, HttpServletRequest request) {
        this.userRepository = userRepository;
        this.request = request;
    }

    /**
     * Handles a failed login.
     * <p>
     * This method does the following:
     *     <ul>
     *         <li>Lock the IP address after {@link #IP_MAX_ATTEMPTS} failed login attempts.</li>
     *         <li>Lock the user after {@link #USER_MAX_ATTEMPTS} failed login attempts.</li>
     *     </ul>
     * </p>
     *
     * @param username The username that tried to log in.
     */
    public void loginFailed(String username) {
        LOG.warn("Login of user {} failed", username);

        // Set user login count
        var user = this.userRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (user != null) {
            user.setFailedLoginCount(user.getFailedLoginCount() + 1);
            if (user.getFailedLoginCount() > USER_MAX_ATTEMPTS)
                user.setLockoutEnd(OffsetDateTime.now().plusMinutes(30));
            LOG.debug("Set failed login count to {} and lockout end to {} for user {}", user.getFailedLoginCount(), user.getLockoutEnd(), username);

            this.userRepository.save(user);
        }

        // Set IP login count
        var ip = this.getClientIP();
        synchronized (attemptsCache) {
            int attempts;
            try {
                attempts = attemptsCache.get(ip);
            } catch (ExecutionException ex) {
                LOG.warn("Could not load login attempts cache for IP {}", ip, ex);
                attempts = 0;
            }
            attempts++;
            attemptsCache.put(ip, attempts);
            LOG.debug("Set fail login count for IP {} to {}", ip, attempts);
        }
    }

    /**
     * Handles a successful login.
     * This method resets the login attempt count for the user and IP-address to zero.
     *
     * @param username The username that tried to log in.
     */
    public void loginSucceeded(String username) {
        LOG.info("Login of user {} succeeded", username);

        // Set user login count
        var user = this.userRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (user != null) {
            user.setFailedLoginCount(0);
            user.setLockoutEnd(null);
            LOG.debug("Reset failed login count and lockout end for user {}", username);
            this.userRepository.save(user);
        }

        // Set IP login count
        synchronized (attemptsCache) {
            LOG.debug("Invalidating login attempts cache for IP {}", this.getClientIP());
            attemptsCache.invalidate(this.getClientIP());
        }
    }

    /**
     * Returns whether the current IP-address is blocked.
     *
     * @return {@code true} if the IP-address is blocked because of too many failed login attempts; {@code false} otherwise.
     */
    public boolean isBlocked() {
        try {
            synchronized (attemptsCache) {
                return attemptsCache.get(this.getClientIP()) > IP_MAX_ATTEMPTS;
            }
        } catch (ExecutionException ex) {
            LOG.warn("Failed to retrieve amount of failed login attempts cache for IP {}", this.getClientIP(), ex);
            return false;
        }
    }

    /**
     * Returns the failed login count of the current IP-address.
     *
     * @return The failed login count.
     */
    public int getFailedLoginCount() {
        try {
            synchronized (attemptsCache) {
                return attemptsCache.get(this.getClientIP());
            }
        } catch (ExecutionException ex) {
            LOG.warn("Failed to retrieve amount of failed login attempts cache for IP {}", this.getClientIP(), ex);
            return 0;
        }
    }

    /**
     * Gets the client IP address.
     *
     * @return The IP address of the client.
     */
    public String getClientIP() {
        final String xfHeader = this.request.getHeader("X-Forwarded-For");
        if (xfHeader != null)
            return xfHeader.split(",")[0];

        return this.request.getRemoteAddr();
    }
}
