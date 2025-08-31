package es.oscasais.pa.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CookieService
 * Tests cookie creation, security attributes, and cleanup
 */
class CookieServiceTest {

    @Mock
    private HttpServletResponse response;

    private CookieService cookieService;

    private final String TEST_COOKIE_NAME = "testAuthToken";
    private final String TEST_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIn0.test";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize CookieService with test configuration
        cookieService = new CookieService(
            TEST_COOKIE_NAME,    // cookieName
            true,                // secure
            true,                // httpOnly
            "Strict",           // sameSite
            3600,               // maxAge (1 hour)
            "example.com"       // domain
        );
    }

    @Test
    void testSetJwtCookie_ShouldSetSecureCookieWithCorrectAttributes() {
        // When: Set JWT cookie
        cookieService.setJwtCookie(response, TEST_JWT_TOKEN);

        // Then: Should add Set-Cookie header with secure attributes
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(response).addHeader(headerCaptor.capture(), valueCaptor.capture());
        
        String headerName = headerCaptor.getValue();
        String cookieHeader = valueCaptor.getValue();
        
        assertEquals("Set-Cookie", headerName);
        assertTrue(cookieHeader.contains(TEST_COOKIE_NAME + "=" + TEST_JWT_TOKEN));
        assertTrue(cookieHeader.contains("Path=/"));
        assertTrue(cookieHeader.contains("Max-Age=3600"));
        assertTrue(cookieHeader.contains("SameSite=Strict"));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Secure"));
        assertTrue(cookieHeader.contains("Domain=example.com"));
    }

    @Test
    void testSetJwtCookie_WithNoDomain_ShouldNotIncludeDomainAttribute() {
        // Given: CookieService with no domain
        CookieService noDomainService = new CookieService(
            TEST_COOKIE_NAME, true, true, "Strict", 3600, ""
        );

        // When: Set JWT cookie
        noDomainService.setJwtCookie(response, TEST_JWT_TOKEN);

        // Then: Should not include Domain attribute
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), valueCaptor.capture());
        
        String cookieHeader = valueCaptor.getValue();
        assertFalse(cookieHeader.contains("Domain="));
    }

    @Test
    void testSetJwtCookie_WithInsecureSettings_ShouldReflectConfiguration() {
        // Given: CookieService with insecure settings for development
        CookieService devService = new CookieService(
            TEST_COOKIE_NAME, false, false, "Lax", 1800, ""
        );

        // When: Set JWT cookie
        devService.setJwtCookie(response, TEST_JWT_TOKEN);

        // Then: Should reflect insecure configuration
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), valueCaptor.capture());
        
        String cookieHeader = valueCaptor.getValue();
        assertTrue(cookieHeader.contains("SameSite=Lax"));
        assertTrue(cookieHeader.contains("Max-Age=1800"));
        assertFalse(cookieHeader.contains("HttpOnly"));
        assertFalse(cookieHeader.contains("Secure"));
    }

    @Test
    void testClearJwtCookie_ShouldSetExpiredCookie() {
        // When: Clear JWT cookie
        cookieService.clearJwtCookie(response);

        // Then: Should add expired cookie
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        
        Cookie expiredCookie = cookieCaptor.getValue();
        assertEquals(TEST_COOKIE_NAME, expiredCookie.getName());
        assertEquals("", expiredCookie.getValue());
        assertEquals(0, expiredCookie.getMaxAge());
        assertEquals("/", expiredCookie.getPath());
        assertTrue(expiredCookie.isHttpOnly());
        assertTrue(expiredCookie.getSecure());
        assertEquals("example.com", expiredCookie.getDomain());
    }

    @Test
    void testClearJwtCookie_WithNoDomain_ShouldClearCorrectly() {
        // Given: CookieService with no domain
        CookieService noDomainService = new CookieService(
            TEST_COOKIE_NAME, true, true, "Strict", 3600, ""
        );

        // When: Clear JWT cookie
        noDomainService.clearJwtCookie(response);

        // Then: Should clear cookie without domain
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        
        Cookie expiredCookie = cookieCaptor.getValue();
        assertEquals(TEST_COOKIE_NAME, expiredCookie.getName());
        assertNull(expiredCookie.getDomain());
    }

    @Test
    void testGetCookieName_ShouldReturnConfiguredName() {
        // When: Get cookie name
        String cookieName = cookieService.getCookieName();

        // Then: Should return configured name
        assertEquals(TEST_COOKIE_NAME, cookieName);
    }

    @Test
    void testSetJwtCookie_WithNullToken_ShouldHandleGracefully() {
        // When: Set JWT cookie with null token
        cookieService.setJwtCookie(response, null);

        // Then: Should still set cookie header (with null value)
        verify(response).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void testSetJwtCookie_WithEmptyToken_ShouldHandleGracefully() {
        // When: Set JWT cookie with empty token
        cookieService.setJwtCookie(response, "");

        // Then: Should set cookie header with empty value
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), valueCaptor.capture());
        
        String cookieHeader = valueCaptor.getValue();
        assertTrue(cookieHeader.contains(TEST_COOKIE_NAME + "=;"));
    }
}