package com.healthcare.appointment.dto;

import com.healthcare.appointment.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private AppointmentStatus status;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;   // optional — e.g. cancellation reason

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
