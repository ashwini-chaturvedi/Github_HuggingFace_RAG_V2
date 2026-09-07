package backend.hugitbackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException exception) {
        return error(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String,Object>>handleValidation(MethodArgumentNotValidException exception){
        String msg=exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err->err.getField()+":"+err.getDefaultMessage())
                .orElse("Validation Failed");

        return error(HttpStatus.BAD_REQUEST,msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception exception) {
        // avoid exposing internal messages in production - return generic message
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "status", status.value(),
                "error", message == null ? status.getReasonPhrase() : message,
                "timestamp", Instant.now().toString()
        );
        return new ResponseEntity<>(body, status);
    }
}
