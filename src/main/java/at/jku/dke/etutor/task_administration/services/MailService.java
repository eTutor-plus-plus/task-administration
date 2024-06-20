package at.jku.dke.etutor.task_administration.services;

import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Helper service for sending mails.
 */
@Service
public class MailService {
    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);
    private final MailSender mailSender;
    private final String sender;

    /**
     * Creates a new instance of class {@link MailService}.
     *
     * @param mailSender The mail sender.
     * @param sender     The sender address.
     */
    public MailService(MailSender mailSender, @Email @Value("${spring.mail.sender}") String sender) {
        this.mailSender = mailSender;
        this.sender = sender;
    }

    /**
     * Sends a mail to the given address.
     * If mail sending failed no exception will be thrown.
     *
     * @param to      The recipient address.
     * @param subject The subject.
     * @param text    The text.
     */
    @Async
    public void sendMail(String to, String subject, String text) {
        LOG.info("Sending mail to {} with subject '{}'", to, subject);
        var message = new SimpleMailMessage();
        message.setFrom(this.sender);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            this.mailSender.send(message);
        } catch (MailException ex) {
            LOG.error("Could not send mail to {}", to, ex);
        }
    }
}
