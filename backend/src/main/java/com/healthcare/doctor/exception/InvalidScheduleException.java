package com.healthcare.doctor.exception;

// ---- 422 ----
public class InvalidScheduleException extends RuntimeException {
    public InvalidScheduleException(String message) {
        super(message);
    }
}
