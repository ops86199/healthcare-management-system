package com.healthcare.billing.repository;

import com.healthcare.billing.entity.InvoiceItem;
import com.healthcare.billing.enums.InvoiceItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {

    List<InvoiceItem> findByInvoiceId(UUID invoiceId);

    List<InvoiceItem> findByInvoiceIdAndItemType(UUID invoiceId, InvoiceItemType type);

    Optional<InvoiceItem> findByIdAndInvoiceId(UUID itemId, UUID invoiceId);

    /** Sum of subtotals for a given invoice — used for cross-check. */
    @Query("SELECT COALESCE(SUM(ii.subtotal), 0) FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId")
    BigDecimal sumSubtotalByInvoiceId(@Param("invoiceId") UUID invoiceId);

    /** Bulk-delete all items for an invoice (used on invoice deletion). */
    @Modifying
    @Query("DELETE FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId")
    void deleteByInvoiceId(@Param("invoiceId") UUID invoiceId);
}
