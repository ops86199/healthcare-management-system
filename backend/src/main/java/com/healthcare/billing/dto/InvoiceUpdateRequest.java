package com.healthcare.billing.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Fields that can be changed after creation.
 * patientId / appointmentId are immutable.
 * Status / payment changes go through dedicated PATCH endpoints.
 */
public class InvoiceUpdateRequest {

    @DecimalMin(value = "0.00", message = "Consultation fee must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid consultation fee format")
    private BigDecimal consultationFee;

    @DecimalMin(value = "0.00", message = "Medicine total must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid medicine total format")
    private BigDecimal medicineTotal;

    @DecimalMin(value = "0.00", message = "Lab total must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid lab total format")
    private BigDecimal labTotal;

    @DecimalMin(value = "0.00", message = "Tax amount must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid tax amount format")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.00", message = "Discount must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid discount format")
    private BigDecimal discount;

    private Boolean insuranceClaimed;

    @DecimalMin(value = "0.00", message = "Insurance amount must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid insurance amount format")
    private BigDecimal insuranceAmount;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    // ---- Getters & Setters ----

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal v) { this.consultationFee = v; }

    public BigDecimal getMedicineTotal() { return medicineTotal; }
    public void setMedicineTotal(BigDecimal v) { this.medicineTotal = v; }

    public BigDecimal getLabTotal() { return labTotal; }
    public void setLabTotal(BigDecimal v) { this.labTotal = v; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal v) { this.taxAmount = v; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal v) { this.discount = v; }

    public Boolean getInsuranceClaimed() { return insuranceClaimed; }
    public void setInsuranceClaimed(Boolean v) { this.insuranceClaimed = v; }

    public BigDecimal getInsuranceAmount() { return insuranceAmount; }
    public void setInsuranceAmount(BigDecimal v) { this.insuranceAmount = v; }

    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
}
