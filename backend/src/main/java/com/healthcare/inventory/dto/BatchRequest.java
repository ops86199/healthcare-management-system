package com.healthcare.inventory.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BatchRequest {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @NotBlank(message = "Batch number is required")
    @Size(max = 80, message = "Batch number must not exceed 80 characters")
    private String batchNumber;

    @Size(max = 200, message = "Supplier must not exceed 200 characters")
    private String supplier;

    private LocalDate receivedDate = LocalDate.now();

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.00", message = "Purchase price must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid purchase price format")
    private BigDecimal purchasePrice;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // ---- Getters & Setters ----

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
}
