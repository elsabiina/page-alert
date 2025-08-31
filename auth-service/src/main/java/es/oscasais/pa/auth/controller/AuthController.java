package es.oscasais.pa.auth.controller;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.oscasais.pa.auth.dto.LoginRequestDTO;
import es.oscasais.pa.auth.dto.UserInfoDTO;
import es.oscasais.pa.auth.service.AuthService;
import es.oscasais.pa.auth.service.CookieService;
import es.oscasais.pa.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "API for managing Authentication")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;
  private final UserService userService;
  private final CookieService cookieService;

  @Autowired
  public AuthController(AuthService authService, UserService userService, CookieService cookieService) {
    this.authService = authService;
    this.userService = userService;
    this.cookieService = cookieService;
  }

  @Operation(summary = "Generate token on user login and set secure cookie")
  @PostMapping("/login")
  public ResponseEntity<Map<String, String>> login(
      @Valid @RequestBody LoginRequestDTO loginRequestDTO,
      HttpServletRequest request,
      HttpServletResponse response) {

    logger.info("Login attempt for user: {}", loginRequestDTO.getEmail());

    Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);

    if (tokenOptional.isEmpty()) {
      logger.warn("Login failed for user: {}", loginRequestDTO.getEmail());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String token = tokenOptional.get();
    
    // Set JWT token as secure HTTP-only cookie
    cookieService.setJwtCookie(response, token);
    
    // Create user session for tracking
    authService.createUserSession(loginRequestDTO, token, request);

    logger.info("Login successful for user: {}, JWT cookie set", loginRequestDTO.getEmail());

    // Return success message (no token in body for security)
    return ResponseEntity.ok(Map.of(
        "message", "Login successful", 
        "cookieName", cookieService.getCookieName()
    ));
  }

  @Operation(summary = "Validate Token")
  @GetMapping("/validate")
  public ResponseEntity<Void> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {

    // Authorization: Bearer <token>
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return authService.validateToken(authHeader.substring(7))
        ? ResponseEntity.ok().build()
        : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @Operation(summary = "Logout user and clear JWT cookie")
  @PostMapping("/logout")
  public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
    logger.info("User logout requested");
    
    // Clear the JWT cookie
    cookieService.clearJwtCookie(response);
    
    logger.info("User logout successful, JWT cookie cleared");
    
    return ResponseEntity.ok(Map.of("message", "Logout successful"));
  }

  @Operation(summary = "Creates a user account and sends email confirmation")
  @PostMapping("/create-me")
  public ResponseEntity<String> createUser(
      @Valid @RequestBody LoginRequestDTO loginRequestDTO,
      HttpServletRequest request) {

    Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);

    if (!tokenOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
    }

    userService.createUser(loginRequestDTO, request);

    return ResponseEntity.ok("User created successfully. Please check your email to confirm your account.");
  }

  @Operation(summary = "Get current user information from valid session")
  @GetMapping("/me")
  public ResponseEntity<UserInfoDTO> getCurrentUser(HttpServletRequest request) {
    logger.debug("Getting current user information from session");

    Optional<UserInfoDTO> userInfo = authService.getCurrentUser(request);
    
    if (userInfo.isEmpty()) {
      logger.debug("No valid session found or user not authenticated");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    logger.debug("Current user information retrieved successfully for user: {}", userInfo.get().getEmail());
    return ResponseEntity.ok(userInfo.get());
  }
}
