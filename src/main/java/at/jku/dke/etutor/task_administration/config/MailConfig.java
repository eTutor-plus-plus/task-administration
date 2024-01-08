package at.jku.dke.etutor.task_administration.config;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
import java.util.Properties;

/**
 * Configuration for the mail service.
 */
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {
    /**
     * Creates a new instance of class {@link MailConfig}.
     */
    public MailConfig() {
    }

    /**
     * Provides a mail sender.
     *
     * @param props The mail sender configuration properties.
     * @return A mail sender.
     */
    @Bean
    MailSender javaMailSender(MailProperties props) {
        var sender = new JavaMailSenderImpl();
        sender.setHost(props.getHost());
        if (props.getPort() != null) {
            sender.setPort(props.getPort());
        }
        sender.setUsername(props.getUsername());
        sender.setPassword(props.getPassword());
        sender.setProtocol(props.getProtocol());
        if (props.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(props.getDefaultEncoding().name());
        }
        if (!props.getProperties().isEmpty()) {
            sender.setJavaMailProperties(asProperties(props.getProperties()));
        }
        return sender;
    }

    private Properties asProperties(Map<String, String> source) {
        Properties properties = new Properties();
        properties.putAll(source);
        return properties;
    }
}
