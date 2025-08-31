package es.oscasais.pa.apiguard.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.ConnectException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalErrorWebExceptionHandler
 * Tests global error handling for connection exceptions and generic errors
 */
class GlobalErrorWebExceptionHandlerTest {

    private GlobalErrorWebExceptionHandler errorHandler;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        errorHandler = new GlobalErrorWebExceptionHandler();
        
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/me").build();
        exchange = MockServerWebExchange.from(request);
    }

    @Test
    void handle_ConnectionException_ShouldReturnSessionNotFoundError() {
        // Given
        ConnectException connectException = new ConnectException("Connection refused: auth-service/172.20.0.5:4005");

        // When
        Mono<Void> result = errorHandler.handle(exchange, connectException);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals("application/json", exchange.getResponse().getHeaders().getFirst("Content-Type"));
    }

    @Test
    void handle_NestedConnectionException_ShouldReturnSessionNotFoundError() {
        // Given
        RuntimeException wrapperException = new RuntimeException("Gateway error", 
            new ConnectException("finishConnect(..) failed: Connection refused"));

        // When
        Mono<Void> result = errorHandler.handle(exchange, wrapperException);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals("application/json", exchange.getResponse().getHeaders().getFirst("Content-Type"));
    }

    @Test
    void handle_GenericException_ShouldReturnInternalServerError() {
        // Given
        RuntimeException genericException = new RuntimeException("Something went wrong");

        // When
        Mono<Void> result = errorHandler.handle(exchange, genericException);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
        assertEquals("application/json", exchange.getResponse().getHeaders().getFirst("Content-Type"));
    }

    @Test
    void handle_ExceptionWithConnectionInClassName_ShouldReturnSessionNotFoundError() {
        // Given - Simulate Netty's AnnotatedConnectException without importing private class
        Exception nettyStyleException = new Exception("io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused");

        // When
        Mono<Void> result = errorHandler.handle(exchange, nettyStyleException);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals("application/json", exchange.getResponse().getHeaders().getFirst("Content-Type"));
    }
}