package at.jku.dke.etutor.task_administration.moodle;

import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the moodle service.
 */
@Validated
@ConfigurationProperties("moodle")
public class MoodleConfig {
    @NotEmpty
    private String token;

    @NotEmpty
    @URL
    private String url;

    private boolean enabled;

    /**
     * Creates a new instance of class {@link MoodleConfig}.
     */
    public MoodleConfig() {
    }

    /**
     * Gets the moodle authentication token.
     *
     * @return The moodle authentication token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the moodle authentication token.
     *
     * @param token The moodle authentication token.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets the moodle url.
     *
     * @return The moodle url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the moodle url.
     *
     * @param url The moodle url.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns whether the moodle synchronization is enabled.
     *
     * @return {@code true} if enabled; {@code false} otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the moodle synchronization is enabled.
     *
     * @param enabled {@code true} if enabled; {@code false} otherwise.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
