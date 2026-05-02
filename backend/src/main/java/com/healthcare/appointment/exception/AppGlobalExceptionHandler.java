package com.healthcare.appointment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---- 404 ----
    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AppointmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error(404, ex.getMessage()));
    }

    // ---- 409 — time slot conflict ----
    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(AppointmentConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error(409, ex.getMessage()));
    }

    // ---- 409 — vital sign already exists ----
    @ExceptionHandler(VitalSignAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleVitalSignExists(VitalSignAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error(409, ex.getMessage()));
    }

    // ---- 422 — illegal status transition ----
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleStatusTransition(InvalidStatusTransitionException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(error(422, ex.getMessage()));
    }

    // ---- 400 — Bean Validation failures ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(
                new ValidationErrorResponse(400, "Validation failed", fieldErrors, OffsetDateTime.now()));
    }

    // ---- 400 — type mismatch (bad UUID in path, etc.) ----
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String msg = "Invalid value '" + ex.getValue()
                + "' for parameter '" + ex.getName() + "'";
        return ResponseEntity.badRequest().body(error(400, msg));
    }

    // ---- 500 ----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(500, "An unexpected error occurred"));
    }

    // ---- helpers ----

    private ErrorResponse error(int status, String message) {
        return new ErrorResponse(status, message, OffsetDateTime.now());
    }

    public record ErrorResponse(int status, String message, OffsetDateTime timestamp) {}

    public record ValidationErrorResponse(
            int status,
            String message,
            Map<String, String> errors,
            OffsetDateTime timestamp) {}
}
