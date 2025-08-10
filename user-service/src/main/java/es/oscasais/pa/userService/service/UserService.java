package es.oscasais.pa.userService.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.oscasais.pa.userService.dto.UserRequestDTO;
import es.oscasais.pa.userService.dto.UserResponseDTO;
import es.oscasais.pa.userService.exception.EmailAlreadyExistsException;
import es.oscasais.pa.userService.exception.UserNotFoundException;
import es.oscasais.pa.userService.mapper.UserMapper;
import es.oscasais.pa.userService.model.User;
import es.oscasais.pa.userService.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  @Autowired
  private UserRepository userRepository;

  /**
   * Gets All User from database
   * 
   * @return a List of UserDTO
   */
  public List<UserResponseDTO> findAll() {
    List<User> users = userRepository.findAll();

    return users.stream()
        .map(UserMapper::toDTO)
        .toList();
  }

  public UserResponseDTO findUser(UUID id) {
    User user = userRepository.findById(id).orElseThrow(
        () -> new UserNotFoundException("User not found with ID: " + id));

    return UserMapper.toDTO(user);
  }

  public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
    if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
      log.error("User could not be created. It already exists: " + userRequestDTO.getEmail());
      throw new EmailAlreadyExistsException("User with email " + userRequestDTO.getEmail() + " already exists");
    }
    User user = userRepository.save(UserMapper.toModel(userRequestDTO));

    return UserMapper.toDTO(user);
  }

  @Transactional
  public UserResponseDTO confirmUserEmail(UserRequestDTO dto) {
    User user = userRepository
        .findByEmail(dto.getEmail())
        .orElseThrow(() -> {
          log.error("User to be confirmed doesn't exist: {}", dto.getEmail());
          return new UserNotFoundException("User to be confirmed doesn't exist: " + dto.getEmail());
        });

    user.setEmailConfirmed(true);
    user = userRepository.save(user);

    return UserMapper.toDTO(user);
  }

  public UserResponseDTO updateUser(UUID id,
      UserRequestDTO userRequestDTO) {

    User user = userRepository.findById(id).orElseThrow(
        () -> new UserNotFoundException("User not found with ID: " + id));

    if (userRepository.existsByEmailAndIdNot(userRequestDTO.getEmail(), id)) {
      throw new EmailAlreadyExistsException(
          "A user with this email already exists: " + userRequestDTO.getEmail());
    }

    user.setEmail(userRequestDTO.getEmail());
    user.setAlias(userRequestDTO.getAlias());
    user.setAvatarUrl(userRequestDTO.getAvatarUrl());

    User updatedUser = userRepository.save(user);
    return UserMapper.toDTO(updatedUser);
  }

  public void deleteUser(UUID id) {
    userRepository.deleteById(id);
  }

  public UserResponseDTO findUserByEmail(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(
        () -> new UserNotFoundException("User not found with email: " + email));

    return UserMapper.toDTO(user);
  }

}
