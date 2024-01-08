package at.jku.dke.etutor.task_administration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for asynchronous tasks.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    /**
     * Creates a new instance of class {@link AsyncConfig}.
     */
    public AsyncConfig() {
    }
}
