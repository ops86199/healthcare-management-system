package com.healthcare.inventory.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maps to the `medicine_inventory` table.
 * Each row represents one physical batch of a medicine.
 * Multiple batches can exist per medicine (different expiry dates / suppliers).
 */
@Entity
@Table(
    name = "medicine_inventory",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_medicine_batch",
        columnNames = {"medicine_id", "batch_number"}
    ),
    indexes = {
        @Index(name = "idx_inventory_medicine", columnList = "medicine_id"),
        @Index(name = "idx_inventory_expiry",   columnList = "expiry_date")
    }
)
public class MedicineInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "quantity", nullable = false)
    private int quantity = 0;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "batch_number", nullable = false, length = 80)
    private String batchNumber;

    @Column(name = "supplier", length = 200)
    private String supplier;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate = LocalDate.now();

    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "inventory",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<InventoryTransaction> transactions = new ArrayList<>();

    // ---- Constructors ----

    public MedicineInventory() {}

    // ---- Business helpers ----

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public int getDaysUntilExpiry() {
        if (expiryDate == null) return Integer.MAX_VALUE;
        return (int) (expiryDate.toEpochDay() - LocalDate.now().toEpochDay());
    }

    // ---- Getters & Setters ----

    public UUID getId() { return id; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<InventoryTransaction> getTransactions() { return transactions; }
}
