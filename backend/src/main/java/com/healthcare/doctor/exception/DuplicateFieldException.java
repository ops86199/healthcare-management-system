package com.healthcare.doctor.exception;

// ---- 409 ----
public class DuplicateFieldException extends RuntimeException {
    public DuplicateFieldException(String field, String value) {
        super("A doctor with " + field + " '" + value + "' already exists");
    }
}
