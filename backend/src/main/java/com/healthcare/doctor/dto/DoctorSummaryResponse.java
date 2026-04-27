package com.healthcare.doctor.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight view returned by GET /doctors (list).
 * Schedules are omitted to keep the payload small.
 */
public class DoctorSummaryResponse {

    private UUID id;
    private String fullName;
    private String specialization;
    private String phone;
    private String email;
    private int experienceYears;
    private BigDecimal consultationFee;
    private boolean isAvailable;

    // ---- Getters & Setters ----

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
