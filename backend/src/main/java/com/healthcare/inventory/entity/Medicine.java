package com.healthcare.inventory.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maps to the `medicines` table — the master catalogue entry for a drug.
 * Physical stock batches are tracked in {@link MedicineInventory}.
 */
@Entity
@Table(
    name = "medicines",
    indexes = {
        @Index(name = "idx_medicines_name",     columnList = "name"),
        @Index(name = "idx_medicines_category", columnList = "category")
    }
)
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "generic_name", length = 200)
    private String genericName;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    /** Dispensing unit — e.g. "tablet", "ml", "capsule", "vial" */
    @Column(name = "unit", nullable = false, length = 40)
    private String unit;

    /** Strength / concentration — e.g. "500mg", "10mg/ml" */
    @Column(name = "strength", length = 60)
    private String strength;

    /** Retail / dispensing price per unit */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    /** Stock level at which a low-stock alert should be raised */
    @Column(name = "reorder_level", nullable = false)
    private int reorderLevel = 50;

    @Column(name = "requires_prescription", nullable = false)
    private boolean requiresPrescription = true;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "medicine",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<MedicineInventory> inventoryBatches = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // ---- Constructors ----

    public Medicine() {}

    // ---- Business helpers ----

    /** Aggregate current stock across all non-expired batches. */
    public int totalStock() {
        return inventoryBatches.stream()
                .filter(b -> !b.isExpired())
                .mapToInt(MedicineInventory::getQuantity)
                .sum();
    }

    public boolean isBelowReorderLevel() {
        return totalStock() <= reorderLevel;
    }

    // ---- Getters & Setters ----

    public UUID getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public boolean isRequiresPrescription() { return requiresPrescription; }
    public void setRequiresPrescription(boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public List<MedicineInventory> getInventoryBatches() { return inventoryBatches; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
