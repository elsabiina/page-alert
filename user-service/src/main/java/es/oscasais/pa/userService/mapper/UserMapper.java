package es.oscasais.pa.userService.mapper;

import es.oscasais.pa.userService.dto.UserRequestDTO;
import es.oscasais.pa.userService.dto.UserResponseDTO;
import es.oscasais.pa.userService.model.User;

public class UserMapper {
  public static UserResponseDTO toDTO(User user) {
    UserResponseDTO userResponseDTO = new UserResponseDTO();
    userResponseDTO.setId(user.getId().toString());
    userResponseDTO.setEmail(user.getEmail());
    userResponseDTO.setAlias(user.getAlias());
    userResponseDTO.setAvatarUrl(user.getAvatarUrl());
    return userResponseDTO;
  }

  public static User toModel(UserRequestDTO userRequestDTO) {
    User user = new User();
    user.setEmail(userRequestDTO.getEmail());
    user.setAlias(userRequestDTO.getAlias());
    user.setAvatarUrl(userRequestDTO.getAvatarUrl());
    return user;
  }
}
