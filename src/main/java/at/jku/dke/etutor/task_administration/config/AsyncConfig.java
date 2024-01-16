package at.jku.dke.etutor.task_administration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

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

    /**
     * Creates a new {@link Executor} for asynchronous tasks.
     *
     * @return The {@link Executor}.
     */
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("eta-async-");
        executor.initialize();
        return executor;
    }
}
