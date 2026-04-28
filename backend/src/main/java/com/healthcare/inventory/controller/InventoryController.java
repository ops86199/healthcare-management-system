package com.healthcare.inventory.controller;

import com.healthcare.inventory.dto.*;
import com.healthcare.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medicines")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // ================================================================ MEDICINE CRUD

    /**
     * POST /api/v1/medicines
     * Add a new medicine to the catalogue.
     */
    @PostMapping
    public ResponseEntity<MedicineResponse> createMedicine(
            @Valid @RequestBody MedicineRequest request) {

        MedicineResponse created = inventoryService.createMedicine(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * GET /api/v1/medicines
     * Paginated catalogue listing.
     *
     * Optional filters:
     *   ?search=         name / genericName / category full-text
     *   ?category=       exact category
     *   ?prescription=   true | false
     *   ?page=0 &size=20 &sort=name,asc
     */
    @GetMapping
    public ResponseEntity<Page<MedicineSummaryResponse>> getAllMedicines(
            @RequestParam(required = false) String  search,
            @RequestParam(required = false) String  category,
            @RequestParam(required = false) Boolean prescription,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {

        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        Page<MedicineSummaryResponse> result;

        if (search != null && !search.isBlank()) {
            result = inventoryService.searchMedicines(search.trim(), pageable);
        } else if (category != null && !category.isBlank()) {
            result = inventoryService.getMedicinesByCategory(category.trim(), pageable);
        } else if (prescription != null) {
            result = inventoryService.getMedicinesByPrescriptionRequired(prescription, pageable);
        } else {
            result = inventoryService.getAllMedicines(pageable);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/medicines/{id}
     * Full detail for one medicine including aggregated stock.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponse> getMedicineById(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.getMedicineById(id));
    }

    /**
     * PUT /api/v1/medicines/{id}
     * Full update of catalogue fields.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MedicineResponse> updateMedicine(
            @PathVariable UUID id,
            @Valid @RequestBody MedicineRequest request) {

        return ResponseEntity.ok(inventoryService.updateMedicine(id, request));
    }

    /**
     * DELETE /api/v1/medicines/{id}
     * Soft-deactivate (isActive = false). Does not delete stock records.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateMedicine(@PathVariable UUID id) {
        inventoryService.deactivateMedicine(id);
        return ResponseEntity.noContent().build();
    }

    // ================================================================ BATCH MANAGEMENT

    /**
     * POST /api/v1/medicines/{id}/batches
     * Receive a new stock batch for a medicine.
     * Automatically records a PURCHASE transaction in the ledger.
     */
    @PostMapping("/{id}/batches")
    public ResponseEntity<BatchResponse> addBatch(
            @PathVariable UUID id,
            @Valid @RequestBody BatchRequest request) {

        BatchResponse batch = inventoryService.addBatch(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{batchId}").buildAndExpand(batch.getId()).toUri();
        return ResponseEntity.created(location).body(batch);
    }

    /**
     * GET /api/v1/medicines/{id}/batches
     * All batches for a given medicine (paginated).
     */
    @GetMapping("/{id}/batches")
    public ResponseEntity<Page<BatchResponse>> getBatchesByMedicine(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "expiryDate"));
        return ResponseEntity.ok(inventoryService.getBatchesByMedicine(id, pageable));
    }

    /**
     * GET /api/v1/medicines/batches/{batchId}
     * Single batch detail.
     */
    @GetMapping("/batches/{batchId}")
    public ResponseEntity<BatchResponse> getBatchById(@PathVariable UUID batchId) {
        return ResponseEntity.ok(inventoryService.getBatchById(batchId));
    }

    /**
     * PUT /api/v1/medicines/batches/{batchId}
     * Update batch metadata (quantity changes must go through /stock endpoint).
     */
    @PutMapping("/batches/{batchId}")
    public ResponseEntity<BatchResponse> updateBatch(
            @PathVariable UUID batchId,
            @Valid @RequestBody BatchRequest request) {

        return ResponseEntity.ok(inventoryService.updateBatch(batchId, request));
    }

    // ================================================================ STOCK UPDATE

    /**
     * PATCH /api/v1/medicines/{id}/stock
     * Perform a signed stock movement on a specific batch.
     * Creates an immutable ledger entry in inventory_transactions.
     *
     * Supported transaction types:
     *   PURCHASE  → stock in
     *   DISPENSE  → stock out (blocked on expired batches)
     *   ADJUSTMENT → stock in or out
     *   EXPIRED   → stock out (manual expiry write-off)
     *   RETURNED  → stock in
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<TransactionResponse> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockUpdateRequest request) {

        return ResponseEntity.ok(inventoryService.updateStock(id, request));
    }

    // ================================================================ TRANSACTION HISTORY

    /**
     * GET /api/v1/medicines/{id}/transactions
     * Paginated ledger history for a medicine (all batches).
     */
    @GetMapping("/{id}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByMedicine(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(inventoryService.getTransactionsByMedicine(id, pageable));
    }

    /**
     * GET /api/v1/medicines/batches/{batchId}/transactions
     * Paginated ledger history for a specific batch.
     */
    @GetMapping("/batches/{batchId}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByBatch(
            @PathVariable UUID batchId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(inventoryService.getTransactionsByBatch(batchId, pageable));
    }

    // ================================================================ ALERTS

    /**
     * GET /api/v1/medicines/alerts/low-stock
     * Returns medicines at or below their reorder level.
     */
    @GetMapping("/alerts/low-stock")
    public ResponseEntity<List<LowStockAlertResponse>> getLowStockAlerts() {
        return ResponseEntity.ok(inventoryService.getLowStockAlerts());
    }

    /**
     * GET /api/v1/medicines/alerts/expiring?days=90
     * Batches with remaining stock expiring within {days} days.
     * Default window: 90 days.
     */
    @GetMapping("/alerts/expiring")
    public ResponseEntity<List<ExpiringMedicineResponse>> getExpiringMedicines(
            @RequestParam(defaultValue = "90") int days) {

        return ResponseEntity.ok(inventoryService.getExpiringMedicines(days));
    }

    /**
     * GET /api/v1/medicines/alerts/expired
     * Batches that have already expired but still have stock > 0 (need write-off).
     */
    @GetMapping("/alerts/expired")
    public ResponseEntity<List<ExpiringMedicineResponse>> getExpiredWithStock() {
        return ResponseEntity.ok(inventoryService.getExpiredMedicinesWithStock());
    }

    // ================================================================ BATCH JOB

    /**
     * POST /api/v1/medicines/jobs/write-off-expired?performedBy={userId}
     * Admin / scheduler endpoint: writes off all expired stock in one pass.
     * Returns count of batches processed.
     */
    @PostMapping("/jobs/write-off-expired")
    public ResponseEntity<String> writeOffExpiredStock(
            @RequestParam(required = false) UUID performedBy) {

        int count = inventoryService.writeOffExpiredStock(performedBy);
        return ResponseEntity.ok(count + " expired batch(es) written off");
    }

    // ================================================================ helpers

    private Sort resolveSort(String[] sortParams) {
        if (sortParams.length == 2) {
            Sort.Direction dir = Sort.Direction.fromOptionalString(sortParams[1])
                    .orElse(Sort.Direction.ASC);
            return Sort.by(dir, sortParams[0]);
        }
        return Sort.by(Sort.Direction.ASC, "name");
    }
}
