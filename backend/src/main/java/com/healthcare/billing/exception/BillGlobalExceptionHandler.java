package com.healthcare.billing.exception;

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
public class BillGlobalExceptionHandler {

    // ---- 404 ----
    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceNotFound(InvoiceNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvoiceItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFound(InvoiceItemNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ---- 409 — duplicate ----
    @ExceptionHandler(DuplicateInvoiceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateInvoiceException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ---- 422 — illegal status transition or operation ----
    @ExceptionHandler(InvalidInvoiceStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(InvalidInvoiceStatusException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // ---- 400 — Bean Validation ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse(400, "Validation failed", fieldErrors, OffsetDateTime.now()));
    }

    // ---- 400 — type mismatch (bad UUID in path, etc.) ----
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        return response(HttpStatus.BAD_REQUEST,
                "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'");
    }

    // ---- 500 ----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // ---- helpers ----

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), message, OffsetDateTime.now()));
    }

    public record ErrorResponse(int status, String message, OffsetDateTime timestamp) {}

    public record ValidationErrorResponse(
            int status, String message,
            Map<String, String> errors,
            OffsetDateTime timestamp) {}
}
