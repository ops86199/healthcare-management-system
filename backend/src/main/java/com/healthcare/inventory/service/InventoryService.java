package com.healthcare.inventory.service;

import com.healthcare.inventory.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    // ---- Medicine CRUD ----
    MedicineResponse  createMedicine(MedicineRequest request);
    Page<MedicineSummaryResponse> getAllMedicines(Pageable pageable);
    MedicineResponse  getMedicineById(UUID id);
    MedicineResponse  updateMedicine(UUID id, MedicineRequest request);
    void              deactivateMedicine(UUID id);    // soft delete

    // ---- Medicine search / filter ----
    Page<MedicineSummaryResponse> searchMedicines(String query, Pageable pageable);
    Page<MedicineSummaryResponse> getMedicinesByCategory(String category, Pageable pageable);
    Page<MedicineSummaryResponse> getMedicinesByPrescriptionRequired(
            boolean required, Pageable pageable);

    // ---- Batch (inventory) management ----
    BatchResponse addBatch(UUID medicineId, BatchRequest request);
    BatchResponse getBatchById(UUID batchId);
    Page<BatchResponse> getBatchesByMedicine(UUID medicineId, Pageable pageable);
    BatchResponse updateBatch(UUID batchId, BatchRequest request);

    // ---- Stock updates (ledger write + quantity mutation) ----
    TransactionResponse updateStock(UUID medicineId, StockUpdateRequest request);

    // ---- Alerts & dashboard ----
    List<LowStockAlertResponse>    getLowStockAlerts();
    List<ExpiringMedicineResponse> getExpiringMedicines(int withinDays);
    List<ExpiringMedicineResponse> getExpiredMedicinesWithStock();

    // ---- Transaction history ----
    Page<TransactionResponse> getTransactionsByMedicine(UUID medicineId, Pageable pageable);
    Page<TransactionResponse> getTransactionsByBatch(UUID batchId, Pageable pageable);

    // ---- Batch job: write-off expired stock ----
    int writeOffExpiredStock(UUID performedBy);
}
