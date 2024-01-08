package at.jku.dke.etutor.task_administration.config;

import at.jku.dke.etutor.task_administration.data.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * The data configuration.
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
public class PersistenceConfig {
    /**
     * Creates a new instance of class {@link PersistenceConfig}.
     */
    public PersistenceConfig() {
    }

    /**
     * Provides the auditor provider.
     *
     * @return The auditor provider.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }
}
