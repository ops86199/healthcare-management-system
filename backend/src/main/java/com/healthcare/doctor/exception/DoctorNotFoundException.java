package com.healthcare.doctor.exception;

import java.util.UUID;

// ---- 404 ----
public class DoctorNotFoundException extends RuntimeException {
    public DoctorNotFoundException(UUID id) {
        super("Doctor not found with id: " + id);
    }
}
