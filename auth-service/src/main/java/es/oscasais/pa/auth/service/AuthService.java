package es.oscasais.pa.auth.service;

import es.oscasais.pa.auth.dto.LoginRequestDTO;
import es.oscasais.pa.auth.dto.UserInfoDTO;
import es.oscasais.pa.auth.exception.AuthenticationException;
import es.oscasais.pa.auth.model.User;
import es.oscasais.pa.auth.util.JwtUtil;
import es.oscasais.pa.auth.kafka.KafkaProducer;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
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
  private final CookieService cookieService;

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  @Autowired
  public AuthService(UserService userService, PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil, KafkaProducer kafkaProducer, CookieService cookieService) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.kafkaProducer = kafkaProducer;
    this.cookieService = cookieService;
  }

  public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
    try {
      return userService.findByEmail(loginRequestDTO.getEmail())
          .filter(u -> u.isEmailConfirmed())
          .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
          .map(u -> jwtUtil.generateToken(u));
    } catch (Exception e) {
      log.error("Database error during authentication for user: {}", loginRequestDTO.getEmail(), e);
      throw new AuthenticationException("Database error during authentication", e);
    }
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

  public Optional<UserInfoDTO> getCurrentUser(HttpServletRequest request) {
    // Extract JWT token from cookie
    String token = extractTokenFromCookie(request);
    if (token == null) {
      log.debug("No JWT token found in cookies");
      return Optional.empty();
    }

    // Validate token
    try {
      jwtUtil.validateToken(token);
    } catch (JwtException e) {
      log.debug("Invalid JWT token: {}", e.getMessage());
      return Optional.empty();
    }

    // Extract email from token
    Optional<String> emailOpt = jwtUtil.extractEmail(token);
    if (emailOpt.isEmpty()) {
      log.debug("No email found in JWT token");
      return Optional.empty();
    }

    // Get user from database
    return userService.findByEmail(emailOpt.get())
        .filter(User::isEmailConfirmed)
        .map(user -> new UserInfoDTO(user.getId(), user.getEmail(), user.isEmailConfirmed()));
  }

  private String extractTokenFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }

    String cookieName = cookieService.getCookieName();
    for (Cookie cookie : request.getCookies()) {
      if (cookieName.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

}
