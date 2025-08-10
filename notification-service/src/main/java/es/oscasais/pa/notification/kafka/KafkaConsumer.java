package es.oscasais.pa.notification.kafka;

import es.oscasais.pa.notification.dto.UserRegistrationEventDTO;
import es.oscasais.pa.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(topics = "user-registration-pending", groupId = "notification-service")
    public void handleUserRegistrationPending(UserRegistrationEventDTO event) {
        try {
            log.info("Received user registration pending event for email: {}", event.getEmail());
            notificationService.processUserRegistration(event.getEmail(), event.getUserId());
        } catch (Exception e) {
            log.error("Failed to process user registration pending event for email: {}", event.getEmail(), e);
        }
    }
}