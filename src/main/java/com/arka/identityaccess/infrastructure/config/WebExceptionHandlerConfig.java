package com.arka.identityaccess.infrastructure.config;

import com.arka.identityaccess.domain.shared.exception.DomainException;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import com.arka.identityaccess.domain.identity.exception.InvalidCredentialsException;
import com.arka.identityaccess.domain.shared.exception.OperationNotPermittedException;
import com.arka.identityaccess.application.exception.RateLimitExceededException;
import com.arka.identityaccess.domain.access.exception.RoleInvalidException;
import com.arka.identityaccess.domain.session.exception.SessionAlreadyRevokedException;
import com.arka.identityaccess.application.exception.SessionNotFoundException;
import com.arka.identityaccess.domain.session.exception.SessionRefreshNotAllowedException;
import com.arka.identityaccess.application.exception.AccountAlreadyExistsException;
import com.arka.identityaccess.application.exception.AccountNotFoundException;
import com.arka.identityaccess.domain.identity.exception.AccountNotEnabledException;
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

    @ExceptionHandler(AccountNotEnabledException.class)
    public ResponseEntity<Map<String, String>> handleUserNotEnabled(AccountNotEnabledException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleRateLimit(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(AccountAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", ex.errorCode(), "message", ex.getMessage()));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(AccountNotFoundException ex) {
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

    @ExceptionHandler(DomainInvariantViolationException.class)
    public ResponseEntity<Map<String, String>> handleDomainInvariantViolation(DomainInvariantViolationException ex) {
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
