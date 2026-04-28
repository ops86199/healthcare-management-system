package com.healthcare.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String medicineName, int requested, int available) {
        super("Insufficient stock for '" + medicineName
              + "': requested=" + requested + ", available=" + available);
    }
}
