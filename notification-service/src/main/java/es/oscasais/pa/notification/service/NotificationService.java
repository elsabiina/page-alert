package es.oscasais.pa.notification.service;

import es.oscasais.pa.notification.dto.EmailConfirmationEventDTO;
import es.oscasais.pa.notification.exception.TokenNotFoundException;
import es.oscasais.pa.notification.kafka.KafkaProducer;
import es.oscasais.pa.notification.model.EmailConfirmationToken;
import es.oscasais.pa.notification.repository.EmailConfirmationTokenRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private EmailConfirmationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Transactional
    public void processUserRegistration(String email, UUID userId) {
        try {
            // Check if there's already a pending token for this email
            tokenRepository.findByUserEmailAndConfirmedFalse(email)
                    .ifPresent(existingToken -> tokenRepository.delete(existingToken));

            // Generate new confirmation token
            String token = UUID.randomUUID().toString();
            Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);

            EmailConfirmationToken confirmationToken = new EmailConfirmationToken(email, token, expiresAt);
            tokenRepository.save(confirmationToken);

            // Send confirmation email
            emailService.sendEmailConfirmation(email, token);

            log.info("Email confirmation process initiated for user: {}", email);

        } catch (Exception e) {
            log.error("Failed to process user registration for email: {}", email, e);
            throw new RuntimeException("Failed to process user registration", e);
        }
    }

    @Transactional
    public void confirmEmail(String token) {
        EmailConfirmationToken confirmationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Invalid or expired confirmation token"));

        if (confirmationToken.isConfirmed()) {
            throw new IllegalStateException("Email already confirmed");
        }

        if (confirmationToken.isExpired()) {
            throw new TokenNotFoundException("Confirmation token has expired");
        }

        // Mark token as confirmed
        confirmationToken.setConfirmed(true);
        confirmationToken.setConfirmedAt(Instant.now());
        tokenRepository.save(confirmationToken);

        // Emit confirmation event
        EmailConfirmationEventDTO event = new EmailConfirmationEventDTO(
                confirmationToken.getUserEmail(),
                confirmationToken.getConfirmedAt()
        );
        kafkaProducer.sendEmailConfirmed(event);

        log.info("Email confirmed successfully for: {}", confirmationToken.getUserEmail());
    }

    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        tokenRepository.deleteByExpiresAtBeforeAndConfirmedFalse(now);
        log.info("Cleaned up expired confirmation tokens");
    }
}