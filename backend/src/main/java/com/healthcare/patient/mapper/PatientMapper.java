package com.healthcare.patient.mapper;

import com.healthcare.patient.dto.PatientRequest;
import com.healthcare.patient.dto.PatientResponse;
import com.healthcare.patient.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    /** Request DTO → new Patient entity */
    public Patient toEntity(PatientRequest request) {
        Patient patient = new Patient();
        applyRequest(patient, request);
        return patient;
    }

    /** Merge request DTO fields into an existing Patient entity */
    public void updateEntity(Patient patient, PatientRequest request) {
        applyRequest(patient, request);
    }

    /** Patient entity → response DTO */
    public PatientResponse toResponse(Patient patient) {
        PatientResponse response = new PatientResponse();
        response.setId(patient.getId());
        response.setFirstName(patient.getFirstName());
        response.setLastName(patient.getLastName());
        response.setFullName(patient.getFirstName() + " " + patient.getLastName());
        response.setDateOfBirth(patient.getDateOfBirth());
        response.setGender(patient.getGender());
        response.setBloodType(patient.getBloodType());
        response.setAddress(patient.getAddress());
        response.setPhone(patient.getPhone());
        response.setEmail(patient.getEmail());
        response.setEmergencyContact(patient.getEmergencyContact());
        response.setInsuranceProvider(patient.getInsuranceProvider());
        response.setInsuranceNumber(patient.getInsuranceNumber());
        response.setAllergies(patient.getAllergies());
        response.setNotes(patient.getNotes());
        response.setActive(patient.isActive());
        response.setCreatedAt(patient.getCreatedAt());
        response.setUpdatedAt(patient.getUpdatedAt());
        return response;
    }

    // ---- private helpers ----

    private void applyRequest(Patient patient, PatientRequest request) {
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodType(request.getBloodType());
        patient.setAddress(request.getAddress());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setInsuranceProvider(request.getInsuranceProvider());
        patient.setInsuranceNumber(request.getInsuranceNumber());
        patient.setAllergies(request.getAllergies());
        patient.setNotes(request.getNotes());
    }
}
