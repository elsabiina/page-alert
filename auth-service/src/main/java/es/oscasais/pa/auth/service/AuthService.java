package es.oscasais.pa.auth.service;

import es.oscasais.pa.auth.dto.LoginRequestDTO;
import es.oscasais.pa.auth.util.JwtUtil;
import io.jsonwebtoken.JwtException;

import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthService(UserService userService, PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
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
}
