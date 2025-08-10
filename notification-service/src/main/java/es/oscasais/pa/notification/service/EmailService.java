package es.oscasais.pa.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8084}")
    private String baseUrl;

    public void sendEmailConfirmation(String toEmail, String confirmationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Confirm your email address - Page Alert");

            Context context = new Context();
            context.setVariable("email", toEmail);
            context.setVariable("confirmationUrl", baseUrl + "/api/notifications/confirm-email/" + confirmationToken);

            String htmlContent = templateEngine.process("email-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email confirmation sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email confirmation to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email confirmation", e);
        }
    }
}