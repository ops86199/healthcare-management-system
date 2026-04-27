package com.healthcare.appointment.exception;

import com.healthcare.appointment.enums.AppointmentStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(AppointmentStatus from, AppointmentStatus to) {
        super("Cannot transition appointment status from " + from + " to " + to);
    }
}
