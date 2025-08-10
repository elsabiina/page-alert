package es.oscasais.pa.userService.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import es.oscasais.pa.userService.dto.UserRequestDTO;
import es.oscasais.pa.userService.dto.kafka.EmailConfirmationEventDTO;
import es.oscasais.pa.userService.dto.kafka.UserEventDTO;
import es.oscasais.pa.userService.service.UserService;

@Service
public class KafkaConsumer {
  private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

  private final UserService userService;

  @Autowired
  public KafkaConsumer(UserService userService) {
    this.userService = userService;
  }

  @KafkaListener(topics = "user-created", groupId = "user-service")
  public void handleUserCreated(UserEventDTO event) {
    UserRequestDTO userRequest = new UserRequestDTO();
    userRequest.setEmail(event.getEmail());
    userRequest.setAlias(event.getEmail().split("@")[0]);

    userService.createUser(userRequest);
    log.info("User account created successfully: {}", event.getEmail());
  }

  @KafkaListener(topics = "user-email-confirmed", groupId = "user-service")
  public void handleEmailConfirmed(EmailConfirmationEventDTO event) {
    UserRequestDTO userRequest = new UserRequestDTO();
    userRequest.setEmail(event.getEmail());

    userService.confirmUserEmail(userRequest);
    log.info("User email confirmed: {}", event.getEmail());
  }
}
