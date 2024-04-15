package at.jku.dke.etutor.task_administration.services;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.mockito.Mockito.*;

class MailServiceTest {

    @Test
    void sendMail() {
        // Assert
        var sender = mock(MailSender.class);
        var service = new MailService(sender, "from@example.com");

        // Assert
        service.sendMail("to@example.com", "subject", "body");

        // Assert
        ArgumentMatcher<SimpleMailMessage> matcher = t ->
            t.getFrom().equals("from@example.com")
                && t.getTo().length == 1 && t.getTo()[0].equals("to@example.com")
                && t.getSubject().equals("subject") && t.getText().equals("body");
        verify(sender, times(1)).send(argThat(matcher));
    }

}
