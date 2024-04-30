package at.jku.dke.etutor.task_administration.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.mail.MailProperties;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MailConfigTest {

    @Test
    void javaMailSender() {
        // Arrange
        var config = new MailConfig();
        var props = new MailProperties();
        props.setPort(1234);
        props.setHost("host");
        props.setUsername("username");
        props.setPassword("password");
        props.setProtocol("protocol");
        props.setDefaultEncoding(Charset.defaultCharset());
        props.getProperties().put("key", "value");

        // Act
        var sender = config.javaMailSender(props);

        // Assert
        assertNotNull(sender);
    }

}
