package com.healthcare.inventory.exception;

import java.util.UUID;

public class MedicineNotFoundException extends RuntimeException {
    public MedicineNotFoundException(UUID id) {
        super("Medicine not found with id: " + id);
    }
}
