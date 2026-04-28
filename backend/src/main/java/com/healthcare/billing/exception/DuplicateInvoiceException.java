package com.healthcare.billing.exception;

import java.util.UUID;

public class DuplicateInvoiceException extends RuntimeException {
    public DuplicateInvoiceException(UUID appointmentId) {
        super("An invoice already exists for appointment: " + appointmentId);
    }
}
