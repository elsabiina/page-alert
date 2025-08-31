package es.oscasais.pa.auth.util;

import es.oscasais.pa.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil
 * Tests token generation, validation, and configuration handling
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String TEST_SECRET = "test-secret-key-for-unit-tests-must-be-long-enough-for-hmac-256-algorithm";
    private final int TEST_MAX_AGE = 1800; // 30 minutes
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, TEST_MAX_AGE);
    }

    @Test
    void testGenerateTokenWithUser_ShouldCreateValidToken() {
        // Given: A test user
        User testUser = createTestUser();

        // When: Generate token with user
        String token = jwtUtil.generateToken(testUser);

        // Then: Should create valid token with user claims
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token structure (JWT has 3 parts separated by dots)
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length);
    }

    @Test
    void testGenerateTokenWithUser_ShouldContainCorrectClaims() {
        // Given: A test user
        User testUser = createTestUser();

        // When: Generate token with user
        String token = jwtUtil.generateToken(testUser);

        // Then: Should contain correct claims
        Claims claims = parseTokenClaims(token);
        
        assertEquals(testUser.getId().toString(), claims.getSubject());
        assertEquals(testUser.getEmail(), claims.get("email", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertNotNull(claims.get("iat"));
        
        // Verify expiration is correctly set
        long expectedExpiration = System.currentTimeMillis() + (TEST_MAX_AGE * 1000L);
        long actualExpiration = claims.getExpiration().getTime();
        long tolerance = 5000; // 5 second tolerance
        assertTrue(Math.abs(actualExpiration - expectedExpiration) < tolerance);
    }

    @Test
    void testGenerateTokenWithEmail_ShouldCreateValidToken() {
        // Given: A test email
        String testEmail = "test@example.com";

        // When: Generate token with email
        String token = jwtUtil.generateToken(testEmail);

        // Then: Should create valid token
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        Claims claims = parseTokenClaims(token);
        assertEquals(testEmail, claims.getSubject());
        assertEquals(testEmail, claims.get("email", String.class));
    }

    @Test
    void testValidateToken_WithValidToken_ShouldNotThrowException() {
        // Given: A valid token
        String validToken = jwtUtil.generateToken("test@example.com");

        // When & Then: Should not throw exception
        assertDoesNotThrow(() -> jwtUtil.validateToken(validToken));
    }

    @Test
    void testValidateToken_WithExpiredToken_ShouldThrowException() {
        // Given: An expired token (create with past expiration)
        String expiredToken = createExpiredToken();

        // When & Then: Should throw JwtException
        JwtException exception = assertThrows(JwtException.class, () -> {
            jwtUtil.validateToken(expiredToken);
        });
        
        assertTrue(exception.getMessage().contains("JWT"));
    }

    @Test
    void testValidateToken_WithInvalidSignature_ShouldThrowException() {
        // Given: A token signed with different key
        String invalidToken = createTokenWithWrongSignature();

        // When & Then: Should throw JwtException
        JwtException exception = assertThrows(JwtException.class, () -> {
            jwtUtil.validateToken(invalidToken);
        });
        
        assertTrue(exception.getMessage().contains("signature"));
    }

    @Test
    void testValidateToken_WithMalformedToken_ShouldThrowException() {
        // Given: Malformed token
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // When & Then: Should throw JwtException
        assertThrows(JwtException.class, () -> {
            jwtUtil.validateToken(malformedToken);
        });
    }

    @Test
    void testValidateToken_WithNullToken_ShouldThrowException() {
        // When & Then: Should throw exception for null token
        assertThrows(Exception.class, () -> {
            jwtUtil.validateToken(null);
        });
    }

    @Test
    void testValidateToken_WithEmptyToken_ShouldThrowException() {
        // When & Then: Should throw exception for empty token
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.validateToken("");
        });
    }

    @Test
    void testGetTokenValidityInMilliseconds_ShouldReturnCorrectValue() {
        // When: Get token validity
        long validity = jwtUtil.getTokenValidityInMilliseconds();

        // Then: Should return configured value in milliseconds
        assertEquals(TEST_MAX_AGE * 1000L, validity);
    }

    @Test
    void testTokenGeneration_WithDifferentUsers_ShouldCreateDifferentTokens() {
        // Given: Two different users
        User user1 = createTestUser();
        User user2 = createTestUser();
        user2.setEmail("different@example.com");

        // When: Generate tokens for both users
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        // Then: Should create different tokens
        assertNotEquals(token1, token2);
    }

    @Test
    void testTokenGeneration_WithSameUserMultipleTimes_ShouldCreateDifferentTokens() {
        // Given: Same user
        User user = createTestUser();

        // When: Generate multiple tokens (with slight delay to ensure different timestamps)
        String token1 = jwtUtil.generateToken(user);
        
        try {
            Thread.sleep(1100); // 1.1 second delay to ensure different iat timestamp (JWT uses second precision)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtUtil.generateToken(user);

        // Then: Should create different tokens (due to different iat timestamps)
        assertNotEquals(token1, token2);
    }

    /**
     * Helper method to create a test user
     */
    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword123");
        user.setEmailConfirmed(true);
        return user;
    }

    /**
     * Helper method to parse token claims using the same secret
     */
    private Claims parseTokenClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Helper method to create an expired token
     */
    private String createExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        return Jwts.builder()
                .subject("test@example.com")
                .claim("email", "test@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 10000)) // 10 seconds ago
                .expiration(new Date(System.currentTimeMillis() - 5000))  // 5 seconds ago (expired)
                .signWith(key)
                .compact();
    }

    /**
     * Helper method to create a token with wrong signature
     */
    private String createTokenWithWrongSignature() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("different-secret-key-for-wrong-signature-test".getBytes());
        return Jwts.builder()
                .subject("test@example.com")
                .claim("email", "test@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000)) // 1 minute
                .signWith(wrongKey) // Wrong key!
                .compact();
    }
}