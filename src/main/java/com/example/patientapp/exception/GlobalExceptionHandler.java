package com.example.patientapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Catches RuntimeExceptions thrown anywhere in a service/controller
 * and turns them into a proper JSON error response instead of a 500 page.
 *
 * Without this:  any throw -> HTTP 500 with a raw HTML stack trace
 * With this:     any throw -> HTTP 400 with { "error": "your message" }
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * (automatically serialises the return value to JSON)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}