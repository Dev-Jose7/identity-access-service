package io.identityaccess.infrastructure.config;

import io.identityaccess.domain.exception.DomainException;
import io.identityaccess.domain.exception.ExclusiveRoleAlreadyAssignedException;
import io.identityaccess.domain.exception.InvalidCredentialsException;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.exception.PrimaryAccountAlreadyExistsException;
import io.identityaccess.domain.exception.RateLimitExceededException;
import io.identityaccess.domain.exception.RoleInvalidException;
import io.identityaccess.domain.exception.SessionAlreadyRevokedException;
import io.identityaccess.domain.exception.SessionNotFoundException;
import io.identityaccess.domain.exception.SessionRefreshNotAllowedException;
import io.identityaccess.domain.exception.UserAlreadyExistsException;
import io.identityaccess.domain.exception.UserNotFoundException;
import io.identityaccess.domain.exception.UserNotEnabledException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WebExceptionHandlerConfig {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(UserNotEnabledException.class)
    public ResponseEntity<Map<String, String>> handleUserNotEnabled(UserNotEnabledException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleRateLimit(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler({PrimaryAccountAlreadyExistsException.class, ExclusiveRoleAlreadyAssignedException.class})
    public ResponseEntity<Map<String, String>> handleExclusiveAccountConflict(DomainException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<Map<String, String>> handleOperationNotPermitted(OperationNotPermittedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(RoleInvalidException.class)
    public ResponseEntity<Map<String, String>> handleRoleInvalid(RoleInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler({SessionNotFoundException.class, SessionRefreshNotAllowedException.class})
    public ResponseEntity<Map<String, String>> handleSessionAccess(DomainException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(SessionAlreadyRevokedException.class)
    public ResponseEntity<Map<String, String>> handleSessionAlreadyRevoked(SessionAlreadyRevokedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomainException(DomainException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("code", "IamDependencyUnavailableException", "message", ex.getMessage()));
    }
}
