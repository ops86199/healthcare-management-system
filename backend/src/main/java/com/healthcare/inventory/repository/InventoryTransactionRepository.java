package com.healthcare.inventory.repository;

import com.healthcare.inventory.entity.InventoryTransaction;
import com.healthcare.inventory.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {

    // ---- by medicine ----
    Page<InventoryTransaction> findByMedicineIdOrderByCreatedAtDesc(
            UUID medicineId, Pageable pageable);

    // ---- by batch ----
    Page<InventoryTransaction> findByInventoryIdOrderByCreatedAtDesc(
            UUID inventoryId, Pageable pageable);

    // ---- by transaction type ----
    Page<InventoryTransaction> findByTransactionTypeOrderByCreatedAtDesc(
            TransactionType type, Pageable pageable);

    // ---- date range ----
    @Query("""
        SELECT t FROM InventoryTransaction t
        WHERE t.medicineId = :medicineId
          AND t.createdAt BETWEEN :start AND :end
        ORDER BY t.createdAt DESC
        """)
    List<InventoryTransaction> findByMedicineAndDateRange(
            @Param("medicineId") UUID medicineId,
            @Param("start")      OffsetDateTime start,
            @Param("end")        OffsetDateTime end);

    // ---- movement summary for a medicine (net in/out per type) ----
    @Query("""
        SELECT t.transactionType, SUM(t.quantityChange)
        FROM InventoryTransaction t
        WHERE t.medicineId = :medicineId
        GROUP BY t.transactionType
        """)
    List<Object[]> movementSummaryByMedicine(@Param("medicineId") UUID medicineId);
}
