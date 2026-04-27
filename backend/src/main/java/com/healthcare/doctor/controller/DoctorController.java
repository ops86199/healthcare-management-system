package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.*;
import com.healthcare.doctor.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // ------------------------------------------------------------------ CRUD

    /**
     * POST /api/v1/doctors
     * Register a new doctor. Returns 201 + Location header.
     */
    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(
            @Valid @RequestBody DoctorRequest request) {

        DoctorResponse created = doctorService.createDoctor(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * GET /api/v1/doctors
     * Paginated list of all doctors.
     * Optional filters: ?search=  ?specialization=  ?availableOnly=true
     */
    @GetMapping
    public ResponseEntity<Page<DoctorSummaryResponse>> getAllDoctors(
            @RequestParam(required = false)              String  search,
            @RequestParam(required = false)              String  specialization,
            @RequestParam(defaultValue = "false")        boolean availableOnly,
            @RequestParam(defaultValue = "0")            int     page,
            @RequestParam(defaultValue = "20")           int     size,
            @RequestParam(defaultValue = "lastName,asc") String[] sort) {

        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));

        Page<DoctorSummaryResponse> result;

        if (search != null && !search.isBlank()) {
            result = doctorService.searchDoctors(search.trim(), pageable);
        } else if (specialization != null && !specialization.isBlank()) {
            result = doctorService.getDoctorsBySpecialization(specialization.trim(), pageable);
        } else if (availableOnly) {
            result = doctorService.getAvailableDoctors(pageable);
        } else {
            result = doctorService.getAllDoctors(pageable);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/doctors/{id}
     * Full detail for a single doctor including schedules.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    /**
     * PUT /api/v1/doctors/{id}
     * Full update of doctor record (and optionally replaces schedules).
     */
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable UUID id,
            @Valid @RequestBody DoctorRequest request) {

        return ResponseEntity.ok(doctorService.updateDoctor(id, request));
    }

    /**
     * DELETE /api/v1/doctors/{id}
     * Soft-delete: marks the doctor as unavailable.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable UUID id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ AVAILABILITY

    /**
     * PATCH /api/v1/doctors/{id}/availability
     * Toggle availability without a full PUT.
     *
     * Body: { "isAvailable": true }
     */
    @PatchMapping("/{id}/availability")
    public ResponseEntity<DoctorResponse> updateAvailability(
            @PathVariable UUID id,
            @Valid @RequestBody AvailabilityUpdateRequest request) {

        return ResponseEntity.ok(doctorService.updateAvailability(id, request.getIsAvailable()));
    }

    // ------------------------------------------------------------------ SCHEDULES

    /**
     * GET /api/v1/doctors/{id}/schedules
     * All schedule slots for a doctor.
     */
    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getSchedules(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.getSchedulesByDoctorId(id));
    }

    /**
     * PUT /api/v1/doctors/{id}/schedules
     * Replace all schedule slots for a doctor.
     */
    @PutMapping("/{id}/schedules")
    public ResponseEntity<List<ScheduleResponse>> replaceSchedules(
            @PathVariable UUID id,
            @Valid @RequestBody List<@Valid ScheduleRequest> schedules) {

        return ResponseEntity.ok(doctorService.replaceSchedules(id, schedules));
    }

    // ------------------------------------------------------------------ helpers

    private Sort resolveSort(String[] sortParams) {
        if (sortParams.length == 2) {
            Sort.Direction dir = Sort.Direction.fromOptionalString(sortParams[1])
                    .orElse(Sort.Direction.ASC);
            return Sort.by(dir, sortParams[0]);
        }
        return Sort.by(Sort.Direction.ASC, "lastName");
    }
}
