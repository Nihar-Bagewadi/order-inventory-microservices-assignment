package com.nihar.order_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors coming from @Valid annotated DTOs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {

        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles Order Not Found exception.
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOrderNotFound(OrderNotFoundException ex) {

        log.error("Order not found error: {}", ex.getMessage());

        Map<String, String> error = Map.of("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles business-level errors like invalid quantity, external service failures, etc.
     */
    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<Map<String, String>> handleInvalidOrder(InvalidOrderException ex) {

        log.error("Invalid order operation: {}", ex.getMessage());

        Map<String, String> error = Map.of("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catch-all for unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {

        log.error("Unexpected error occurred", ex); // logs full stack trace

        Map<String, String> error = Map.of(
                "error", "Internal Server Error",
                "message", ex.getMessage()
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
