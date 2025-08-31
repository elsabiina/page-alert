package es.oscasais.pa.apiguard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Global error handler for Spring Cloud Gateway
 * Catches all unhandled exceptions and provides custom error responses
 */
@Component
@Order(-1)
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        logger.error("Unhandled exception in gateway: {}", ex.getMessage(), ex);
        
        // Check if this is a connection exception (service unavailable)
        if (isConnectionException(ex)) {
            return handleConnectionError(exchange, ex);
        }
        
        // Handle other exceptions with generic error
        return handleGenericError(exchange, ex);
    }
    
    private boolean isConnectionException(Throwable ex) {
        // Check the exception hierarchy for connection-related exceptions
        Throwable current = ex;
        while (current != null) {
            if (current instanceof ConnectException || 
                current.getClass().getSimpleName().contains("ConnectException") ||
                current.getClass().getName().contains("ConnectException") ||
                current.getMessage() != null && current.getMessage().contains("ConnectException")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
    
    private Mono<Void> handleConnectionError(ServerWebExchange exchange, Throwable ex) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        String errorResponse = createErrorResponse("SESSION_NOT_FOUND", "Session not found", "Authentication service is temporarily unavailable");
        var buffer = exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes());
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    
    private Mono<Void> handleGenericError(ServerWebExchange exchange, Throwable ex) {
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        String errorResponse = createErrorResponse("INTERNAL_ERROR", "Internal server error", "An unexpected error occurred");
        var buffer = exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes());
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    
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