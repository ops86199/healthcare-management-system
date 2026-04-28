package com.healthcare.billing.service;

import com.healthcare.billing.dto.*;
import com.healthcare.billing.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface InvoiceService {

    // ---- Invoice CRUD ----
    InvoiceResponse  createInvoice(InvoiceRequest request);
    Page<InvoiceSummaryResponse> getAllInvoices(Pageable pageable);
    InvoiceResponse  getInvoiceById(UUID id);
    InvoiceResponse  updateInvoice(UUID id, InvoiceUpdateRequest request);
    void             deleteInvoice(UUID id);

    // ---- Filtered listings ----
    Page<InvoiceSummaryResponse> getByPatient(UUID patientId, InvoiceStatus status, Pageable pageable);
    Page<InvoiceSummaryResponse> getByStatus(InvoiceStatus status, Pageable pageable);
    Page<InvoiceSummaryResponse> getByDateRange(OffsetDateTime start, OffsetDateTime end, Pageable pageable);
    InvoiceResponse  getByAppointmentId(UUID appointmentId);

    // ---- Status / payment transitions ----
    InvoiceResponse  issueInvoice(UUID id);
    InvoiceResponse  markPaid(UUID id, PaymentRequest request);
    InvoiceResponse  markOverdue(UUID id);
    InvoiceResponse  cancelInvoice(UUID id, String reason);

    // ---- Line-item management ----
    InvoiceResponse  addItem(UUID invoiceId, InvoiceItemRequest request);
    InvoiceResponse  updateItem(UUID invoiceId, UUID itemId, InvoiceItemRequest request);
    InvoiceResponse  removeItem(UUID invoiceId, UUID itemId);

    // ---- Analytics ----
    BigDecimal       outstandingBalance(UUID patientId);
    List<RevenueReportResponse> monthlyRevenue(OffsetDateTime start, OffsetDateTime end);
    int              markOverdueInvoices(int overdueDaysThreshold);
}
