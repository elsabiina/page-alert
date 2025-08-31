package es.oscasais.pa.auth.exception;

/**
 * Exception thrown when authentication operations fail.
 * This exception is used for various authentication-related errors
 * such as database connectivity issues, invalid credentials, or system failures.
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}