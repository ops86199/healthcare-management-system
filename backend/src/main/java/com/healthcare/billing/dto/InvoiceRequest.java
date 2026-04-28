package com.healthcare.billing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InvoiceRequest {

    /** Optional — null for walk-in billing */
    private UUID appointmentId;

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @DecimalMin(value = "0.00", message = "Consultation fee must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid consultation fee format")
    private BigDecimal consultationFee = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Medicine total must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid medicine total format")
    private BigDecimal medicineTotal = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Lab total must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid lab total format")
    private BigDecimal labTotal = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Tax amount must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid tax amount format")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Discount must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid discount format")
    private BigDecimal discount = BigDecimal.ZERO;

    private boolean insuranceClaimed = false;

    @DecimalMin(value = "0.00", message = "Insurance amount must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid insurance amount format")
    private BigDecimal insuranceAmount = BigDecimal.ZERO;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    private UUID createdBy;

    @Valid
    private List<InvoiceItemRequest> items = new ArrayList<>();

    // ---- Getters & Setters ----

    public UUID getAppointmentId() { return appointmentId; }
    public void setAppointmentId(UUID appointmentId) { this.appointmentId = appointmentId; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public BigDecimal getMedicineTotal() { return medicineTotal; }
    public void setMedicineTotal(BigDecimal medicineTotal) { this.medicineTotal = medicineTotal; }

    public BigDecimal getLabTotal() { return labTotal; }
    public void setLabTotal(BigDecimal labTotal) { this.labTotal = labTotal; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public boolean isInsuranceClaimed() { return insuranceClaimed; }
    public void setInsuranceClaimed(boolean insuranceClaimed) { this.insuranceClaimed = insuranceClaimed; }

    public BigDecimal getInsuranceAmount() { return insuranceAmount; }
    public void setInsuranceAmount(BigDecimal insuranceAmount) { this.insuranceAmount = insuranceAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public List<InvoiceItemRequest> getItems() { return items; }
    public void setItems(List<InvoiceItemRequest> items) { this.items = items; }
}
