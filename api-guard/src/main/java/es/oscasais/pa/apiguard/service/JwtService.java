package es.oscasais.pa.apiguard.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Service to handle JWT operations
 * - Token validation
 * - Claims extraction (user data)
 * - Expiration verification
 */
@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    private final SecretKey secretKey;
    
    /**
     * Constructor that initializes the secret key from configuration
     */
    public JwtService(@Value("${jwt.secret}") String jwtSecret) {
        // Convert the string secret into a secure SecretKey for HMAC
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        logger.info("JwtService initialized successfully");
        logger.info("JWT_SECRET length: {}, first 10 chars: {}", jwtSecret.length(), jwtSecret.substring(0, Math.min(10, jwtSecret.length())));
    }
    
    /**
     * Validates a JWT token and returns the claims if valid
     * @param token the JWT token without "Bearer " prefix
     * @return Claims from the token if valid
     * @throws JwtException if the token is invalid or expired
     */
    public Claims validateTokenAndGetClaims(String token) {
        try {
            // Parse and validate the token using the secret key
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)  // Verify signature
                    .build()
                    .parseSignedClaims(token)  // Parse and validate
                    .getPayload();
            
            // Additional expiration check (for extra security)
            if (claims.getExpiration().before(new Date())) {
                throw new JwtException("Token has expired");
            }
            
            logger.debug("Token validated successfully for user: {}", claims.getSubject());
            return claims;
            
        } catch (JwtException e) {
            logger.warn("Invalid token: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Extracts the user ID from the token
     * @param token the JWT token
     * @return the user ID or null if not found
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = validateTokenAndGetClaims(token);
            return claims.getSubject(); // Usually "sub" contains the user ID
        } catch (JwtException e) {
            logger.warn("Could not extract user ID from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Checks if a token is valid without throwing exceptions
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            validateTokenAndGetClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}