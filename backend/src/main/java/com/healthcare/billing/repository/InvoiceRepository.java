package com.healthcare.billing.repository;

import com.healthcare.billing.entity.Invoice;
import com.healthcare.billing.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    // ---- lookups ----
    Optional<Invoice> findByAppointmentId(UUID appointmentId);
    Page<Invoice> findByPatientId(UUID patientId, Pageable pageable);
    Page<Invoice> findByPatientIdAndStatus(UUID patientId, InvoiceStatus status, Pageable pageable);
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    // ---- date range ----
    @Query("SELECT i FROM Invoice i WHERE i.issuedAt BETWEEN :start AND :end")
    Page<Invoice> findByIssuedAtBetween(
            @Param("start") OffsetDateTime start,
            @Param("end")   OffsetDateTime end,
            Pageable pageable);

    // ---- overdue detection: ISSUED invoices older than a given timestamp ----
    @Query("""
        SELECT i FROM Invoice i
        WHERE i.status = 'ISSUED'
          AND i.issuedAt < :cutoff
        """)
    List<Invoice> findOverdueInvoices(@Param("cutoff") OffsetDateTime cutoff);

    // ---- monthly revenue aggregation ----
    @Query("""
        SELECT
            EXTRACT(YEAR  FROM i.issuedAt) AS yr,
            EXTRACT(MONTH FROM i.issuedAt) AS mo,
            COUNT(i)                        AS cnt,
            SUM(i.totalAmount)              AS gross,
            SUM(i.discount)                 AS discounts
        FROM Invoice i
        WHERE i.issuedAt BETWEEN :start AND :end
        GROUP BY EXTRACT(YEAR FROM i.issuedAt), EXTRACT(MONTH FROM i.issuedAt)
        ORDER BY yr DESC, mo DESC
        """)
    List<Object[]> monthlyRevenueSummary(
            @Param("start") OffsetDateTime start,
            @Param("end")   OffsetDateTime end);

    // ---- total outstanding balance for a patient ----
    @Query("""
        SELECT COALESCE(SUM(i.totalAmount), 0)
        FROM Invoice i
        WHERE i.patientId = :patientId
          AND i.status IN ('ISSUED', 'OVERDUE')
        """)
    BigDecimal outstandingBalanceByPatient(@Param("patientId") UUID patientId);

    // ---- duplicate appointment invoice guard ----
    boolean existsByAppointmentId(UUID appointmentId);
}
