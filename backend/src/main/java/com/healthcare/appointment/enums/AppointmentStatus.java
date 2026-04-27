package com.healthcare.appointment.enums;

/**
 * Lifecycle states of an appointment.
 *
 * Valid transitions:
 *   SCHEDULED → CONFIRMED | CANCELLED
 *   CONFIRMED → COMPLETED | CANCELLED | NO_SHOW
 *   COMPLETED → (terminal)
 *   CANCELLED → (terminal)
 *   NO_SHOW   → (terminal)
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
