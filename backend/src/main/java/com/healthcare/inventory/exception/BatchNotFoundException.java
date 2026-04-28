package com.healthcare.inventory.exception;

import java.util.UUID;

public class BatchNotFoundException extends RuntimeException {
    public BatchNotFoundException(UUID id) {
        super("Inventory batch not found with id: " + id);
    }

    public BatchNotFoundException(UUID medicineId, String batchNumber) {
        super("Batch '" + batchNumber + "' not found for medicine: " + medicineId);
    }
}
