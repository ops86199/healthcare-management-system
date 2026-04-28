package com.healthcare.inventory.service;

import com.healthcare.inventory.dto.*;
import com.healthcare.inventory.entity.InventoryTransaction;
import com.healthcare.inventory.entity.Medicine;
import com.healthcare.inventory.entity.MedicineInventory;
import com.healthcare.inventory.enums.TransactionType;
import com.healthcare.inventory.exception.*;
import com.healthcare.inventory.mapper.InventoryMapper;
import com.healthcare.inventory.repository.InventoryTransactionRepository;
import com.healthcare.inventory.repository.MedicineInventoryRepository;
import com.healthcare.inventory.repository.MedicineRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    /**
     * Transaction types that REMOVE stock (signed negative in ledger).
     */
    private static final Set<TransactionType> STOCK_OUT_TYPES = EnumSet.of(
            TransactionType.DISPENSE,
            TransactionType.EXPIRED
    );

    private final MedicineRepository             medicineRepository;
    private final MedicineInventoryRepository    batchRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryMapper                mapper;

    public InventoryServiceImpl(MedicineRepository medicineRepository,
                                MedicineInventoryRepository batchRepository,
                                InventoryTransactionRepository transactionRepository,
                                InventoryMapper mapper) {
        this.medicineRepository   = medicineRepository;
        this.batchRepository      = batchRepository;
        this.transactionRepository = transactionRepository;
        this.mapper                = mapper;
    }

    // ================================================================ Medicine CRUD

    @Override
    public MedicineResponse createMedicine(MedicineRequest request) {
        if (medicineRepository.existsByNameIgnoreCaseAndIsActiveTrue(request.getName())) {
            throw new DuplicateMedicineException(request.getName());
        }
        Medicine m = mapper.toEntity(request);
        return mapper.toResponse(medicineRepository.save(m));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicineSummaryResponse> getAllMedicines(Pageable pageable) {
        return medicineRepository.findByIsActiveTrue(pageable).map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponse getMedicineById(UUID id) {
        return mapper.toResponse(findActiveMedicine(id));
    }

    @Override
    public MedicineResponse updateMedicine(UUID id, MedicineRequest request) {
        Medicine m = findActiveMedicine(id);
        if (medicineRepository.existsByNameIgnoreCaseAndIsActiveTrueAndIdNot(request.getName(), id)) {
            throw new DuplicateMedicineException(request.getName());
        }
        mapper.applyUpdate(m, request);
        return mapper.toResponse(medicineRepository.save(m));
    }

    @Override
    public void deactivateMedicine(UUID id) {
        Medicine m = findActiveMedicine(id);
        m.setActive(false);
        medicineRepository.save(m);
    }

    // ================================================================ Search / Filter

    @Override
    @Transactional(readOnly = true)
    public Page<MedicineSummaryResponse> searchMedicines(String query, Pageable pageable) {
        return medicineRepository.searchActive(query, pageable).map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicineSummaryResponse> getMedicinesByCategory(String category, Pageable pageable) {
        return medicineRepository.findByCategoryIgnoreCase(category, pageable).map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicineSummaryResponse> getMedicinesByPrescriptionRequired(
            boolean required, Pageable pageable) {
        return medicineRepository
                .findByRequiresPrescriptionAndIsActiveTrue(required, pageable)
                .map(mapper::toSummary);
    }

    // ================================================================ Batch management

    @Override
    public BatchResponse addBatch(UUID medicineId, BatchRequest request) {
        Medicine medicine = findActiveMedicine(medicineId);

        if (batchRepository.existsByMedicineIdAndBatchNumber(medicineId, request.getBatchNumber())) {
            throw new DuplicateBatchException(request.getBatchNumber());
        }

        MedicineInventory batch = mapper.toBatchEntity(request, medicine);
        batch = batchRepository.save(batch);

        // Record PURCHASE transaction in ledger
        appendTransaction(batch, TransactionType.PURCHASE,
                +batch.getQuantity(), null, null,
                "Initial batch receipt: " + request.getBatchNumber());

        return mapper.toBatchResponse(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchResponse getBatchById(UUID batchId) {
        return mapper.toBatchResponse(findBatch(batchId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BatchResponse> getBatchesByMedicine(UUID medicineId, Pageable pageable) {
        findActiveMedicine(medicineId); // validate medicine exists
        return batchRepository.findByMedicineId(medicineId, pageable).map(mapper::toBatchResponse);
    }

    @Override
    public BatchResponse updateBatch(UUID batchId, BatchRequest request) {
        MedicineInventory batch = findBatch(batchId);
        // Batch number change guard
        if (!batch.getBatchNumber().equals(request.getBatchNumber())
                && batchRepository.existsByMedicineIdAndBatchNumber(
                        batch.getMedicine().getId(), request.getBatchNumber())) {
            throw new DuplicateBatchException(request.getBatchNumber());
        }
        mapper.applyBatchUpdate(batch, request);
        return mapper.toBatchResponse(batchRepository.save(batch));
    }

    // ================================================================ Stock update

    /**
     * Adjusts the quantity on a specific batch and appends an immutable
     * ledger entry. DISPENSE is blocked on expired batches.
     */
    @Override
    public TransactionResponse updateStock(UUID medicineId, StockUpdateRequest request) {
        findActiveMedicine(medicineId); // validate medicine is active
        MedicineInventory batch = findBatch(request.getBatchId());

        TransactionType type = request.getTransactionType();
        int qty = request.getQuantity();

        // Guard: cannot dispense from an expired batch
        if (type == TransactionType.DISPENSE && batch.isExpired()) {
            throw new ExpiredBatchOperationException(batch.getBatchNumber());
        }

        // Determine signed change
        boolean isOut = STOCK_OUT_TYPES.contains(type);
        int signedChange = isOut ? -qty : +qty;

        // Guard: cannot go negative
        if (isOut && batch.getQuantity() < qty) {
            throw new InsufficientStockException(
                    batch.getMedicine().getName(), qty, batch.getQuantity());
        }

        batch.setQuantity(batch.getQuantity() + signedChange);
        batchRepository.save(batch);

        InventoryTransaction tx = appendTransaction(
                batch, type, signedChange,
                request.getReferenceId(), request.getPerformedBy(), request.getNotes());

        return mapper.toTransactionResponse(tx);
    }

    // ================================================================ Alerts

    @Override
    @Transactional(readOnly = true)
    public List<LowStockAlertResponse> getLowStockAlerts() {
        return medicineRepository.findLowStockMedicines()
                .stream().map(mapper::toLowStockAlert).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpiringMedicineResponse> getExpiringMedicines(int withinDays) {
        LocalDate today  = LocalDate.now();
        LocalDate cutoff = today.plusDays(withinDays);
        return batchRepository.findExpiringSoon(today, cutoff)
                .stream().map(mapper::toExpiringResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpiringMedicineResponse> getExpiredMedicinesWithStock() {
        return batchRepository.findAlreadyExpiredWithStock(LocalDate.now())
                .stream().map(mapper::toExpiringResponse).collect(Collectors.toList());
    }

    // ================================================================ Transaction history

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByMedicine(UUID medicineId, Pageable pageable) {
        findActiveMedicine(medicineId);
        return transactionRepository
                .findByMedicineIdOrderByCreatedAtDesc(medicineId, pageable)
                .map(mapper::toTransactionResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByBatch(UUID batchId, Pageable pageable) {
        findBatch(batchId);
        return transactionRepository
                .findByInventoryIdOrderByCreatedAtDesc(batchId, pageable)
                .map(mapper::toTransactionResponse);
    }

    // ================================================================ Batch job

    /**
     * Scans all expired batches with remaining stock and writes them off.
     * Intended to run nightly via @Scheduled.
     *
     * @return number of batches processed
     */
    @Override
    public int writeOffExpiredStock(UUID performedBy) {
        List<MedicineInventory> expired =
                batchRepository.findAlreadyExpiredWithStock(LocalDate.now());

        for (MedicineInventory batch : expired) {
            int qty = batch.getQuantity();
            appendTransaction(batch, TransactionType.EXPIRED, -qty,
                    null, performedBy, "Automatic expiry write-off");
            batch.setQuantity(0);
            batchRepository.save(batch);
        }
        return expired.size();
    }

    // ================================================================ Private helpers

    private Medicine findActiveMedicine(UUID id) {
        return medicineRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new MedicineNotFoundException(id));
    }

    private MedicineInventory findBatch(UUID batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));
    }

    private InventoryTransaction appendTransaction(MedicineInventory batch,
                                                    TransactionType type,
                                                    int signedChange,
                                                    UUID referenceId,
                                                    UUID performedBy,
                                                    String notes) {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setInventory(batch);
        tx.setMedicineId(batch.getMedicine().getId());
        tx.setTransactionType(type);
        tx.setQuantityChange(signedChange);
        tx.setReferenceId(referenceId);
        tx.setPerformedBy(performedBy);
        tx.setNotes(notes);
        return transactionRepository.save(tx);
    }
}
