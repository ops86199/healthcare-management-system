package com.healthcare.billing.entity;

import com.healthcare.billing.enums.InvoiceItemType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Maps to the `invoice_items` table.
 * `subtotal` is computed by the entity itself (quantity × unitPrice)
 * rather than relying on PostgreSQL's GENERATED ALWAYS AS column,
 * ensuring consistency whether the row comes from the DB or is new.
 */
@Entity
@Table(name = "invoice_items",
       indexes = @Index(name = "idx_invoice_items_invoice", columnList = "invoice_id"))
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private InvoiceItemType itemType;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Derived field: quantity × unitPrice.
     * Written to DB on every save; never mutated directly.
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Optional FK to any related record (medicine_id, prescription_id, …).
     * Stored as a plain UUID to avoid cross-module coupling.
     */
    @Column(name = "reference_id")
    private UUID referenceId;

    // ---- Constructors ----

    public InvoiceItem() {}

    // ---- Business logic ----

    /** Recompute subtotal — must be called before every save/update. */
    public void recalculate() {
        if (unitPrice != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // ---- Getters & Setters ----

    public UUID getId() { return id; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public InvoiceItemType getItemType() { return itemType; }
    public void setItemType(InvoiceItemType itemType) { this.itemType = itemType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }

    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }
}
