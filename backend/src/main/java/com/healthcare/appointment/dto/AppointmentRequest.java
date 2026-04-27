package com.healthcare.appointment.dto;

import com.healthcare.appointment.enums.AppointmentType;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AppointmentRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    /** Optional: ties booking to a specific weekly schedule slot */
    private UUID scheduleId;

    @NotNull(message = "Appointment time is required")
    @Future(message = "Appointment time must be in the future")
    private OffsetDateTime appointmentTime;

    @Min(value = 5,   message = "Duration must be at least 5 minutes")
    @Max(value = 480, message = "Duration must not exceed 480 minutes (8 hours)")
    private int durationMinutes = 30;

    @NotNull(message = "Appointment type is required")
    private AppointmentType type;

    @Size(max = 1000, message = "Chief complaint must not exceed 1000 characters")
    private String chiefComplaint;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    /** Optional: pre-assigned booking user (receptionist / system) */
    private UUID createdBy;

    // ---- Getters & Setters ----

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public UUID getDoctorId() { return doctorId; }
    public void setDoctorId(UUID doctorId) { this.doctorId = doctorId; }

    public UUID getScheduleId() { return scheduleId; }
    public void setScheduleId(UUID scheduleId) { this.scheduleId = scheduleId; }

    public OffsetDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(OffsetDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public AppointmentType getType() { return type; }
    public void setType(AppointmentType type) { this.type = type; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}
