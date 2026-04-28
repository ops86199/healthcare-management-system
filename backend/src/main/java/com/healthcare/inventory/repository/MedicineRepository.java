package com.healthcare.inventory.repository;

import com.healthcare.inventory.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, UUID> {

    // ---- basic lookups ----
    Page<Medicine> findByIsActiveTrue(Pageable pageable);
    Page<Medicine> findByCategoryIgnoreCase(String category, Pageable pageable);

    // ---- search ----
    @Query("""
        SELECT m FROM Medicine m
        WHERE m.isActive = true
          AND (LOWER(m.name)        LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(m.category)    LIKE LOWER(CONCAT('%', :q, '%')))
        """)
    Page<Medicine> searchActive(@Param("q") String query, Pageable pageable);

    // ---- prescription filter ----
    Page<Medicine> findByRequiresPrescriptionAndIsActiveTrue(
            boolean requiresPrescription, Pageable pageable);

    // ---- uniqueness guards ----
    boolean existsByNameIgnoreCaseAndIsActiveTrue(String name);
    boolean existsByNameIgnoreCaseAndIsActiveTrueAndIdNot(String name, UUID id);

    // ---- low-stock detection: medicines whose total non-expired stock ≤ reorderLevel ----
    @Query("""
        SELECT m FROM Medicine m
        WHERE m.isActive = true
          AND m.reorderLevel >= (
              SELECT COALESCE(SUM(b.quantity), 0)
              FROM MedicineInventory b
              WHERE b.medicine = m
                AND b.expiryDate > CURRENT_DATE
          )
        ORDER BY m.name
        """)
    List<Medicine> findLowStockMedicines();

    Optional<Medicine> findByIdAndIsActiveTrue(UUID id);
}
