package com.healthcare.patient.controler;

import com.healthcare.patient.dto.PatientRequest;
import com.healthcare.patient.dto.PatientResponse;
import com.healthcare.patient.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * POST /api/v1/patients
     * Register a new patient.
     */
    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody PatientRequest request) {

        PatientResponse created = patientService.createPatient(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * GET /api/v1/patients?page=0&size=20&sort=lastName,asc
     * List all active patients (paginated).
     * Optional ?search= performs a name search.
     */
    @GetMapping
    public ResponseEntity<Page<PatientResponse>> getAllPatients(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName,asc") String[] sort) {

        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));

        Page<PatientResponse> result = (search != null && !search.isBlank())
                ? patientService.searchPatients(search.trim(), pageable)
                : patientService.getAllPatients(pageable);

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/patients/{id}
     * Fetch a single patient by UUID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable UUID id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    /**
     * PUT /api/v1/patients/{id}
     * Full update of a patient record.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody PatientRequest request) {

        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    /**
     * DELETE /api/v1/patients/{id}
     * Soft-delete (sets isActive = false). Recoverable.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/v1/patients/{id}/permanent
     * Permanently removes the record. Restricted to admin roles.
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> hardDeletePatient(@PathVariable UUID id) {
        patientService.hardDeletePatient(id);
        return ResponseEntity.noContent().build();
    }

    // ---- helpers ----

    private Sort resolveSort(String[] sortParams) {
        if (sortParams.length == 2) {
            Sort.Direction dir = Sort.Direction.fromOptionalString(sortParams[1])
                    .orElse(Sort.Direction.ASC);
            return Sort.by(dir, sortParams[0]);
        }
        return Sort.by(Sort.Direction.ASC, "lastName");
    }
}
