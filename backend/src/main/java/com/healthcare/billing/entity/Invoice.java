package com.healthcare.billing.entity;

import com.healthcare.billing.enums.InvoiceStatus;
import com.healthcare.billing.enums.PaymentMethod;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maps to the `invoices` table.
 *
 * `totalAmount` mirrors the PostgreSQL GENERATED ALWAYS AS column but is
 * maintained in Java via {@link #recalculate()} so the object is always
 * consistent without a DB round-trip.
 *
 * patientId / appointmentId are plain UUID FK columns — no cross-module
 * @ManyToOne — keeping this module independently deployable.
 */
@Entity
@Table(
    name = "invoices",
    indexes = {
        @Index(name = "idx_invoices_patient", columnList = "patient_id"),
        @Index(name = "idx_invoices_status",  columnList = "status"),
        @Index(name = "idx_invoices_issued",  columnList = "issued_at")
    }
)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** FK → appointments.id (1-to-1; optional for walk-in billing) */
    @Column(name = "appointment_id", unique = true)
    private UUID appointmentId;

    /** FK → patients.id */
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    // ---- Fee breakdown ----

    @Column(name = "consultation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee = BigDecimal.ZERO;

    @Column(name = "medicine_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal medicineTotal = BigDecimal.ZERO;

    @Column(name = "lab_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal labTotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    /**
     * Computed: consultationFee + medicineTotal + labTotal + taxAmount - discount.
     * Kept in sync by {@link #recalculate()}.
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // ---- Status & payment ----

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "insurance_claimed", nullable = false)
    private boolean insuranceClaimed = false;

    @Column(name = "insurance_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal insuranceAmount = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** FK → users.id (the billing clerk who raised this invoice) */
    @Column(name = "created_by")
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    // ---- Line items ----

    @OneToMany(mappedBy = "invoice",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<InvoiceItem> items = new ArrayList<>();

    // ---- Constructors ----

    public Invoice() {}

    // ---- Business logic ----

    /**
     * Recomputes totalAmount from the five fee components.
     * Call this after any fee field or discount changes before saving.
     */
    public void recalculate() {
        BigDecimal gross = safeAdd(consultationFee, medicineTotal, labTotal, taxAmount);
        this.totalAmount = gross.subtract(safeVal(discount)).max(BigDecimal.ZERO);
    }

    /** Convenience: add a line item and set its back-reference. */
    public void addItem(InvoiceItem item) {
        item.setInvoice(this);
        item.recalculate();
        items.add(item);
    }

    /** Remove a line item by its id. */
    public void removeItem(UUID itemId) {
        items.removeIf(i -> i.getId().equals(itemId));
    }

    // ---- Helpers ----

    private BigDecimal safeVal(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal safeAdd(BigDecimal... values) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : values) sum = sum.add(safeVal(v));
        return sum;
    }

    // ---- Getters & Setters ----

    public UUID getId() { return id; }

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

    public BigDecimal getTotalAmount() { return totalAmount; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isInsuranceClaimed() { return insuranceClaimed; }
    public void setInsuranceClaimed(boolean insuranceClaimed) { this.insuranceClaimed = insuranceClaimed; }

    public BigDecimal getInsuranceAmount() { return insuranceAmount; }
    public void setInsuranceAmount(BigDecimal insuranceAmount) { this.insuranceAmount = insuranceAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public OffsetDateTime getIssuedAt() { return issuedAt; }

    public OffsetDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(OffsetDateTime paidAt) { this.paidAt = paidAt; }

    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
}
