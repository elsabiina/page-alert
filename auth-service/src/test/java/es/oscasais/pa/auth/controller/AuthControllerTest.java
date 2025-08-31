package es.oscasais.pa.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.oscasais.pa.auth.dto.LoginRequestDTO;
import es.oscasais.pa.auth.dto.UserInfoDTO;
import es.oscasais.pa.auth.exception.AuthenticationException;
import es.oscasais.pa.auth.service.AuthService;
import es.oscasais.pa.auth.service.CookieService;
import es.oscasais.pa.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Integration tests for AuthController
 * Tests the cookie-based authentication flow
 */
@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CookieService cookieService;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PASSWORD = "testPassword123";
    private final String TEST_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    private final String COOKIE_NAME = "authToken";
    private final UUID TEST_USER_ID = UUID.randomUUID();

    @Test
    void testLogin_WithValidCredentials_ShouldSetCookieAndReturnSuccess() throws Exception {
        // Given: Valid login request and successful authentication
        LoginRequestDTO loginRequest = new LoginRequestDTO(TEST_EMAIL, TEST_PASSWORD);
        when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(Optional.of(TEST_JWT_TOKEN));
        when(cookieService.getCookieName()).thenReturn(COOKIE_NAME);

        // When: Perform login request
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Then: Should return success and set cookie
        result.andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.message").value("Login successful"))
              .andExpect(jsonPath("$.cookieName").value(COOKIE_NAME));

        // Verify service interactions
        verify(authService).authenticate(any(LoginRequestDTO.class));
        verify(cookieService).setJwtCookie(any(HttpServletResponse.class), eq(TEST_JWT_TOKEN));
        verify(authService).createUserSession(any(LoginRequestDTO.class), eq(TEST_JWT_TOKEN), any(HttpServletRequest.class));
    }

    @Test
    void testLogin_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given: Invalid login request
        LoginRequestDTO loginRequest = new LoginRequestDTO(TEST_EMAIL, "wrongPassword");
        when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(Optional.empty());

        // When: Perform login request
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Then: Should return unauthorized
        result.andExpect(status().isUnauthorized());

        // Verify service interactions
        verify(authService).authenticate(any(LoginRequestDTO.class));
        verify(cookieService, never()).setJwtCookie(any(), any());
        verify(authService, never()).createUserSession(any(), any(), any());
    }

    @Test
    void testLogin_WithInvalidRequestBody_ShouldReturnBadRequest() throws Exception {
        // Given: Invalid JSON request
        String invalidJson = "{\"email\":\"invalid-email\",\"password\":\"\"}";

        // When: Perform login request with invalid data
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson));

        // Then: Should return bad request
        result.andExpect(status().isBadRequest());

        // Verify no service interactions
        verify(authService, never()).authenticate(any());
        verify(cookieService, never()).setJwtCookie(any(), any());
    }

    @Test
    void testValidateToken_WithValidBearerToken_ShouldReturnOk() throws Exception {
        // Given: Valid bearer token
        String bearerToken = "Bearer " + TEST_JWT_TOKEN;
        when(authService.validateToken(TEST_JWT_TOKEN)).thenReturn(true);

        // When: Perform token validation
        ResultActions result = mockMvc.perform(get("/auth/validate")
                .header("Authorization", bearerToken));

        // Then: Should return ok
        result.andExpect(status().isOk());

        // Verify service interaction
        verify(authService).validateToken(TEST_JWT_TOKEN);
    }

    @Test
    void testValidateToken_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Given: Invalid bearer token
        String bearerToken = "Bearer invalidToken";
        when(authService.validateToken("invalidToken")).thenReturn(false);

        // When: Perform token validation
        ResultActions result = mockMvc.perform(get("/auth/validate")
                .header("Authorization", bearerToken));

        // Then: Should return unauthorized
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void testValidateToken_WithoutBearerPrefix_ShouldReturnUnauthorized() throws Exception {
        // Given: Token without Bearer prefix
        String invalidHeader = TEST_JWT_TOKEN;

        // When: Perform token validation
        ResultActions result = mockMvc.perform(get("/auth/validate")
                .header("Authorization", invalidHeader));

        // Then: Should return unauthorized
        result.andExpect(status().isUnauthorized());

        // Verify no service interaction
        verify(authService, never()).validateToken(any());
    }

    @Test
    void testValidateToken_WithoutAuthorizationHeader_ShouldReturnUnauthorized() throws Exception {
        // When: Perform token validation without header
        ResultActions result = mockMvc.perform(get("/auth/validate"));

        // Then: Should return unauthorized
        result.andExpect(status().isUnauthorized());

        // Verify no service interaction
        verify(authService, never()).validateToken(any());
    }

    @Test
    void testLogout_ShouldClearCookieAndReturnSuccess() throws Exception {
        // When: Perform logout request
        ResultActions result = mockMvc.perform(post("/auth/logout"));

        // Then: Should return success
        result.andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.message").value("Logout successful"));

        // Verify cookie is cleared
        verify(cookieService).clearJwtCookie(any(HttpServletResponse.class));
    }

    @Test
    void testCreateUser_WithNewUser_ShouldReturnSuccess() throws Exception {
        // Given: New user request and no existing authentication
        LoginRequestDTO createUserRequest = new LoginRequestDTO(TEST_EMAIL, TEST_PASSWORD);
        when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(Optional.empty());

        // When: Perform create user request
        ResultActions result = mockMvc.perform(post("/auth/create-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)));

        // Then: Should return success
        result.andExpect(status().isOk())
              .andExpect(content().string("User created successfully. Please check your email to confirm your account."));

        // Verify service interactions
        verify(authService).authenticate(any(LoginRequestDTO.class));
        verify(userService).createUser(any(LoginRequestDTO.class), any(HttpServletRequest.class));
    }

    @Test
    void testCreateUser_WithExistingUser_ShouldReturnAlreadyReported() throws Exception {
        // Given: Existing user request
        LoginRequestDTO createUserRequest = new LoginRequestDTO(TEST_EMAIL, TEST_PASSWORD);
        when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(Optional.of(TEST_JWT_TOKEN));

        // When: Perform create user request
        ResultActions result = mockMvc.perform(post("/auth/create-me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)));

        // Then: Should return already reported
        result.andExpect(status().isAlreadyReported());

        // Verify service interactions
        verify(authService).authenticate(any(LoginRequestDTO.class));
        verify(userService, never()).createUser(any(), any());
    }

    @Test
    void testLogin_ShouldLogUserActivity() throws Exception {
        // Given: Valid login request
        LoginRequestDTO loginRequest = new LoginRequestDTO(TEST_EMAIL, TEST_PASSWORD);
        when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(Optional.of(TEST_JWT_TOKEN));
        when(cookieService.getCookieName()).thenReturn(COOKIE_NAME);

        // When: Perform login request
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Then: Should log the activity (verify through service calls)
        verify(authService).authenticate(any(LoginRequestDTO.class));
        verify(authService).createUserSession(any(LoginRequestDTO.class), eq(TEST_JWT_TOKEN), any(HttpServletRequest.class));
    }

    @Test
    void testLogin_WithAuthServiceException_ShouldReturnInternalServerError() throws Exception {
        // Given: AuthService throws authentication exception
        LoginRequestDTO loginRequest = new LoginRequestDTO(TEST_EMAIL, TEST_PASSWORD);
        when(authService.authenticate(any(LoginRequestDTO.class)))
            .thenThrow(new AuthenticationException("Database error during authentication"));

        // When: Perform login request
        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Then: Should return internal server error with proper error message
        result.andExpect(status().isInternalServerError())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.message").value("Authentication failed due to system error"));

        // Verify no cookie is set
        verify(cookieService, never()).setJwtCookie(any(), any());
    }

    @Test
    void testGetCurrentUser_WithValidSession_ShouldReturnUserInfo() throws Exception {
        // Given: Valid user session
        UserInfoDTO userInfo = new UserInfoDTO(TEST_USER_ID, TEST_EMAIL, true);
        when(authService.getCurrentUser(any(HttpServletRequest.class))).thenReturn(Optional.of(userInfo));

        // When: Perform get current user request
        ResultActions result = mockMvc.perform(get("/auth/me"));

        // Then: Should return user info
        result.andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.id").value(TEST_USER_ID.toString()))
              .andExpect(jsonPath("$.email").value(TEST_EMAIL))
              .andExpect(jsonPath("$.emailConfirmed").value(true));

        // Verify service interaction
        verify(authService).getCurrentUser(any(HttpServletRequest.class));
    }

    @Test
    void testGetCurrentUser_WithInvalidSession_ShouldReturnUnauthorized() throws Exception {
        // Given: Invalid user session
        when(authService.getCurrentUser(any(HttpServletRequest.class))).thenReturn(Optional.empty());

        // When: Perform get current user request
        ResultActions result = mockMvc.perform(get("/auth/me"));

        // Then: Should return unauthorized
        result.andExpect(status().isUnauthorized());

        // Verify service interaction
        verify(authService).getCurrentUser(any(HttpServletRequest.class));
    }

    @Test
    void testGetCurrentUser_WithNoJwtCookie_ShouldReturnUnauthorized() throws Exception {
        // Given: No JWT cookie in request
        when(authService.getCurrentUser(any(HttpServletRequest.class))).thenReturn(Optional.empty());

        // When: Perform get current user request
        ResultActions result = mockMvc.perform(get("/auth/me"));

        // Then: Should return unauthorized
        result.andExpect(status().isUnauthorized());

        // Verify service interaction
        verify(authService).getCurrentUser(any(HttpServletRequest.class));
    }

    @Test
    void testGetCurrentUser_WithExpiredToken_ShouldReturnUnauthorized() throws Exception {
        // Given: Expired JWT token
        when(authService.getCurrentUser(any(HttpServletRequest.class))).thenReturn(Optional.empty());

        // When: Perform get current user request
        ResultActions result = mockMvc.perform(get("/auth/me"));

        // Then: Should return unauthorized
        result.andExpect(status().isUnauthorized());

        // Verify service interaction
        verify(authService).getCurrentUser(any(HttpServletRequest.class));
    }
}
