
package es.oscasais.pa.scraper.exception;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {

  private static final Logger log = LoggerFactory.getLogger(
      GlobalExceptionHandler.class);

  /**
   * Handles validation exceptions thrown when method arguments fail validation.
   * 
   * <p>
   * This method processes {@link MethodArgumentNotValidException} that occur
   * when Spring MVC encounters invalid method arguments annotated with validation
   * constraints.
   * It collects all field validation errors and returns them in a struc format.
   * </p>
   * 
   * @param ex the validation exception containing binding errors
   * @return a {@link ResponseEntity} with HTTP status 400 (Bad Request)
   *         containing
   *         a map of field names to their corresponding error messages. The map
   *         will
   *         be empty if no field errors are present.
   **/
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(
        error -> errors.put(error.getField(), error.getDefaultMessage()));

    return ResponseEntity.badRequest().body(errors);
  }
}
