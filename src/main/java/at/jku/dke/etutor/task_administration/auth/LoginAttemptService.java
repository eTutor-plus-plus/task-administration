package at.jku.dke.etutor.task_administration.auth;

import at.jku.dke.etutor.task_administration.data.repositories.UserRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Manages login attempts and locks users in case of too many failed login attempts on the same IP.
 */
@Service
public class LoginAttemptService {
    /**
     * The maximum amount of login attempts before login.
     */
    public static final int IP_MAX_ATTEMPTS = 10;

    /**
     * The constant USER_MAX_ATTEMPTS.
     */
    public static final int USER_MAX_ATTEMPTS = 5;

    private static final LoadingCache<String, Integer> attemptsCache = CacheBuilder.newBuilder().expireAfterWrite(4, TimeUnit.HOURS).build(new CacheLoader<>() {
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
        // Set user login count
        var user = this.userRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (user != null) {
            user.setFailedLoginCount(user.getFailedLoginCount() + 1);
            if (user.getFailedLoginCount() > USER_MAX_ATTEMPTS)
                user.setLockoutEnd(OffsetDateTime.now().plusMinutes(30));

            this.userRepository.save(user);
        }

        // Set IP login count
        var ip = this.getClientIP();
        synchronized (attemptsCache) {
            int attempts;
            try {
                attempts = attemptsCache.get(ip);
            } catch (ExecutionException ex) {
                attempts = 0;
            }
            attempts++;
            attemptsCache.put(ip, attempts);
        }
    }

    /**
     * Handles a successful login.
     * This method resets the login attempt count for the user and IP-address to zero.
     *
     * @param username The username that tried to log in.
     */
    public void loginSucceeded(String username) {
        // Set user login count
        var user = this.userRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (user != null) {
            user.setFailedLoginCount(0);
            user.setLockoutEnd(null);
            this.userRepository.save(user);
        }

        // Set IP login count
        synchronized (attemptsCache) {
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
        } catch (ExecutionException e) {
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
        } catch (ExecutionException e) {
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
