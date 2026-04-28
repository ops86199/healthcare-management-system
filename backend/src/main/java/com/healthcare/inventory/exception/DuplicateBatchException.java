package com.healthcare.inventory.exception;

public class DuplicateBatchException extends RuntimeException {
    public DuplicateBatchException(String batchNumber) {
        super("A batch with number '" + batchNumber + "' already exists for this medicine");
    }
}
