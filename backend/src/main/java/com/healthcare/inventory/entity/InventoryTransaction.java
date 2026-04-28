package com.healthcare.inventory.entity;

import com.healthcare.inventory.enums.TransactionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maps to the `inventory_transactions` table.
 * Append-only ledger — rows are NEVER updated or deleted.
 * Positive {@code quantityChange} = stock in, negative = stock out.
 */
@Entity
@Table(
    name = "inventory_transactions",
    indexes = {
        @Index(name = "idx_inv_tx_medicine", columnList = "medicine_id"),
        @Index(name = "idx_inv_tx_date",     columnList = "created_at")
    }
)
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private MedicineInventory inventory;

    /**
     * Denormalized FK so queries can filter by medicine without joining inventory.
     */
    @Column(name = "medicine_id", nullable = false)
    private UUID medicineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    /**
     * Signed quantity: positive = stock received, negative = stock removed.
     */
    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;

    /**
     * Optional reference to an external entity — appointment_id, prescription_id,
     * purchase-order id, etc.
     */
    @Column(name = "reference_id")
    private UUID referenceId;

    /** FK → users.id — stored as plain UUID to avoid cross-module coupling. */
    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // ---- Constructors ----

    public InventoryTransaction() {}

    // ---- Getters & Setters ----

    public UUID getId() { return id; }

    public MedicineInventory getInventory() { return inventory; }
    public void setInventory(MedicineInventory inventory) { this.inventory = inventory; }

    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public int getQuantityChange() { return quantityChange; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }

    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }

    public UUID getPerformedBy() { return performedBy; }
    public void setPerformedBy(UUID performedBy) { this.performedBy = performedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
