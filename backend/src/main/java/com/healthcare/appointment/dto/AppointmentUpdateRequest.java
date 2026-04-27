package com.healthcare.appointment.dto;

import com.healthcare.appointment.enums.AppointmentType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Used for PUT /appointments/{id}.
 * Does NOT allow patientId / doctorId changes after creation.
 * Status changes go through dedicated PATCH endpoints.
 */
public class AppointmentUpdateRequest {

    @Future(message = "Appointment time must be in the future")
    private OffsetDateTime appointmentTime;

    @Min(value = 5,   message = "Duration must be at least 5 minutes")
    @Max(value = 480, message = "Duration must not exceed 480 minutes")
    private Integer durationMinutes;

    private AppointmentType type;

    @Size(max = 1000, message = "Chief complaint must not exceed 1000 characters")
    private String chiefComplaint;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    @Size(max = 5000, message = "Diagnosis must not exceed 5000 characters")
    private String diagnosis;

    private LocalDate followUpDate;

    // ---- Getters & Setters ----

    public OffsetDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(OffsetDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public AppointmentType getType() { return type; }
    public void setType(AppointmentType type) { this.type = type; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }
}
