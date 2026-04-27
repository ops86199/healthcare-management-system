package com.healthcare.appointment.exception;

import java.util.UUID;

public class VitalSignAlreadyExistsException extends RuntimeException {
    public VitalSignAlreadyExistsException(UUID appointmentId) {
        super("Vital signs already recorded for appointment: " + appointmentId
              + ". Use PUT to update them.");
    }
}
