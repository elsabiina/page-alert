package es.oscasais.pa.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to handle JWT cookies configuration and management
 */
@Service
public class CookieService {
    
    private static final Logger logger = LoggerFactory.getLogger(CookieService.class);
    
    private final String cookieName;
    private final boolean secure;
    private final boolean httpOnly;
    private final String sameSite;
    private final int maxAge;
    private final String domain;
    
    public CookieService(
            @Value("${jwt.cookie.name:authToken}") String cookieName,
            @Value("${jwt.cookie.secure:false}") boolean secure,
            @Value("${jwt.cookie.http-only:true}") boolean httpOnly,
            @Value("${jwt.cookie.same-site:Strict}") String sameSite,
            @Value("${jwt.cookie.max-age:3600}") int maxAge,
            @Value("${jwt.cookie.domain:}") String domain) {
        
        this.cookieName = cookieName;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.sameSite = sameSite;
        this.maxAge = maxAge;
        this.domain = domain.trim().isEmpty() ? null : domain;
        
        logger.info("CookieService initialized - Cookie: {}, Secure: {}, HttpOnly: {}, SameSite: {}", 
                   cookieName, secure, httpOnly, sameSite);
    }
    
    /**
     * Sets JWT token as an HTTP-only secure cookie
     * @param response HTTP response to set cookie in
     * @param token JWT token to set as cookie value
     */
    public void setJwtCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = new Cookie(cookieName, token);
        
        // Security configurations
        jwtCookie.setHttpOnly(httpOnly);
        jwtCookie.setSecure(secure);
        jwtCookie.setMaxAge(maxAge);
        jwtCookie.setPath("/"); // Available for all paths
        
        // Set domain if specified (for production)
        if (domain != null && !domain.isEmpty()) {
            jwtCookie.setDomain(domain);
        }
        
        // Add SameSite attribute via Set-Cookie header (since Cookie class doesn't support it directly)
        String cookieHeader = String.format("%s=%s; Path=/; Max-Age=%d; SameSite=%s%s%s%s",
                cookieName, token, maxAge, sameSite,
                httpOnly ? "; HttpOnly" : "",
                secure ? "; Secure" : "",
                domain != null ? "; Domain=" + domain : "");
        
        response.addHeader("Set-Cookie", cookieHeader);
        
        logger.debug("JWT cookie set successfully for token");
    }
    
    /**
     * Clears JWT cookie (for logout)
     * @param response HTTP response to clear cookie in
     */
    public void clearJwtCookie(HttpServletResponse response) {
        Cookie expiredCookie = new Cookie(cookieName, "");
        expiredCookie.setMaxAge(0);
        expiredCookie.setPath("/");
        expiredCookie.setHttpOnly(httpOnly);
        expiredCookie.setSecure(secure);
        
        if (domain != null && !domain.isEmpty()) {
            expiredCookie.setDomain(domain);
        }
        
        response.addCookie(expiredCookie);
        logger.debug("JWT cookie cleared");
    }
    
    /**
     * Gets the configured cookie name
     * @return cookie name
     */
    public String getCookieName() {
        return cookieName;
    }
}