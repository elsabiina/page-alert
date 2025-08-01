package es.oscasais.pa.userService.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "API for managing Users")
public class UserController {
  // TODO: now any logged in user can do anything. Need to implement some Auth logic.
  //
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
  @Operation(summary = "Get an user by id")
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

  @GetMapping("/me")
  @Operation(summary = "Get the user logged in account info")
  public ResponseEntity<UserResponseDTO> getCurrentUser(HttpServletRequest request) {
    HttpSession session = request.getSession();

    if (session == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    String email = (String) session.getAttribute("user");
    UserResponseDTO user = userService.findUserByEmail(email);

    return ResponseEntity.ok().body(user);
  }

  @PostMapping("/create-user-account")
  @Operation(summary = "Create a new User Account", description="this endping is public")
  public ResponseEntity<UserResponseDTO> createUserAccount(@Valid @RequestBody UserRequestDTO userRequestDTO) {
    return createUser(userRequestDTO);
  }
}
