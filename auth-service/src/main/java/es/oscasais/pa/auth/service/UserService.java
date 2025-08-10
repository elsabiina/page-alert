package es.oscasais.pa.auth.service;

import es.oscasais.pa.auth.dto.LoginRequestDTO;
import es.oscasais.pa.auth.mapper.UserMapper;
import es.oscasais.pa.auth.dto.kafka.UserEventDTO;
import es.oscasais.pa.auth.exception.UserCreationException;
import es.oscasais.pa.auth.exception.UserNotFoundException;
import es.oscasais.pa.auth.kafka.KafkaProducer;
import es.oscasais.pa.auth.model.User;
import es.oscasais.pa.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final KafkaProducer kafkaProducer;

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  @Autowired
  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, KafkaProducer kafkaProducer) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.kafkaProducer = kafkaProducer;
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public User save(User user) {
    return userRepository.save(user);
  }

  @Transactional
  public User createUser(@Valid LoginRequestDTO loginDTO, HttpServletRequest request) {
    try {
      // Create user with emailConfirmed = false
      User userDTO = UserMapper.toModel(loginDTO, passwordEncoder);
      userDTO.setEmailConfirmed(false);

      User user = save(userDTO);

      // Emit Kafka event for user-service
      kafkaProducer.sendUserCreated(user.getEmail(), user.getId());

      // Emit Kafka event for notification-service
      kafkaProducer.sendUserRegistrationPending(user.getEmail(), user.getId());

      return user;
    } catch (Exception e) {
      log.error("Error creating user: {}", loginDTO.getEmail());
      throw new UserCreationException("Error creating user: " + loginDTO.getEmail());
    }
  }

  @Transactional
  public void confirmUserEmail(String email) {
    User user = findByEmail(email)
        .orElseThrow(() -> {
          log.error("User email to be confirmed could not be found: {}", email);
          return new UserNotFoundException("User email to be confirmed could not be found: " + email);
        });

    user.setEmailConfirmed(true);
    save(user);
    log.info("User email confirmed successfully: {}", email);
  }
}
