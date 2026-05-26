package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    // ---- basic lookups ----

    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    // ---- uniqueness guards (exclude current record on update) ----

    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByLicenseNumberAndIdNot(String licenseNumber, UUID id);
    boolean existsByPhoneAndIdNot(String phone, UUID id);

    // ---- filtered listings ----

    Page<Doctor> findAllByIsAvailableTrue(Pageable pageable);
    Page<Doctor> findAllBySpecializationIgnoreCase(String specialization, Pageable pageable);

    // ---- full-text name / specialization search ----
    @Query("""
        SELECT d FROM Doctor d
        WHERE LOWER(d.firstName)      LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(d.lastName)       LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :q, '%'))
        """)
    Page<Doctor> searchByNameOrSpecialization(@Param("q") String query, Pageable pageable);

    // ---- available doctors filtered by specialization ----
    @Query("""
        SELECT d FROM Doctor d
        WHERE d.isAvailable = true
          AND LOWER(d.specialization) LIKE LOWER(CONCAT('%', :spec, '%'))
        """)
    Page<Doctor> findAvailableBySpecialization(@Param("spec") String specialization, Pageable pageable);
}
