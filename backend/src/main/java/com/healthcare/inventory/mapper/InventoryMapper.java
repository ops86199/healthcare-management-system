package com.healthcare.inventory.mapper;

import com.healthcare.inventory.dto.*;
import com.healthcare.inventory.entity.InventoryTransaction;
import com.healthcare.inventory.entity.Medicine;
import com.healthcare.inventory.entity.MedicineInventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    // ================================================================ Medicine

    public Medicine toEntity(MedicineRequest req) {
        Medicine m = new Medicine();
        applyMedicineRequest(m, req);
        return m;
    }

    public void applyUpdate(Medicine m, MedicineRequest req) {
        applyMedicineRequest(m, req);
    }

    public MedicineResponse toResponse(Medicine m) {
        MedicineResponse r = new MedicineResponse();
        r.setId(m.getId());
        r.setName(m.getName());
        r.setGenericName(m.getGenericName());
        r.setCategory(m.getCategory());
        r.setManufacturer(m.getManufacturer());
        r.setUnit(m.getUnit());
        r.setStrength(m.getStrength());
        r.setPrice(m.getPrice());
        r.setReorderLevel(m.getReorderLevel());
        r.setRequiresPrescription(m.isRequiresPrescription());
        r.setActive(m.isActive());
        r.setTotalStock(m.totalStock());
        r.setBelowReorderLevel(m.isBelowReorderLevel());
        r.setCreatedAt(m.getCreatedAt());
        r.setUpdatedAt(m.getUpdatedAt());
        return r;
    }

    public MedicineSummaryResponse toSummary(Medicine m) {
        MedicineSummaryResponse s = new MedicineSummaryResponse();
        s.setId(m.getId());
        s.setName(m.getName());
        s.setGenericName(m.getGenericName());
        s.setCategory(m.getCategory());
        s.setUnit(m.getUnit());
        s.setStrength(m.getStrength());
        s.setPrice(m.getPrice());
        s.setReorderLevel(m.getReorderLevel());
        s.setTotalStock(m.totalStock());
        s.setBelowReorderLevel(m.isBelowReorderLevel());
        s.setActive(m.isActive());
        return s;
    }

    // ================================================================ Batch

    public MedicineInventory toBatchEntity(BatchRequest req, Medicine medicine) {
        MedicineInventory b = new MedicineInventory();
        b.setMedicine(medicine);
        applyBatchRequest(b, req);
        return b;
    }

    public void applyBatchUpdate(MedicineInventory b, BatchRequest req) {
        applyBatchRequest(b, req);
    }

    public BatchResponse toBatchResponse(MedicineInventory b) {
        BatchResponse r = new BatchResponse();
        r.setId(b.getId());
        r.setMedicineId(b.getMedicine().getId());
        r.setMedicineName(b.getMedicine().getName());
        r.setQuantity(b.getQuantity());
        r.setExpiryDate(b.getExpiryDate());
        r.setDaysUntilExpiry(b.getDaysUntilExpiry());
        r.setExpired(b.isExpired());
        r.setBatchNumber(b.getBatchNumber());
        r.setSupplier(b.getSupplier());
        r.setReceivedDate(b.getReceivedDate());
        r.setPurchasePrice(b.getPurchasePrice());
        r.setNotes(b.getNotes());
        return r;
    }

    // ================================================================ Transaction

    public TransactionResponse toTransactionResponse(InventoryTransaction tx) {
        TransactionResponse r = new TransactionResponse();
        r.setId(tx.getId());
        r.setInventoryId(tx.getInventory().getId());
        r.setMedicineId(tx.getMedicineId());
        r.setMedicineName(tx.getInventory().getMedicine().getName());
        r.setBatchNumber(tx.getInventory().getBatchNumber());
        r.setTransactionType(tx.getTransactionType());
        r.setQuantityChange(tx.getQuantityChange());
        r.setReferenceId(tx.getReferenceId());
        r.setPerformedBy(tx.getPerformedBy());
        r.setNotes(tx.getNotes());
        r.setCreatedAt(tx.getCreatedAt());
        return r;
    }

    // ================================================================ Alerts

    public LowStockAlertResponse toLowStockAlert(Medicine m) {
        LowStockAlertResponse a = new LowStockAlertResponse();
        a.setMedicineId(m.getId());
        a.setMedicineName(m.getName());
        a.setCategory(m.getCategory());
        a.setReorderLevel(m.getReorderLevel());
        int stock = m.totalStock();
        a.setCurrentStock(stock);
        a.setDeficit(Math.max(0, m.getReorderLevel() - stock));
        return a;
    }

    public ExpiringMedicineResponse toExpiringResponse(MedicineInventory b) {
        ExpiringMedicineResponse r = new ExpiringMedicineResponse();
        r.setBatchId(b.getId());
        r.setMedicineId(b.getMedicine().getId());
        r.setMedicineName(b.getMedicine().getName());
        r.setCategory(b.getMedicine().getCategory());
        r.setBatchNumber(b.getBatchNumber());
        r.setQuantity(b.getQuantity());
        r.setExpiryDate(b.getExpiryDate());
        r.setDaysUntilExpiry(b.getDaysUntilExpiry());
        return r;
    }

    // ================================================================ Helpers

    private void applyMedicineRequest(Medicine m, MedicineRequest req) {
        m.setName(req.getName());
        m.setGenericName(req.getGenericName());
        m.setCategory(req.getCategory());
        m.setManufacturer(req.getManufacturer());
        m.setUnit(req.getUnit());
        m.setStrength(req.getStrength());
        m.setPrice(req.getPrice());
        m.setReorderLevel(req.getReorderLevel());
        m.setRequiresPrescription(req.isRequiresPrescription());
    }

    private void applyBatchRequest(MedicineInventory b, BatchRequest req) {
        b.setQuantity(req.getQuantity());
        b.setExpiryDate(req.getExpiryDate());
        b.setBatchNumber(req.getBatchNumber());
        b.setSupplier(req.getSupplier());
        b.setReceivedDate(req.getReceivedDate() != null
                ? req.getReceivedDate() : java.time.LocalDate.now());
        b.setPurchasePrice(req.getPurchasePrice());
        b.setNotes(req.getNotes());
    }
}
