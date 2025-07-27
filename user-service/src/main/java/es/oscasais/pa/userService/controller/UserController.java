package es.oscasais.pa.userService.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.oscasais.pa.userService.dto.UserRequestDTO;
import es.oscasais.pa.userService.dto.UserResponseDTO;
import es.oscasais.pa.userService.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "API for managing Users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  @Operation(summary = "Get All Users (not paginated)")
  public ResponseEntity<List<UserResponseDTO>> getUsers() {
    List<UserResponseDTO> users = userService.findAll();

    return ResponseEntity.ok().body(users);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Ge an user by id")
  public ResponseEntity<UserResponseDTO> getUser(@PathVariable UUID id) {
    UserResponseDTO user = userService.findUser(id);

    return ResponseEntity.ok().body(user);
  }

  @PostMapping
  @Operation(summary = "Create a new User")
  public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
    UserResponseDTO user = userService.createUser(userRequestDTO);

    return ResponseEntity.ok().body(user);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing User")
  public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id,
      @Validated({ Default.class }) @RequestBody UserRequestDTO userRequestDTO) {

    UserResponseDTO userResponseDTO = userService.updateUser(id,
        userRequestDTO);

    return ResponseEntity.ok().body(userResponseDTO);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete an existing User")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
