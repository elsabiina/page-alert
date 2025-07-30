package es.oscasais.pa.auth.service;

import es.oscasais.pa.auth.dto.LoginRequestDTO;
import es.oscasais.pa.auth.model.User;
import es.oscasais.pa.auth.util.JwtUtil;
import es.oscasais.pa.auth.exception.UserCreationException;
import es.oscasais.pa.auth.mapper.UserMapper;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthService {

  @Autowired
  private WebClient webClient;

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  private static final Logger log = LoggerFactory.getLogger(UserCreationException.class);

  @Autowired
  public AuthService(UserService userService, PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil, WebClient webClient) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.webClient = webClient;
  }

  public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
    Optional<String> token = userService.findByEmail(loginRequestDTO.getEmail())
        .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(),
            u.getPassword()))
        .map(u -> jwtUtil.generateToken(u.getEmail()));

    return token;
  }

  public boolean validateToken(String token) {
    try {
      jwtUtil.validateToken(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }

  @Transactional
  public User createUser(@Valid LoginRequestDTO loginDTO, HttpServletRequest request) {
    try {

      // create user
      User userDTO = UserMapper.toModel(loginDTO, passwordEncoder);

      User user = userService.save(userDTO);

      // create user session
      Optional<String> tokenOptional = authenticate(loginDTO);
      createUserSession(loginDTO, tokenOptional.get(), request);

      // TODO: create user account on user-service.
      // Impossible to make it post anything to user-service
      // Possible error converting to JSON
      // webClient.post()
      // .uri("/api/users/create-user-account")
      // .bodyValue(loginDTO)
      // .retrieve()
      // .bodyToMono(String.class)
      // .block();

      return user;
    } catch (Exception e) {

      log.error("Error creating user: {}", loginDTO.getEmail());
      throw new UserCreationException("Error creating User: " + loginDTO.getEmail());
    }
  }

  public void createUserSession(LoginRequestDTO user, String token, HttpServletRequest request) {
    // Session will be used to get logged in user info
    HttpSession session = request.getSession(true);
    session.setAttribute("user", user.getEmail());
  }

}
