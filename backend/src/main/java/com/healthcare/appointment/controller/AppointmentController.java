package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.*;
import com.healthcare.appointment.enums.AppointmentStatus;
import com.healthcare.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // ================================================================ CORE CRUD

    /**
     * POST /api/v1/appointments
     * Book a new appointment.
     * Returns 201 Created + Location header pointing to the new resource.
     */
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentRequest request) {

        AppointmentResponse created = appointmentService.createAppointment(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * GET /api/v1/appointments
     * Paginated list of all appointments.
     *
     * Optional query params:
     *   ?patientId=   filter by patient
     *   ?doctorId=    filter by doctor
     *   ?status=      filter by status  (SCHEDULED | CONFIRMED | COMPLETED | CANCELLED | NO_SHOW)
     *   ?start=       ISO-8601 range start
     *   ?end=         ISO-8601 range end
     *   ?page=0  &size=20  &sort=appointmentTime,asc
     */
    @GetMapping
    public ResponseEntity<Page<AppointmentSummaryResponse>> getAllAppointments(
            @RequestParam(required = false) UUID              patientId,
            @RequestParam(required = false) UUID              doctorId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
            @RequestParam(defaultValue = "0")                   int  page,
            @RequestParam(defaultValue = "20")                  int  size,
            @RequestParam(defaultValue = "appointmentTime,asc") String[] sort) {

        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));

        Page<AppointmentSummaryResponse> result;

        if (patientId != null) {
            result = appointmentService.getByPatient(patientId, status, pageable);
        } else if (doctorId != null) {
            result = appointmentService.getByDoctor(doctorId, status, pageable);
        } else if (start != null && end != null) {
            result = appointmentService.getByDateRange(start, end, pageable);
        } else if (status != null) {
            result = appointmentService.getByStatus(status, pageable);
        } else {
            result = appointmentService.getAllAppointments(pageable);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/appointments/{id}
     * Full appointment detail including vital signs.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    /**
     * PUT /api/v1/appointments/{id}
     * Update mutable fields (time, type, notes, diagnosis, follow-up).
     * Patient and doctor cannot be changed after creation.
     * Status changes must go through PATCH /status.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentUpdateRequest request) {

        return ResponseEntity.ok(appointmentService.updateAppointment(id, request));
    }

    /**
     * DELETE /api/v1/appointments/{id}
     * Cancels the appointment (status → CANCELLED).
     * Supply optional ?reason= query param.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {

        return ResponseEntity.ok(appointmentService.cancelAppointment(id, reason));
    }

    // ================================================================ STATUS

    /**
     * PATCH /api/v1/appointments/{id}/status
     * Explicit status transition with optional reason note.
     *
     * Body: { "status": "CONFIRMED", "reason": "optional note" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {

        return ResponseEntity.ok(appointmentService.updateStatus(id, request));
    }

    // ================================================================ DASHBOARD

    /**
     * GET /api/v1/appointments/today
     * All appointments scheduled for today (any status).
     */
    @GetMapping("/today")
    public ResponseEntity<List<AppointmentSummaryResponse>> getTodaysAppointments() {
        return ResponseEntity.ok(appointmentService.getTodaysAppointments());
    }

    /**
     * GET /api/v1/appointments/upcoming/doctor/{doctorId}
     * Upcoming SCHEDULED/CONFIRMED appointments for a specific doctor.
     */
    @GetMapping("/upcoming/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentSummaryResponse>> getUpcomingByDoctor(
            @PathVariable UUID doctorId) {

        return ResponseEntity.ok(appointmentService.getUpcomingByDoctor(doctorId));
    }

    // ================================================================ VITAL SIGNS

    /**
     * POST /api/v1/appointments/{id}/vitals
     * Record vital signs for an appointment (once only — returns 409 if already set).
     */
    @PostMapping("/{id}/vitals")
    public ResponseEntity<VitalSignResponse> recordVitalSigns(
            @PathVariable UUID id,
            @Valid @RequestBody VitalSignRequest request) {

        VitalSignResponse recorded = appointmentService.recordVitalSigns(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(recorded);
    }

    /**
     * PUT /api/v1/appointments/{id}/vitals
     * Update existing vital signs.
     */
    @PutMapping("/{id}/vitals")
    public ResponseEntity<VitalSignResponse> updateVitalSigns(
            @PathVariable UUID id,
            @Valid @RequestBody VitalSignRequest request) {

        return ResponseEntity.ok(appointmentService.updateVitalSigns(id, request));
    }

    /**
     * GET /api/v1/appointments/{id}/vitals
     * Retrieve recorded vital signs for an appointment.
     */
    @GetMapping("/{id}/vitals")
    public ResponseEntity<VitalSignResponse> getVitalSigns(@PathVariable UUID id) {
        VitalSignResponse vs = appointmentService.getVitalSigns(id);
        return (vs != null) ? ResponseEntity.ok(vs) : ResponseEntity.noContent().build();
    }

    // ================================================================ helpers

    private Sort resolveSort(String[] sortParams) {
        if (sortParams.length == 2) {
            Sort.Direction dir = Sort.Direction.fromOptionalString(sortParams[1])
                    .orElse(Sort.Direction.ASC);
            return Sort.by(dir, sortParams[0]);
        }
        return Sort.by(Sort.Direction.ASC, "appointmentTime");
    }
}
