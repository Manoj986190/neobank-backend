package com.neobank.backend.exception;

import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 409 — duplicate email
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailExists(
            EmailAlreadyExistsException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody(ex.getMessage(), 409));
    }

    // 400 — validation failures (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> body = errorBody("Validation failed", 400);
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    // 500 — catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("An unexpected error occurred", 500));
    }

    // 401 — bad credentials
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody("Invalid email or password", 401));
    }

    // 403 — inactive user
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(
            DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorBody("Account is inactive", 403));
    }

    // 404 — resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody(ex.getMessage(), 404));
    }

    private Map<String, Object> errorBody(String message, int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("message", message);
        return body;
    }
}