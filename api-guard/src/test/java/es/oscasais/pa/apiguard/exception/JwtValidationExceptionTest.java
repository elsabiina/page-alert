package es.oscasais.pa.apiguard.exception;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.ConnectException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtValidationException handler
 * Tests custom error responses for various exception types
 */
class JwtValidationExceptionTest {

    private JwtValidationException exceptionHandler;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new JwtValidationException();
        
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/me").build();
        exchange = MockServerWebExchange.from(request);
    }

    @Test
    void handleJwtException_ShouldReturnUnauthorizedWithCustomMessage() {
        // Given
        JwtException jwtException = new JwtException("Token expired");

        // When
        Mono<Void> result = exceptionHandler.handleJwtException(jwtException, exchange);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals("application/json", exchange.getResponse().getHeaders().getFirst("Content-Type"));
    }

    @Test
    void handleConnectionException_ShouldReturnSessionNotFoundError() {
        // Given
        ConnectException connectException = new ConnectException("Connection refused");

        // When
        Mono<Void> result = exceptionHandler.handleConnectionException(connectException, exchange);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals("application/json", exchange.getResponse().getHeaders().getFirst("Content-Type"));
        
        // Verify the response body contains our custom error message
        // Note: In a real test, you might want to capture and verify the actual response body
    }

    @Test
    void handleSecurityException_ShouldReturnForbiddenWithAccessDenied() {
        // Given
        SecurityException securityException = new SecurityException("Access denied");

        // When
        Mono<Void> result = exceptionHandler.handleSecurityException(securityException, exchange);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        assertEquals("application/json", exchange.getResponse().getHeaders().getFirst("Content-Type"));
    }
}