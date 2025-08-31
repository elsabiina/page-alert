package es.oscasais.pa.userService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.oscasais.pa.userService.dto.UserResponseDTO;
import es.oscasais.pa.userService.exception.UserNotFoundException;
import es.oscasais.pa.userService.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.oscasais.pa.userService.exception.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for UserController focusing on the /users/me endpoint.
 * 
 * This test class validates the email-based user lookup functionality
 * introduced for the /users/me endpoint, ensuring proper handling of
 * X-User-Email headers and various error scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController /users/me endpoint tests")
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    private UserResponseDTO validUserResponse;
    private static final String VALID_EMAIL = "user@example.com";
    private static final String USER_ID = "123e4567-e89b-12d3-a456-426614174000";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        
        validUserResponse = new UserResponseDTO();
        validUserResponse.setId(USER_ID);
        validUserResponse.setEmail(VALID_EMAIL);
        validUserResponse.setAlias("Test User");
        validUserResponse.setAvatarUrl("https://example.com/avatar.jpg");
    }

    @Test
    @DisplayName("Should successfully return user info when X-User-Email header is valid")
    void getCurrentUser_WithValidEmail_ShouldReturnUserInfo() throws Exception {
        // Given
        when(userService.findUserByEmail(VALID_EMAIL)).thenReturn(validUserResponse);

        // When & Then
        mockMvc.perform(get("/users/me")
                .header("X-User-Email", VALID_EMAIL)
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.email").value(VALID_EMAIL))
                .andExpect(jsonPath("$.alias").value("Test User"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when X-User-Email header is missing")
    void getCurrentUser_WithMissingEmailHeader_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/me")
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when X-User-Email header is null")
    void getCurrentUser_WithNullEmailHeader_ShouldReturnUnauthorized() throws Exception {
        // When & Then - Note: null headers are not included in MockMvc, so this tests missing header behavior
        mockMvc.perform(get("/users/me")
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when X-User-Email header is empty string")
    void getCurrentUser_WithEmptyEmailHeader_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/me")
                .header("X-User-Id", USER_ID)
                .header("X-User-Email", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when X-User-Email header is only whitespace")
    void getCurrentUser_WithWhitespaceOnlyEmailHeader_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/me")
                .header("X-User-Id", USER_ID)
                .header("X-User-Email", "   ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when user is not found by email")
    void getCurrentUser_WithNonExistentEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(userService.findUserByEmail(nonExistentEmail))
                .thenThrow(new UserNotFoundException("User not found with email: " + nonExistentEmail));

        // When & Then
        mockMvc.perform(get("/users/me")
                .header("X-User-Email", nonExistentEmail)
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when unexpected service exception occurs")
    void getCurrentUser_WithUnexpectedServiceException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(userService.findUserByEmail(VALID_EMAIL))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/users/me")
                .header("X-User-Email", VALID_EMAIL)
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should successfully process request with only X-User-Email header (X-User-Id is optional)")
    void getCurrentUser_WithOnlyEmailHeader_ShouldReturnUserInfo() throws Exception {
        // Given
        when(userService.findUserByEmail(VALID_EMAIL)).thenReturn(validUserResponse);

        // When & Then
        mockMvc.perform(get("/users/me")
                .header("X-User-Email", VALID_EMAIL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.email").value(VALID_EMAIL));
    }

    @Test
    @DisplayName("Should handle special characters in email header correctly")
    void getCurrentUser_WithSpecialCharactersInEmail_ShouldReturnUserInfo() throws Exception {
        // Given
        String emailWithSpecialChars = "user+test@example-domain.com";
        UserResponseDTO userWithSpecialEmail = new UserResponseDTO();
        userWithSpecialEmail.setId(USER_ID);
        userWithSpecialEmail.setEmail(emailWithSpecialChars);
        userWithSpecialEmail.setAlias("Special User");

        when(userService.findUserByEmail(emailWithSpecialChars)).thenReturn(userWithSpecialEmail);

        // When & Then
        mockMvc.perform(get("/users/me")
                .header("X-User-Email", emailWithSpecialChars)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(emailWithSpecialChars));
    }

    @Test
    @DisplayName("Should handle case sensitivity in email lookup")
    void getCurrentUser_WithDifferentCaseEmail_ShouldCallServiceWithExactCase() throws Exception {
        // Given
        String mixedCaseEmail = "User.Test@Example.COM";
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setId(USER_ID);
        userResponse.setEmail(mixedCaseEmail);
        userResponse.setAlias("Mixed Case User");

        when(userService.findUserByEmail(mixedCaseEmail)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/users/me")
                .header("X-User-Email", mixedCaseEmail)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(mixedCaseEmail));
    }
}