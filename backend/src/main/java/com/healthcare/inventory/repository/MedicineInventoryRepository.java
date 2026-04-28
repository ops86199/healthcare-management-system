package com.healthcare.inventory.repository;

import com.healthcare.inventory.entity.MedicineInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicineInventoryRepository extends JpaRepository<MedicineInventory, UUID> {

    // ---- by medicine ----
    List<MedicineInventory> findByMedicineId(UUID medicineId);
    Page<MedicineInventory> findByMedicineId(UUID medicineId, Pageable pageable);
    List<MedicineInventory> findByMedicineIdAndExpiryDateAfter(UUID medicineId, LocalDate after);

    // ---- batch lookup ----
    Optional<MedicineInventory> findByMedicineIdAndBatchNumber(UUID medicineId, String batchNumber);
    boolean existsByMedicineIdAndBatchNumber(UUID medicineId, String batchNumber);

    // ---- expiry queries ----
    @Query("""
        SELECT b FROM MedicineInventory b
        WHERE b.expiryDate BETWEEN :today AND :cutoff
          AND b.quantity > 0
        ORDER BY b.expiryDate ASC
        """)
    List<MedicineInventory> findExpiringSoon(
            @Param("today")  LocalDate today,
            @Param("cutoff") LocalDate cutoff);

    @Query("""
        SELECT b FROM MedicineInventory b
        WHERE b.expiryDate < :today
          AND b.quantity > 0
        ORDER BY b.expiryDate ASC
        """)
    List<MedicineInventory> findAlreadyExpiredWithStock(@Param("today") LocalDate today);

    // ---- aggregate stock per medicine (used by service) ----
    @Query("""
        SELECT COALESCE(SUM(b.quantity), 0)
        FROM MedicineInventory b
        WHERE b.medicine.id = :medicineId
          AND b.expiryDate > :today
        """)
    int totalValidStock(
            @Param("medicineId") UUID medicineId,
            @Param("today")      LocalDate today);

    // ---- FEFO (First-Expiry, First-Out): oldest valid batch with stock > 0 ----
    @Query("""
        SELECT b FROM MedicineInventory b
        WHERE b.medicine.id = :medicineId
          AND b.expiryDate > :today
          AND b.quantity > 0
        ORDER BY b.expiryDate ASC
        """)
    List<MedicineInventory> findValidBatchesFEFO(
            @Param("medicineId") UUID medicineId,
            @Param("today")      LocalDate today);
}
