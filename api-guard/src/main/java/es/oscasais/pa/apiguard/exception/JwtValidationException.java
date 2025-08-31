package es.oscasais.pa.apiguard.exception;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Global exception handler for JWT validation errors and authentication failures
 * Handles both legacy WebClient exceptions and new JWT cookie validation exceptions
 */
@RestControllerAdvice
@Order(-1) // High priority to handle JWT exceptions first
public class JwtValidationException {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtValidationException.class);
    
    /**
     * Handles JWT-related exceptions from cookie validation
     */
    @ExceptionHandler(JwtException.class)
    public Mono<Void> handleJwtException(JwtException ex, ServerWebExchange exchange) {
        logger.warn("JWT validation failed: {}", ex.getMessage());
        
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        String errorResponse = createErrorResponse("JWT_INVALID", "Invalid or expired JWT token", ex.getMessage());
        var buffer = exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes());
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    
    /**
     * Handles legacy WebClient authorization exceptions (for backward compatibility)
     */
    @ExceptionHandler(WebClientResponseException.Unauthorized.class)
    public Mono<Void> handleUnauthorizedException(WebClientResponseException.Unauthorized ex, ServerWebExchange exchange) {
        logger.warn("WebClient authorization failed: {}", ex.getMessage());
        
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        String errorResponse = createErrorResponse("AUTH_FAILED", "Authentication failed", ex.getMessage());
        var buffer = exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes());
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    
    /**
     * Handles connection refused errors when downstream services are unavailable
     */
    @ExceptionHandler(ConnectException.class)
    public Mono<Void> handleConnectionException(ConnectException ex, ServerWebExchange exchange) {
        logger.warn("Connection to downstream service failed: {}", ex.getMessage());
        
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        String errorResponse = createErrorResponse("SESSION_NOT_FOUND", "Session not found", "Authentication service is temporarily unavailable");
        var buffer = exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes());
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    
    /**
     * Handles general authentication errors
     */
    @ExceptionHandler(SecurityException.class)
    public Mono<Void> handleSecurityException(SecurityException ex, ServerWebExchange exchange) {
        logger.warn("Security exception: {}", ex.getMessage());
        
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        String errorResponse = createErrorResponse("ACCESS_DENIED", "Access denied", ex.getMessage());
        var buffer = exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes());
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    
    /**
     * Creates a standardized JSON error response
     */
    private String createErrorResponse(String code, String message, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return String.format("""
            {
              "timestamp": "%s",
              "error": {
                "code": "%s",
                "message": "%s",
                "details": "%s"
              }
            }
            """, timestamp, code, message, details);
    }
}
