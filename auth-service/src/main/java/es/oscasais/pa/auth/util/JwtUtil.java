package es.oscasais.pa.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import es.oscasais.pa.auth.model.User;

/**
 * Utility class for JWT operations
 * Handles token generation and validation for authentication
 */
@Component
public class JwtUtil {

  private final Key secretKey;
  private final long tokenValidityInMilliseconds;

  public JwtUtil(@Value("${jwt.secret}") String secret,
                 @Value("${jwt.cookie.max-age:3600}") int maxAgeInSeconds) {
    // Convert string secret to secure key for HMAC
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.tokenValidityInMilliseconds = maxAgeInSeconds * 1000L; // Convert to milliseconds
    
    // Debug logging
    System.out.println("AUTH-SERVICE JWT_SECRET length: " + secret.length() + ", first 10 chars: " + secret.substring(0, Math.min(10, secret.length())));
  }

  /**
   * Generates JWT token with user information
   * @param user the authenticated user
   * @return JWT token string
   */
  public String generateToken(User user) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);
    
    return Jwts.builder()
        .subject(user.getId().toString()) // Use user ID as subject
        .claim("email", user.getEmail())
        .claim("iat", now.getTime() / 1000) // Issued at timestamp
        .issuedAt(now)
        .expiration(validity)
        .signWith(secretKey)
        .compact();
  }

  /**
   * Generates JWT token with email (for backward compatibility)
   * @param email user email
   * @return JWT token string
   */
  public String generateToken(String email) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);
    
    return Jwts.builder()
        .subject(email) // Use email as subject for backward compatibility
        .claim("email", email)
        .claim("iat", now.getTime() / 1000)
        .issuedAt(now)
        .expiration(validity)
        .signWith(secretKey)
        .compact();
  }

  /**
   * Validates JWT token
   * @param token JWT token to validate
   * @throws JwtException if token is invalid
   */
  public void validateToken(String token) {
    try {
      Jwts.parser()
          .verifyWith((SecretKey) secretKey)
          .build()
          .parseSignedClaims(token);
    } catch (SignatureException e) {
      throw new JwtException("Invalid JWT signature");
    } catch (JwtException e) {
      throw new JwtException("Invalid JWT token: " + e.getMessage());
    }
  }

  /**
   * Gets token validity duration in milliseconds
   * @return token validity duration
   */
  public long getTokenValidityInMilliseconds() {
    return tokenValidityInMilliseconds;
  }

  /**
   * Extracts email from JWT token
   * @param token JWT token
   * @return Optional containing email if valid, empty if invalid
   */
  public Optional<String> extractEmail(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith((SecretKey) secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
      
      return Optional.ofNullable(claims.get("email", String.class));
    } catch (JwtException e) {
      return Optional.empty();
    }
  }

  /**
   * Extracts subject (user ID or email) from JWT token
   * @param token JWT token
   * @return Optional containing subject if valid, empty if invalid
   */
  public Optional<String> extractSubject(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith((SecretKey) secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
      
      return Optional.ofNullable(claims.getSubject());
    } catch (JwtException e) {
      return Optional.empty();
    }
  }

  /**
   * Extracts all claims from JWT token
   * @param token JWT token
   * @return Optional containing claims if valid, empty if invalid
   */
  public Optional<Claims> extractClaims(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith((SecretKey) secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
      
      return Optional.of(claims);
    } catch (JwtException e) {
      return Optional.empty();
    }
  }
}
