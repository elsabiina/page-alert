package es.oscasais.pa.auth.service;

import es.oscasais.pa.auth.dto.LoginRequestDTO;
import es.oscasais.pa.auth.model.User;
import es.oscasais.pa.auth.util.JwtUtil;
import es.oscasais.pa.auth.kafka.KafkaProducer;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final KafkaProducer kafkaProducer;

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  @Autowired
  public AuthService(UserService userService, PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil, KafkaProducer kafkaProducer) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.kafkaProducer = kafkaProducer;
  }

  public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
    return userService.findByEmail(loginRequestDTO.getEmail())
        .filter(u -> u.isEmailConfirmed())
        .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
        .map(u -> jwtUtil.generateToken(u.getEmail()));
  }

  public boolean validateToken(String token) {
    try {
      jwtUtil.validateToken(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }


  public void createUserSession(LoginRequestDTO user, String token, HttpServletRequest request) {
    // Session will be used to get logged in user info
    HttpSession session = request.getSession(true);
    session.setAttribute("user", user.getEmail());
  }

}
