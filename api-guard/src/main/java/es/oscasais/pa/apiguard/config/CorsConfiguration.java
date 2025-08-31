package es.oscasais.pa.apiguard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for Spring Cloud Gateway
 * 
 * This configuration handles Cross-Origin Resource Sharing (CORS) for the API Gateway.
 * It allows the frontend PWA application to make requests from localhost during development
 * while maintaining security considerations for production.
 * 
 * Key CORS Concepts:
 * - Origin: The protocol, domain, and port where the request originates
 * - Preflight: Browser's OPTIONS request to check if the actual request is allowed
 * - Credentials: Cookies, authorization headers, or TLS client certificates
 * 
 * Why we need custom CORS in Spring Cloud Gateway:
 * - Gateway is reactive (WebFlux-based), not servlet-based
 * - Standard Spring MVC CORS annotations don't work
 * - We need to handle both preflight and actual requests
 * - Cookie-based authentication requires credentials support
 */
@Configuration
public class CorsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CorsConfiguration.class);
    
    private static final String ALLOWED_HEADERS = "x-requested-with, authorization, Content-Type, Authorization, credential, X-XSRF-TOKEN, Access-Control-Allow-Origin, Access-Control-Allow-Credentials";
    private static final String ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS";
    private static final String MAX_AGE = "3600"; // Cache preflight response for 1 hour
    
    @Value("${cors.allowed-origins}")
    private String allowedOriginsConfig;
    
    @Value("${cors.allowed-credentials}")
    private boolean allowCredentials;

    /**
     * Custom CORS WebFilter for Spring Cloud Gateway
     * 
     * Why a custom filter instead of @CrossOrigin or CorsConfigurationSource?
     * - Spring Cloud Gateway is reactive and uses WebFlux
     * - Standard Spring MVC CORS configuration doesn't work with Gateway
     * - We need to handle both preflight OPTIONS requests and actual requests
     * 
     * This approach follows the Single Responsibility Principle by:
     * - Only handling CORS concerns
     * - Delegating non-CORS requests to the next filter in the chain
     * - Configuration is externalized to application.yml
     */
    @Bean
    public WebFilter corsFilter() {
        return new CorsWebFilter();
    }
    
    /**
     * Custom CORS WebFilter implementation with high priority
     * This ensures CORS headers are set before any other filters execute
     */
    private class CorsWebFilter implements WebFilter, Ordered {
        
        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE; // Run before other filters
        }
        
        @Override
        public Mono<Void> filter(ServerWebExchange ctx, WebFilterChain chain) {
            ServerHttpRequest request = ctx.getRequest();
            ServerHttpResponse response = ctx.getResponse();
            HttpHeaders headers = response.getHeaders();
            
            String origin = request.getHeaders().getOrigin();
            String method = request.getMethod().name();
            String path = request.getPath().value();
            
            logger.debug("Processing {} request to {} from origin: {}", method, path, origin);
            
            // Check if this is a CORS request
            if (CorsUtils.isCorsRequest(request)) {
                logger.debug("CORS request detected for path: {}", path);
                
                // Validate origin against configured allowed origins
                if (isValidOrigin(origin)) {
                    headers.add("Access-Control-Allow-Origin", origin);
                    if (allowCredentials) {
                        headers.add("Access-Control-Allow-Credentials", "true");
                    }
                    logger.debug("CORS headers added for allowed origin: {}", origin);
                } else {
                    // Log unauthorized origin attempts for security monitoring
                    logger.warn("CORS: Rejected origin: {} for path: {}", origin, path);
                }
                
                headers.add("Access-Control-Allow-Headers", ALLOWED_HEADERS);
                headers.add("Access-Control-Allow-Methods", ALLOWED_METHODS);
                headers.add("Access-Control-Max-Age", MAX_AGE);
            }
            
            // Handle preflight requests
            if (CorsUtils.isPreFlightRequest(request)) {
                logger.debug("Handling CORS preflight request for path: {}", path);
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty(); // Complete the request without continuing the filter chain
            }
            
            // Continue with the filter chain for actual requests
            return chain.filter(ctx);
        }
    }
    
    /**
     * Validates if the origin is allowed for CORS requests
     * 
     * Uses the configured allowed origins from application.yml.
     * This provides flexibility between development and production environments.
     * 
     * Security considerations:
     * - Development: Allow localhost with various ports
     * - Production: Only allow your actual frontend domain(s)
     * 
     * @param origin The origin header from the request
     * @return true if the origin is allowed, false otherwise
     */
    private boolean isValidOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        
        // Parse configured allowed origins
        List<String> allowedOrigins = Arrays.asList(allowedOriginsConfig.split(","));
        
        // Check exact match first
        if (allowedOrigins.contains(origin)) {
            return true;
        }
        
        // For development flexibility, also allow localhost with any port
        // This covers scenarios where the port might change during development
        return origin.matches("^https?://localhost(:\\d+)?$");
    }
}