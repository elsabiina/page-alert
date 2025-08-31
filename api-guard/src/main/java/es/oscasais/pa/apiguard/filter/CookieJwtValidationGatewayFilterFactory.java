package es.oscasais.pa.apiguard.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import es.oscasais.pa.apiguard.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

/**
 * Gateway filter that validates JWT tokens from HTTP cookies
 * This filter replaces the header-based JWT validation with cookie-based validation
 */
@Component
public class CookieJwtValidationGatewayFilterFactory extends 
        AbstractGatewayFilterFactory<Object> {
    
    private static final Logger logger = LoggerFactory.getLogger(CookieJwtValidationGatewayFilterFactory.class);
    
    private final JwtService jwtService;
    private final String cookieName;
    
    /**
     * Constructor with dependency injection
     * @param jwtService service to handle JWT operations
     * @param cookieName name of the cookie containing the JWT token
     */
    public CookieJwtValidationGatewayFilterFactory(
            JwtService jwtService,
            @Value("${jwt.cookie.name:authToken}") String cookieName) {
        this.jwtService = jwtService;
        this.cookieName = cookieName;
        logger.info("CookieJwtValidationGatewayFilterFactory initialized with cookie name: {}", cookieName);
    }
    
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Skip JWT validation for OPTIONS requests (CORS preflight)
            if (HttpMethod.OPTIONS.equals(request.getMethod())) {
                logger.debug("Skipping JWT validation for OPTIONS request to: {}", request.getURI().getPath());
                return chain.filter(exchange);
            }
            
            // Extract JWT token from cookie
            String jwtToken = extractJwtFromCookie(request);
            
            if (jwtToken == null) {
                logger.warn("No JWT cookie found in request to: {}", request.getURI().getPath());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            try {
                // Validate the JWT token
                Claims claims = jwtService.validateTokenAndGetClaims(jwtToken);
                
                // Extract user information with null safety
                String userId = claims.getSubject();
                String userEmail = claims.get("email", String.class);
                
                // Validate required claims
                if (userId == null || userId.trim().isEmpty()) {
                    logger.warn("JWT token missing or empty subject (user ID)");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
                
                if (userEmail == null || userEmail.trim().isEmpty()) {
                    logger.warn("JWT token missing or empty email claim");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
                
                // Add user information to request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Email", userEmail)
                        .build();
                
                logger.debug("JWT cookie validated successfully for user with email: {} and ID: {}", userEmail, userId);
                
                // Continue with the modified request
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                
            } catch (JwtException e) {
                logger.warn("Invalid JWT cookie: {}", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }
    
    /**
     * Extracts JWT token from the specified cookie
     * @param request the HTTP request
     * @return JWT token string or null if not found
     */
    private String extractJwtFromCookie(ServerHttpRequest request) {
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        
        if (cookies == null || cookies.isEmpty()) {
            logger.warn("No cookies found in request to: {}", request.getURI().getPath());
            return null;
        }
        
        // Log all available cookies for debugging
        logger.debug("Available cookies: {}", cookies.keySet());
        
        HttpCookie jwtCookie = cookies.getFirst(cookieName);
        if (jwtCookie == null) {
            logger.warn("Cookie '{}' not found in request. Available cookies: {}", cookieName, cookies.keySet());
            return null;
        }
        
        String tokenValue = jwtCookie.getValue();
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            logger.warn("Cookie '{}' is empty", cookieName);
            return null;
        }
        
        logger.debug("Found JWT cookie '{}' with value length: {}", cookieName, tokenValue.length());
        return tokenValue.trim();
    }
}
