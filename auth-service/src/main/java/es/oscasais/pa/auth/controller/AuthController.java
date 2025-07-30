package es.oscasais.pa.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import es.oscasais.pa.auth.dto.LoginRequestDTO;
import es.oscasais.pa.auth.dto.LoginResponseDTO;
import es.oscasais.pa.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private final AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(summary = "Generate token on user login")
  @PostMapping("/login")
  public ResponseEntity<LoginResponseDTO> login(
      @Valid @RequestBody LoginRequestDTO loginRequestDTO,
      HttpServletRequest request) {

    Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);

    if (tokenOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String token = tokenOptional.get();
    authService.createUserSession(loginRequestDTO, token, request);

    return ResponseEntity.ok(new LoginResponseDTO(token));
  }

  @Operation(summary = "Validate Token")
  @GetMapping("/validate")
  public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {

    // Authorization: Bearer <token>
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return authService.validateToken(authHeader.substring(7))
        ? ResponseEntity.ok().build()
        : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @Operation(summary = "Creates an user account and login auth")
  @PostMapping("/create-me")
  public ResponseEntity<LoginResponseDTO> createUser(
      @Valid @RequestBody LoginRequestDTO loginRequestDTO,
      HttpServletRequest request) {

    Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);

    // User found on DB
    if (!tokenOptional.isEmpty()) {

      return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
    }

    // create auhtorized user
    authService.createUser(loginRequestDTO, request);
    tokenOptional = authService.authenticate(loginRequestDTO);

    return ResponseEntity.ok(new LoginResponseDTO(tokenOptional.get()));
  }
}
