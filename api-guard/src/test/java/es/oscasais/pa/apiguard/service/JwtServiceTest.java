package es.oscasais.pa.apiguard.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService
 * Tests JWT validation, user extraction, and error handling
 */
class JwtServiceTest {
    
    private JwtService jwtService;
    private SecretKey testSecretKey;
    private final String TEST_SECRET = "test-secret-key-for-unit-tests-must-be-long-enough-for-hmac-256";
    
    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET);
        testSecretKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
    }
    
    @Test
    void testValidateTokenAndGetClaims_ValidToken_ShouldReturnClaims() {
        // Given: Create a valid JWT token
        String validToken = createTestToken("user123", "test@example.com", 1000 * 60 * 15); // 15 min expiry
        
        // When: Validate the token
        Claims claims = jwtService.validateTokenAndGetClaims(validToken);
        
        // Then: Should return valid claims
        assertNotNull(claims);
        assertEquals("user123", claims.getSubject());
        assertEquals("test@example.com", claims.get("email", String.class));
        assertTrue(claims.getExpiration().after(new Date()));
    }
    
    @Test
    void testValidateTokenAndGetClaims_ExpiredToken_ShouldThrowException() {
        // Given: Create an expired JWT token
        String expiredToken = createTestToken("user123", "test@example.com", -1000); // Expired 1 sec ago
        
        // When & Then: Should throw JwtException
        JwtException exception = assertThrows(JwtException.class, () -> {
            jwtService.validateTokenAndGetClaims(expiredToken);
        });
        
        assertTrue(exception.getMessage().contains("expired"));
    }
    
    @Test
    void testValidateTokenAndGetClaims_InvalidSignature_ShouldThrowException() {
        // Given: Create a token with wrong signature
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong-secret-key-different-from-test-secret".getBytes());
        String invalidToken = Jwts.builder()
                .subject("user123")
                .claim("email", "test@example.com")
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .signWith(wrongKey)
                .compact();
        
        // When & Then: Should throw JwtException
        assertThrows(JwtException.class, () -> {
            jwtService.validateTokenAndGetClaims(invalidToken);
        });
    }
    
    @Test
    void testValidateTokenAndGetClaims_MalformedToken_ShouldThrowException() {
        // Given: Malformed token
        String malformedToken = "this.is.not.a.valid.jwt.token";
        
        // When & Then: Should throw JwtException
        assertThrows(JwtException.class, () -> {
            jwtService.validateTokenAndGetClaims(malformedToken);
        });
    }
    
    @Test
    void testGetUserIdFromToken_ValidToken_ShouldReturnUserId() {
        // Given: Create a valid JWT token
        String validToken = createTestToken("user456", "user456@example.com", 1000 * 60 * 15);
        
        // When: Extract user ID
        String userId = jwtService.getUserIdFromToken(validToken);
        
        // Then: Should return correct user ID
        assertEquals("user456", userId);
    }
    
    @Test
    void testGetUserIdFromToken_InvalidToken_ShouldReturnNull() {
        // Given: Invalid token
        String invalidToken = "invalid.jwt.token";
        
        // When: Extract user ID
        String userId = jwtService.getUserIdFromToken(invalidToken);
        
        // Then: Should return null
        assertNull(userId);
    }
    
    @Test
    void testIsTokenValid_ValidToken_ShouldReturnTrue() {
        // Given: Create a valid JWT token
        String validToken = createTestToken("user789", "user789@example.com", 1000 * 60 * 15);
        
        // When: Check if token is valid
        boolean isValid = jwtService.isTokenValid(validToken);
        
        // Then: Should return true
        assertTrue(isValid);
    }
    
    @Test
    void testIsTokenValid_InvalidToken_ShouldReturnFalse() {
        // Given: Invalid token
        String invalidToken = "invalid.jwt.token";
        
        // When: Check if token is valid
        boolean isValid = jwtService.isTokenValid(invalidToken);
        
        // Then: Should return false
        assertFalse(isValid);
    }
    
    @Test
    void testIsTokenValid_ExpiredToken_ShouldReturnFalse() {
        // Given: Create an expired JWT token
        String expiredToken = createTestToken("user999", "user999@example.com", -1000);
        
        // When: Check if token is valid
        boolean isValid = jwtService.isTokenValid(expiredToken);
        
        // Then: Should return false
        assertFalse(isValid);
    }
    
    /**
     * Helper method to create test JWT tokens
     */
    private String createTestToken(String userId, String email, long expiryOffsetMs) {
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryOffsetMs))
                .signWith(testSecretKey)
                .compact();
    }
}