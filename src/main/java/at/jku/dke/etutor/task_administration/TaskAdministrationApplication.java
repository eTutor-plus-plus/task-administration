package at.jku.dke.etutor.task_administration;

import at.jku.dke.etutor.task_administration.data.entities.User;
import at.jku.dke.etutor.task_administration.data.repositories.UserRepository;
import at.jku.dke.etutor.task_administration.moodle.MoodleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * The main class of the application.
 */
@SpringBootApplication
@EnableConfigurationProperties(MoodleConfig.class)
public class TaskAdministrationApplication {

    private static final Logger LOG = LoggerFactory.getLogger(TaskAdministrationApplication.class);

    /**
     * The entry point of the application.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        var app = new SpringApplication(TaskAdministrationApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port");
        String contextPath = Optional
            .ofNullable(env.getProperty("server.servlet.context-path"))
            .filter(x -> !x.isBlank())
            .orElse("/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            LOG.warn("The host name could not be determined, using `localhost` as fallback");
        }
        var profiles = env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles();

        LOG.info("""

                ----------------------------------------------------------
                \tApplication '{}' is running! Access URLs:
                \tLocal: \t\t{}://localhost:{}{}
                \tExternal: \t{}://{}:{}{}
                \tProfile(s): \t{}
                ----------------------------------------------------------""",
            env.getProperty("spring.application.name"),
            protocol,
            serverPort,
            contextPath,
            protocol,
            hostAddress,
            serverPort,
            contextPath,
            profiles
        );
    }

    /**
     * Determines the local address of the application.
     *
     * @param env The environment.
     * @return The local address.
     */
    public static String determineLocalAddress(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port");
        String contextPath = Optional
            .ofNullable(env.getProperty("server.servlet.context-path"))
            .filter(x -> !x.isBlank())
            .orElse("/");
        String result = String.format("%s://127.0.0.1:%s%s", protocol, serverPort, contextPath);
        if (!result.endsWith("/"))
            result = result + "/";
        return result;
    }

    /**
     * Creates a new instance of class {@link TaskAdministrationApplication}.
     */
    public TaskAdministrationApplication() {
    }

    /**
     * Seeds the admin-user if no users exist.
     *
     * @param repository The user repository.
     * @return The command line runner.
     */
    @Bean
    CommandLineRunner seedAdminUser(UserRepository repository) {
        return args -> {
            long cnt = repository.count();
            if (cnt > 0)
                return;

            LOG.info("Seeding user 'admin' with password 'secret'");
            var user = new User();
            user.setUsername("admin");
            user.setEmail("etutor@example.com");
            user.setFirstName("eTutor");
            user.setLastName("Administrator");
            user.setEnabled(true);
            user.setActivatedDate(OffsetDateTime.now());
            user.setFullAdmin(true);
            user.setFailedLoginCount(0);
            user.setPassword("secret");

            repository.save(user);
        };
    }
}
