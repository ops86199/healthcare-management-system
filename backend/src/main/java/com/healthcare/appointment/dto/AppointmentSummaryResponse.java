package com.healthcare.appointment.dto;

import com.healthcare.appointment.enums.AppointmentStatus;
import com.healthcare.appointment.enums.AppointmentType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Compact projection used for paginated list endpoints.
 * Omits notes, diagnosis, and vital signs to reduce payload size.
 */
public class AppointmentSummaryResponse {

    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private OffsetDateTime appointmentTime;
    private int durationMinutes;
    private AppointmentStatus status;
    private AppointmentType type;
    private String chiefComplaint;

    // ---- Getters & Setters ----

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public UUID getDoctorId() { return doctorId; }
    public void setDoctorId(UUID doctorId) { this.doctorId = doctorId; }

    public OffsetDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(OffsetDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public AppointmentType getType() { return type; }
    public void setType(AppointmentType type) { this.type = type; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
}
