package com.hl.fambud.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPathVariableException.class)
    public Mono<ResponseEntity<String>> handleInvalidPathVariableException(InvalidPathVariableException ex) {
        // Returning a 400 Bad Request status with the exception message
        return Mono.just(ResponseEntity.badRequest().body(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<String>> handleRuntimeException(RuntimeException ex) {
        // Handling generic runtime exceptions, returning a 500 Internal Server Error
        return Mono.just(ResponseEntity.badRequest().body(ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<String>> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        // Collecting all validation error messages into a single string
        String errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        return Mono.just(new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public Mono<ResponseEntity<String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage()));
    }
}
