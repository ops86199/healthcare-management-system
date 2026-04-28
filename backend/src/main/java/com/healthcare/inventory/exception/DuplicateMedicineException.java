package com.healthcare.inventory.exception;

public class DuplicateMedicineException extends RuntimeException {
    public DuplicateMedicineException(String name) {
        super("An active medicine with name '" + name + "' already exists");
    }
}
