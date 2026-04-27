package com.healthcare.patient.repository;

import com.healthcare.patient.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    // Active patients only — used for listings
    Page<Patient> findAllByIsActiveTrue(Pageable pageable);

    // Lookup by phone (unique in practice)
    Optional<Patient> findByPhone(String phone);

    // Lookup by email
    Optional<Patient> findByEmail(String email);

    // Full-name search (case-insensitive)
    @Query("""
        SELECT p FROM Patient p
        WHERE p.isActive = true
          AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(p.lastName)  LIKE LOWER(CONCAT('%', :query, '%')))
        """)
    Page<Patient> searchByName(@Param("query") String query, Pageable pageable);

    // Existence check for phone uniqueness validation
    boolean existsByPhoneAndIdNot(String phone, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
}
