package com.healthcare.patient.exception;

public class DuplicateFieldException extends RuntimeException {

    public DuplicateFieldException(String field, String value) {
        super("A patient with " + field + " '" + value + "' already exists");
    }
}
