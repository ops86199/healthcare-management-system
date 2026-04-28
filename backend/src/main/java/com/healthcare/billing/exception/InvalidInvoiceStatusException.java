package com.healthcare.billing.exception;

import com.healthcare.billing.enums.InvoiceStatus;

public class InvalidInvoiceStatusException extends RuntimeException {
    public InvalidInvoiceStatusException(InvoiceStatus current, String operation) {
        super("Cannot perform '" + operation + "' on an invoice with status: " + current);
    }

    public InvalidInvoiceStatusException(InvoiceStatus from, InvoiceStatus to) {
        super("Cannot transition invoice status from " + from + " to " + to);
    }
}
