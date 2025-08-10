package es.oscasais.pa.auth.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import es.oscasais.pa.auth.service.UserService;
import es.oscasais.pa.auth.dto.kafka.EmailConfirmationEventDTO;

@Service
public class KafkaConsumer {
  private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

  private final UserService userService;

  @Autowired
  public KafkaConsumer(UserService userService) {
    this.userService = userService;
  }

  @KafkaListener(topics = "user-email-confirmed", groupId = "auth-service")
  public void handleEmailConfirmed(EmailConfirmationEventDTO message) {
    userService.confirmUserEmail(message.getEmail());
    log.info("User email confirmed successfully: {}", message.getEmail());
  }
}
