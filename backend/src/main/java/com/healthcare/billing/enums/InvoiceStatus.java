package com.healthcare.billing.enums;

/**
 * Lifecycle states of an Invoice.
 *
 * Allowed transitions:
 *   DRAFT   → ISSUED  | CANCELLED
 *   ISSUED  → PAID    | OVERDUE   | CANCELLED
 *   OVERDUE → PAID    | CANCELLED
 *   PAID    → (terminal)
 *   CANCELLED → (terminal)
 */
public enum InvoiceStatus {
    DRAFT,
    ISSUED,
    PAID,
    OVERDUE,
    CANCELLED
}
