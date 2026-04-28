package com.healthcare.billing.mapper;

import com.healthcare.billing.dto.*;
import com.healthcare.billing.entity.Invoice;
import com.healthcare.billing.entity.InvoiceItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BillingMapper {

    // ================================================================ Invoice

    /** InvoiceRequest → new Invoice entity (items NOT yet attached) */
    public Invoice toEntity(InvoiceRequest req) {
        Invoice invoice = new Invoice();
        applyRequest(invoice, req);
        return invoice;
    }

    /** Apply only the mutable fields from an update request (null = keep existing) */
    public void applyUpdate(Invoice invoice, InvoiceUpdateRequest req) {
        if (req.getConsultationFee() != null) invoice.setConsultationFee(req.getConsultationFee());
        if (req.getMedicineTotal()   != null) invoice.setMedicineTotal(req.getMedicineTotal());
        if (req.getLabTotal()        != null) invoice.setLabTotal(req.getLabTotal());
        if (req.getTaxAmount()       != null) invoice.setTaxAmount(req.getTaxAmount());
        if (req.getDiscount()        != null) invoice.setDiscount(req.getDiscount());
        if (req.getInsuranceClaimed()!= null) invoice.setInsuranceClaimed(req.getInsuranceClaimed());
        if (req.getInsuranceAmount() != null) invoice.setInsuranceAmount(req.getInsuranceAmount());
        if (req.getNotes()           != null) invoice.setNotes(req.getNotes());
    }

    /** Invoice entity → full response DTO */
    public InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse r = new InvoiceResponse();
        r.setId(invoice.getId());
        r.setAppointmentId(invoice.getAppointmentId());
        r.setPatientId(invoice.getPatientId());
        r.setConsultationFee(invoice.getConsultationFee());
        r.setMedicineTotal(invoice.getMedicineTotal());
        r.setLabTotal(invoice.getLabTotal());
        r.setTaxAmount(invoice.getTaxAmount());
        r.setDiscount(invoice.getDiscount());
        r.setTotalAmount(invoice.getTotalAmount());
        r.setStatus(invoice.getStatus());
        r.setPaymentMethod(invoice.getPaymentMethod());
        r.setInsuranceClaimed(invoice.isInsuranceClaimed());
        r.setInsuranceAmount(invoice.getInsuranceAmount());
        r.setNotes(invoice.getNotes());
        r.setCreatedBy(invoice.getCreatedBy());
        r.setIssuedAt(invoice.getIssuedAt());
        r.setPaidAt(invoice.getPaidAt());
        r.setItems(toItemResponseList(invoice.getItems()));
        return r;
    }

    /** Invoice entity → compact summary DTO */
    public InvoiceSummaryResponse toSummary(Invoice invoice) {
        InvoiceSummaryResponse s = new InvoiceSummaryResponse();
        s.setId(invoice.getId());
        s.setPatientId(invoice.getPatientId());
        s.setAppointmentId(invoice.getAppointmentId());
        s.setTotalAmount(invoice.getTotalAmount());
        s.setStatus(invoice.getStatus());
        s.setPaymentMethod(invoice.getPaymentMethod());
        s.setIssuedAt(invoice.getIssuedAt());
        s.setPaidAt(invoice.getPaidAt());
        return s;
    }

    // ================================================================ InvoiceItem

    /** InvoiceItemRequest → InvoiceItem entity (invoice back-ref not set here) */
    public InvoiceItem toItemEntity(InvoiceItemRequest req) {
        InvoiceItem item = new InvoiceItem();
        applyItemRequest(item, req);
        item.recalculate();
        return item;
    }

    /** Apply updates to an existing InvoiceItem */
    public void applyItemUpdate(InvoiceItem item, InvoiceItemRequest req) {
        applyItemRequest(item, req);
        item.recalculate();
    }

    /** InvoiceItem entity → response DTO */
    public InvoiceItemResponse toItemResponse(InvoiceItem item) {
        InvoiceItemResponse r = new InvoiceItemResponse();
        r.setId(item.getId());
        r.setItemType(item.getItemType());
        r.setDescription(item.getDescription());
        r.setQuantity(item.getQuantity());
        r.setUnitPrice(item.getUnitPrice());
        r.setSubtotal(item.getSubtotal());
        r.setReferenceId(item.getReferenceId());
        return r;
    }

    public List<InvoiceItemResponse> toItemResponseList(List<InvoiceItem> items) {
        if (items == null) return Collections.emptyList();
        return items.stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    // ================================================================ private helpers

    private void applyRequest(Invoice invoice, InvoiceRequest req) {
        invoice.setAppointmentId(req.getAppointmentId());
        invoice.setPatientId(req.getPatientId());
        invoice.setConsultationFee(req.getConsultationFee());
        invoice.setMedicineTotal(req.getMedicineTotal());
        invoice.setLabTotal(req.getLabTotal());
        invoice.setTaxAmount(req.getTaxAmount());
        invoice.setDiscount(req.getDiscount());
        invoice.setInsuranceClaimed(req.isInsuranceClaimed());
        invoice.setInsuranceAmount(req.getInsuranceAmount());
        invoice.setNotes(req.getNotes());
        invoice.setCreatedBy(req.getCreatedBy());
    }

    private void applyItemRequest(InvoiceItem item, InvoiceItemRequest req) {
        item.setItemType(req.getItemType());
        item.setDescription(req.getDescription());
        item.setQuantity(req.getQuantity());
        item.setUnitPrice(req.getUnitPrice());
        item.setReferenceId(req.getReferenceId());
    }
}
