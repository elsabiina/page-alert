package es.oscasais.pa.notification.kafka;

import es.oscasais.pa.notification.dto.EmailConfirmationEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEmailConfirmed(EmailConfirmationEventDTO event) {
        try {
            kafkaTemplate.send("user-email-confirmed", event);
            log.info("Email confirmation event sent for: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email confirmation event for: {}", event.getEmail(), e);
        }
    }
}