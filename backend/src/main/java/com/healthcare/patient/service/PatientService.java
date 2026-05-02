package com.healthcare.patient.service;

import com.healthcare.patient.dto.PatientRequest;
import com.healthcare.patient.dto.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PatientService {

    PatientResponse createPatient(PatientRequest request);

    Page<PatientResponse> getAllPatients(Pageable pageable);

    Page<PatientResponse> searchPatients(String query, Pageable pageable);

    PatientResponse getPatientById(UUID id);

    PatientResponse updatePatient(UUID id, PatientRequest request);

    void deletePatient(UUID id);       // soft delete (sets isActive = false)

    void hardDeletePatient(UUID id);   // permanent removal (admin use)
}