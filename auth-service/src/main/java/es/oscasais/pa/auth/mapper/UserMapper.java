package es.oscasais.pa.auth.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;

import es.oscasais.pa.auth.dto.LoginRequestDTO;
import es.oscasais.pa.auth.model.User;

public class UserMapper {
  public static User toModel(LoginRequestDTO loginRequestDTO, PasswordEncoder passwordEncoder) {
    User user = new User();
    String password = passwordEncoder.encode(loginRequestDTO.getPassword());
    user.setEmail(loginRequestDTO.getEmail());
    user.setPassword(password);
    return user;
  }
}
