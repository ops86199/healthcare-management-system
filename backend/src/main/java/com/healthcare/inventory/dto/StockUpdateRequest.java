package com.healthcare.inventory.dto;

import com.healthcare.inventory.enums.TransactionType;
import jakarta.validation.constraints.*;

import java.util.UUID;

public class StockUpdateRequest {

    @NotNull(message = "Batch ID is required")
    private UUID batchId;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Quantity change is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;  // always positive; direction implied by transactionType

    /** Optional reference to a prescription, appointment, or purchase order. */
    private UUID referenceId;

    /** ID of the user performing the transaction. */
    private UUID performedBy;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // ---- Getters & Setters ----

    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }

    public UUID getPerformedBy() { return performedBy; }
    public void setPerformedBy(UUID performedBy) { this.performedBy = performedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
