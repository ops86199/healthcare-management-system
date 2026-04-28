package com.healthcare.inventory.exception;

public class ExpiredBatchOperationException extends RuntimeException {
    public ExpiredBatchOperationException(String batchNumber) {
        super("Cannot dispense from expired batch: " + batchNumber);
    }
}
