package com.healthcare.billing.exception;

import java.util.UUID;

public class InvoiceItemNotFoundException extends RuntimeException {
    public InvoiceItemNotFoundException(UUID itemId, UUID invoiceId) {
        super("Invoice item " + itemId + " not found on invoice " + invoiceId);
    }
}
