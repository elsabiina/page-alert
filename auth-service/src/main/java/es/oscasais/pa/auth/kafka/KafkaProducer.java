package es.oscasais.pa.auth.kafka;

import es.oscasais.pa.auth.dto.kafka.UserEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaProducer {
  private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendUserCreated(String email, UUID userId) {
    UserEventDTO event = new UserEventDTO();
    event.setUserId(userId);
    event.setEmail(email);

    kafkaTemplate.send("user-created", event);
    log.info("User created with email: {}", email);
  }

  public void sendUserRegistrationPending(String email, UUID userId) {
    UserEventDTO event = new UserEventDTO();
    event.setUserId(userId);
    event.setEmail(email);

    kafkaTemplate.send("user-registration-pending", event);
    log.info("User registration pending event sent for email: {}", email);
  }
}
