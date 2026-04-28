package com.healthcare.billing.service;

import com.healthcare.billing.dto.*;
import com.healthcare.billing.entity.Invoice;
import com.healthcare.billing.entity.InvoiceItem;
import com.healthcare.billing.enums.InvoiceStatus;
import com.healthcare.billing.exception.*;
import com.healthcare.billing.mapper.BillingMapper;
import com.healthcare.billing.repository.InvoiceItemRepository;
import com.healthcare.billing.repository.InvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    /**
     * Valid status transitions.
     * Key = current status → Value = allowed next states.
     */
    private static final Map<InvoiceStatus, Set<InvoiceStatus>> TRANSITIONS = Map.of(
        InvoiceStatus.DRAFT,     EnumSet.of(InvoiceStatus.ISSUED,  InvoiceStatus.CANCELLED),
        InvoiceStatus.ISSUED,    EnumSet.of(InvoiceStatus.PAID,    InvoiceStatus.OVERDUE,  InvoiceStatus.CANCELLED),
        InvoiceStatus.OVERDUE,   EnumSet.of(InvoiceStatus.PAID,    InvoiceStatus.CANCELLED),
        InvoiceStatus.PAID,      EnumSet.noneOf(InvoiceStatus.class),
        InvoiceStatus.CANCELLED, EnumSet.noneOf(InvoiceStatus.class)
    );

    private final InvoiceRepository     invoiceRepository;
    private final InvoiceItemRepository itemRepository;
    private final BillingMapper         mapper;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              InvoiceItemRepository itemRepository,
                              BillingMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.itemRepository    = itemRepository;
        this.mapper            = mapper;
    }

    // ================================================================ CRUD

    @Override
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        // Guard: one invoice per appointment
        if (request.getAppointmentId() != null
                && invoiceRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new DuplicateInvoiceException(request.getAppointmentId());
        }

        Invoice invoice = mapper.toEntity(request);
        invoice.recalculate();

        // Attach line items (cascade saves them)
        if (request.getItems() != null) {
            for (InvoiceItemRequest itemReq : request.getItems()) {
                InvoiceItem item = mapper.toItemEntity(itemReq);
                invoice.addItem(item);
            }
        }

        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceSummaryResponse> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable).map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(UUID id) {
        return mapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getByAppointmentId(UUID appointmentId) {
        Invoice invoice = invoiceRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new InvoiceNotFoundException(appointmentId));
        return mapper.toResponse(invoice);
    }

    @Override
    public InvoiceResponse updateInvoice(UUID id, InvoiceUpdateRequest request) {
        Invoice invoice = findById(id);
        guardTerminal(invoice, "update");

        mapper.applyUpdate(invoice, request);
        invoice.recalculate();
        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    /**
     * Hard-delete only allowed on DRAFT/CANCELLED invoices.
     * ISSUED or PAID invoices must be cancelled first.
     */
    @Override
    public void deleteInvoice(UUID id) {
        Invoice invoice = findById(id);
        if (invoice.getStatus() == InvoiceStatus.PAID
                || invoice.getStatus() == InvoiceStatus.ISSUED) {
            throw new InvalidInvoiceStatusException(invoice.getStatus(), "delete");
        }
        invoiceRepository.delete(invoice);
    }

    // ================================================================ Filtered listings

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceSummaryResponse> getByPatient(UUID patientId,
                                                      InvoiceStatus status,
                                                      Pageable pageable) {
        Page<Invoice> page = (status != null)
                ? invoiceRepository.findByPatientIdAndStatus(patientId, status, pageable)
                : invoiceRepository.findByPatientId(patientId, pageable);
        return page.map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceSummaryResponse> getByStatus(InvoiceStatus status, Pageable pageable) {
        return invoiceRepository.findByStatus(status, pageable).map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceSummaryResponse> getByDateRange(OffsetDateTime start,
                                                        OffsetDateTime end,
                                                        Pageable pageable) {
        return invoiceRepository.findByIssuedAtBetween(start, end, pageable).map(mapper::toSummary);
    }

    // ================================================================ Status transitions

    @Override
    public InvoiceResponse issueInvoice(UUID id) {
        Invoice invoice = findById(id);
        transition(invoice, InvoiceStatus.ISSUED);
        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse markPaid(UUID id, PaymentRequest request) {
        Invoice invoice = findById(id);
        transition(invoice, InvoiceStatus.PAID);
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setPaidAt(OffsetDateTime.now());
        if (request.getTransactionReference() != null) {
            String note = (invoice.getNotes() != null ? invoice.getNotes() + "\n" : "")
                    + "Txn ref: " + request.getTransactionReference();
            invoice.setNotes(note);
        }
        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse markOverdue(UUID id) {
        Invoice invoice = findById(id);
        transition(invoice, InvoiceStatus.OVERDUE);
        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse cancelInvoice(UUID id, String reason) {
        Invoice invoice = findById(id);
        transition(invoice, InvoiceStatus.CANCELLED);
        if (reason != null && !reason.isBlank()) {
            String existing = invoice.getNotes() != null ? invoice.getNotes() + "\n" : "";
            invoice.setNotes(existing + "Cancellation reason: " + reason);
        }
        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    // ================================================================ Line-item management

    @Override
    public InvoiceResponse addItem(UUID invoiceId, InvoiceItemRequest request) {
        Invoice invoice = findById(invoiceId);
        guardTerminal(invoice, "add item to");

        InvoiceItem item = mapper.toItemEntity(request);
        invoice.addItem(item);
        invoice.recalculate();

        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse updateItem(UUID invoiceId, UUID itemId, InvoiceItemRequest request) {
        Invoice invoice = findById(invoiceId);
        guardTerminal(invoice, "update item on");

        InvoiceItem item = itemRepository.findByIdAndInvoiceId(itemId, invoiceId)
                .orElseThrow(() -> new InvoiceItemNotFoundException(itemId, invoiceId));

        mapper.applyItemUpdate(item, request);
        invoice.recalculate();

        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse removeItem(UUID invoiceId, UUID itemId) {
        Invoice invoice = findById(invoiceId);
        guardTerminal(invoice, "remove item from");

        // Verify the item belongs to this invoice
        itemRepository.findByIdAndInvoiceId(itemId, invoiceId)
                .orElseThrow(() -> new InvoiceItemNotFoundException(itemId, invoiceId));

        invoice.removeItem(itemId);
        invoice.recalculate();

        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    // ================================================================ Analytics

    @Override
    @Transactional(readOnly = true)
    public BigDecimal outstandingBalance(UUID patientId) {
        return invoiceRepository.outstandingBalanceByPatient(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueReportResponse> monthlyRevenue(OffsetDateTime start, OffsetDateTime end) {
        return invoiceRepository.monthlyRevenueSummary(start, end)
                .stream()
                .map(row -> {
                    RevenueReportResponse r = new RevenueReportResponse();
                    r.setYear(((Number) row[0]).intValue());
                    r.setMonth(((Number) row[1]).intValue());
                    r.setInvoiceCount(((Number) row[2]).longValue());
                    r.setGrossRevenue((BigDecimal) row[3]);
                    r.setTotalDiscounts((BigDecimal) row[4]);
                    r.setNetRevenue(((BigDecimal) row[3]).subtract((BigDecimal) row[4]));
                    return r;
                })
                .collect(Collectors.toList());
    }

    /**
     * Batch job: marks ISSUED invoices older than {@code overdueDaysThreshold} days as OVERDUE.
     *
     * @return number of invoices updated
     */
    @Override
    public int markOverdueInvoices(int overdueDaysThreshold) {
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(overdueDaysThreshold);
        List<Invoice> overdue = invoiceRepository.findOverdueInvoices(cutoff);
        overdue.forEach(inv -> inv.setStatus(InvoiceStatus.OVERDUE));
        invoiceRepository.saveAll(overdue);
        return overdue.size();
    }

    // ================================================================ Helpers

    private Invoice findById(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
    }

    private void transition(Invoice invoice, InvoiceStatus target) {
        Set<InvoiceStatus> allowed = TRANSITIONS.getOrDefault(
                invoice.getStatus(), EnumSet.noneOf(InvoiceStatus.class));
        if (!allowed.contains(target)) {
            throw new InvalidInvoiceStatusException(invoice.getStatus(), target);
        }
        invoice.setStatus(target);
    }

    /** Block mutations on terminal or in-progress invoices. */
    private void guardTerminal(Invoice invoice, String operation) {
        if (invoice.getStatus() == InvoiceStatus.PAID
                || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new InvalidInvoiceStatusException(invoice.getStatus(), operation);
        }
    }
}
