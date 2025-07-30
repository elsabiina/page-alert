package es.oscasais.pa.auth.service;

import es.oscasais.pa.auth.dto.LoginRequestDTO;
import es.oscasais.pa.auth.mapper.UserMapper;
import es.oscasais.pa.auth.model.User;
import es.oscasais.pa.auth.repository.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public User save(User user) {
    return userRepository.save(user);
  }
}
