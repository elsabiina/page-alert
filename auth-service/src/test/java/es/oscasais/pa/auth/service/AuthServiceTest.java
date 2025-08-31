package es.oscasais.pa.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.oscasais.pa.auth.dto.UserInfoDTO;
import es.oscasais.pa.auth.kafka.KafkaProducer;
import es.oscasais.pa.auth.model.User;
import es.oscasais.pa.auth.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private CookieService cookieService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthService authService;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    private final String COOKIE_NAME = "authToken";
    private final UUID TEST_USER_ID = UUID.randomUUID();

    private User testUser;
    private Cookie[] cookies;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setEmailConfirmed(true);

        cookies = new Cookie[]{new Cookie(COOKIE_NAME, TEST_JWT_TOKEN)};
    }

    @Test
    void getCurrentUser_WithValidCookieAndUser_ShouldReturnUserInfo() {
        // Given: Valid JWT cookie and confirmed user
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.getCookieName()).thenReturn(COOKIE_NAME);
        when(jwtUtil.extractEmail(TEST_JWT_TOKEN)).thenReturn(Optional.of(TEST_EMAIL));
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When: Get current user
        Optional<UserInfoDTO> result = authService.getCurrentUser(request);

        // Then: Should return user info
        assertTrue(result.isPresent());
        UserInfoDTO userInfo = result.get();
        assertEquals(TEST_USER_ID, userInfo.getId());
        assertEquals(TEST_EMAIL, userInfo.getEmail());
        assertTrue(userInfo.isEmailConfirmed());

        // Verify service interactions
        verify(jwtUtil).validateToken(TEST_JWT_TOKEN);
        verify(jwtUtil).extractEmail(TEST_JWT_TOKEN);
        verify(userService).findByEmail(TEST_EMAIL);
    }

    @Test
    void getCurrentUser_WithNoCookies_ShouldReturnEmpty() {
        // Given: No cookies in request
        when(request.getCookies()).thenReturn(null);

        // When: Get current user
        Optional<UserInfoDTO> result = authService.getCurrentUser(request);

        // Then: Should return empty
        assertTrue(result.isEmpty());

        // Verify no service interactions
        verify(jwtUtil, never()).validateToken(any());
        verify(userService, never()).findByEmail(any());
    }

    @Test
    void getCurrentUser_WithNoJwtCookie_ShouldReturnEmpty() {
        // Given: No JWT cookie in request
        Cookie[] otherCookies = new Cookie[]{new Cookie("other", "value")};
        when(request.getCookies()).thenReturn(otherCookies);
        when(cookieService.getCookieName()).thenReturn(COOKIE_NAME);

        // When: Get current user
        Optional<UserInfoDTO> result = authService.getCurrentUser(request);

        // Then: Should return empty
        assertTrue(result.isEmpty());

        // Verify no service interactions
        verify(jwtUtil, never()).validateToken(any());
        verify(userService, never()).findByEmail(any());
    }

    @Test
    void getCurrentUser_WithInvalidJwtToken_ShouldReturnEmpty() {
        // Given: Invalid JWT token
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.getCookieName()).thenReturn(COOKIE_NAME);
        doThrow(new JwtException("Invalid token")).when(jwtUtil).validateToken(TEST_JWT_TOKEN);

        // When: Get current user
        Optional<UserInfoDTO> result = authService.getCurrentUser(request);

        // Then: Should return empty
        assertTrue(result.isEmpty());

        // Verify service interactions
        verify(jwtUtil).validateToken(TEST_JWT_TOKEN);
        verify(jwtUtil, never()).extractEmail(any());
        verify(userService, never()).findByEmail(any());
    }

    @Test
    void getCurrentUser_WithNoEmailInToken_ShouldReturnEmpty() {
        // Given: JWT token without email claim
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.getCookieName()).thenReturn(COOKIE_NAME);
        when(jwtUtil.extractEmail(TEST_JWT_TOKEN)).thenReturn(Optional.empty());

        // When: Get current user
        Optional<UserInfoDTO> result = authService.getCurrentUser(request);

        // Then: Should return empty
        assertTrue(result.isEmpty());

        // Verify service interactions
        verify(jwtUtil).validateToken(TEST_JWT_TOKEN);
        verify(jwtUtil).extractEmail(TEST_JWT_TOKEN);
        verify(userService, never()).findByEmail(any());
    }

    @Test
    void getCurrentUser_WithUserNotFound_ShouldReturnEmpty() {
        // Given: User not found in database
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.getCookieName()).thenReturn(COOKIE_NAME);
        when(jwtUtil.extractEmail(TEST_JWT_TOKEN)).thenReturn(Optional.of(TEST_EMAIL));
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When: Get current user
        Optional<UserInfoDTO> result = authService.getCurrentUser(request);

        // Then: Should return empty
        assertTrue(result.isEmpty());

        // Verify service interactions
        verify(jwtUtil).validateToken(TEST_JWT_TOKEN);
        verify(jwtUtil).extractEmail(TEST_JWT_TOKEN);
        verify(userService).findByEmail(TEST_EMAIL);
    }

    @Test
    void getCurrentUser_WithEmailNotConfirmed_ShouldReturnEmpty() {
        // Given: User with unconfirmed email
        testUser.setEmailConfirmed(false);
        when(request.getCookies()).thenReturn(cookies);
        when(cookieService.getCookieName()).thenReturn(COOKIE_NAME);
        when(jwtUtil.extractEmail(TEST_JWT_TOKEN)).thenReturn(Optional.of(TEST_EMAIL));
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When: Get current user
        Optional<UserInfoDTO> result = authService.getCurrentUser(request);

        // Then: Should return empty
        assertTrue(result.isEmpty());

        // Verify service interactions
        verify(jwtUtil).validateToken(TEST_JWT_TOKEN);
        verify(jwtUtil).extractEmail(TEST_JWT_TOKEN);
        verify(userService).findByEmail(TEST_EMAIL);
    }

    @Test
    void getCurrentUser_WithMultipleCookies_ShouldFindCorrectOne() {
        // Given: Multiple cookies including JWT
        Cookie[] multipleCookies = new Cookie[]{
            new Cookie("session", "value1"),
            new Cookie(COOKIE_NAME, TEST_JWT_TOKEN),
            new Cookie("preferences", "value2")
        };
        when(request.getCookies()).thenReturn(multipleCookies);
        when(cookieService.getCookieName()).thenReturn(COOKIE_NAME);
        when(jwtUtil.extractEmail(TEST_JWT_TOKEN)).thenReturn(Optional.of(TEST_EMAIL));
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When: Get current user
        Optional<UserInfoDTO> result = authService.getCurrentUser(request);

        // Then: Should return user info
        assertTrue(result.isPresent());
        assertEquals(TEST_EMAIL, result.get().getEmail());

        // Verify service interactions
        verify(jwtUtil).validateToken(TEST_JWT_TOKEN);
        verify(jwtUtil).extractEmail(TEST_JWT_TOKEN);
        verify(userService).findByEmail(TEST_EMAIL);
    }
}