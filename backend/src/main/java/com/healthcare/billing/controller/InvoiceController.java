package com.healthcare.billing.controller;

import com.healthcare.billing.dto.*;
import com.healthcare.billing.enums.InvoiceStatus;
import com.healthcare.billing.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // ================================================================ CORE CRUD

    /**
     * POST /api/v1/invoices
     * Create a new invoice, optionally with line items.
     * Returns 201 Created + Location header.
     */
    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(
            @Valid @RequestBody InvoiceRequest request) {

        InvoiceResponse created = invoiceService.createInvoice(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * GET /api/v1/invoices
     * Paginated list of all invoices.
     *
     * Optional filters:
     *   ?patientId=   filter by patient
     *   ?status=      filter by status (DRAFT | ISSUED | PAID | OVERDUE | CANCELLED)
     *   ?start= &end= ISO-8601 date-range on issuedAt
     *   ?page=0 &size=20 &sort=issuedAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<InvoiceSummaryResponse>> getAllInvoices(
            @RequestParam(required = false) UUID          patientId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
            @RequestParam(defaultValue = "0")              int  page,
            @RequestParam(defaultValue = "20")             int  size,
            @RequestParam(defaultValue = "issuedAt,desc")  String[] sort) {

        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));

        Page<InvoiceSummaryResponse> result;
        if (patientId != null) {
            result = invoiceService.getByPatient(patientId, status, pageable);
        } else if (start != null && end != null) {
            result = invoiceService.getByDateRange(start, end, pageable);
        } else if (status != null) {
            result = invoiceService.getByStatus(status, pageable);
        } else {
            result = invoiceService.getAllInvoices(pageable);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/invoices/{id}
     * Full invoice detail including all line items.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    /**
     * GET /api/v1/invoices/appointment/{appointmentId}
     * Find the invoice linked to a specific appointment.
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<InvoiceResponse> getByAppointmentId(
            @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(invoiceService.getByAppointmentId(appointmentId));
    }

    /**
     * PUT /api/v1/invoices/{id}
     * Update fee breakdown and notes (null fields = keep current value).
     * Cannot update PAID or CANCELLED invoices.
     */
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceUpdateRequest request) {

        return ResponseEntity.ok(invoiceService.updateInvoice(id, request));
    }

    /**
     * DELETE /api/v1/invoices/{id}
     * Hard-delete; only allowed for DRAFT or CANCELLED invoices.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    // ================================================================ STATUS TRANSITIONS

    /**
     * PATCH /api/v1/invoices/{id}/issue
     * Transition DRAFT → ISSUED (makes invoice visible to patient).
     */
    @PatchMapping("/{id}/issue")
    public ResponseEntity<InvoiceResponse> issueInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.issueInvoice(id));
    }

    /**
     * PATCH /api/v1/invoices/{id}/pay
     * Transition ISSUED/OVERDUE → PAID. Requires payment method.
     *
     * Body: { "paymentMethod": "CARD", "transactionReference": "TXN-12345" }
     */
    @PatchMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponse> markPaid(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentRequest request) {

        return ResponseEntity.ok(invoiceService.markPaid(id, request));
    }

    /**
     * PATCH /api/v1/invoices/{id}/overdue
     * Manually transition ISSUED → OVERDUE (also available via batch job).
     */
    @PatchMapping("/{id}/overdue")
    public ResponseEntity<InvoiceResponse> markOverdue(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.markOverdue(id));
    }

    /**
     * PATCH /api/v1/invoices/{id}/cancel
     * Cancel an invoice (DRAFT / ISSUED / OVERDUE → CANCELLED).
     * Optional ?reason= query param appended to notes.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InvoiceResponse> cancelInvoice(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {

        return ResponseEntity.ok(invoiceService.cancelInvoice(id, reason));
    }

    // ================================================================ LINE ITEMS

    /**
     * POST /api/v1/invoices/{id}/items
     * Add a line item to an existing invoice.
     */
    @PostMapping("/{id}/items")
    public ResponseEntity<InvoiceResponse> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceItemRequest request) {

        return ResponseEntity.ok(invoiceService.addItem(id, request));
    }

    /**
     * PUT /api/v1/invoices/{invoiceId}/items/{itemId}
     * Replace a line item's details.
     */
    @PutMapping("/{invoiceId}/items/{itemId}")
    public ResponseEntity<InvoiceResponse> updateItem(
            @PathVariable UUID invoiceId,
            @PathVariable UUID itemId,
            @Valid @RequestBody InvoiceItemRequest request) {

        return ResponseEntity.ok(invoiceService.updateItem(invoiceId, itemId, request));
    }

    /**
     * DELETE /api/v1/invoices/{invoiceId}/items/{itemId}
     * Remove a single line item and recalculate total.
     */
    @DeleteMapping("/{invoiceId}/items/{itemId}")
    public ResponseEntity<InvoiceResponse> removeItem(
            @PathVariable UUID invoiceId,
            @PathVariable UUID itemId) {

        return ResponseEntity.ok(invoiceService.removeItem(invoiceId, itemId));
    }

    // ================================================================ ANALYTICS

    /**
     * GET /api/v1/invoices/balance/{patientId}
     * Total outstanding (ISSUED + OVERDUE) balance for a patient.
     */
    @GetMapping("/balance/{patientId}")
    public ResponseEntity<BigDecimal> outstandingBalance(@PathVariable UUID patientId) {
        return ResponseEntity.ok(invoiceService.outstandingBalance(patientId));
    }

    /**
     * GET /api/v1/invoices/revenue?start=&end=
     * Monthly revenue summary for a date range.
     */
    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueReportResponse>> monthlyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {

        return ResponseEntity.ok(invoiceService.monthlyRevenue(start, end));
    }

    /**
     * POST /api/v1/invoices/overdue-scan?days=30
     * Admin / scheduler endpoint: marks ISSUED invoices older than {days} as OVERDUE.
     * Returns count of updated invoices.
     */
    @PostMapping("/overdue-scan")
    public ResponseEntity<String> markOverdueInvoices(
            @RequestParam(defaultValue = "30") int days) {

        int count = invoiceService.markOverdueInvoices(days);
        return ResponseEntity.ok(count + " invoice(s) marked as OVERDUE");
    }

    // ================================================================ helpers

    private Sort resolveSort(String[] sortParams) {
        if (sortParams.length == 2) {
            Sort.Direction dir = Sort.Direction.fromOptionalString(sortParams[1])
                    .orElse(Sort.Direction.DESC);
            return Sort.by(dir, sortParams[0]);
        }
        return Sort.by(Sort.Direction.DESC, "issuedAt");
    }
}
