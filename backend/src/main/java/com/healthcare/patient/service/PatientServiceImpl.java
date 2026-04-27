package com.healthcare.patient.service;

import com.healthcare.patient.dto.PatientRequest;
import com.healthcare.patient.dto.PatientResponse;
import com.healthcare.patient.entity.Patient;
import com.healthcare.patient.exception.DuplicateFieldException;
import com.healthcare.patient.exception.PatientNotFoundException;
import com.healthcare.patient.mapper.PatientMapper;
import com.healthcare.patient.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientServiceImpl(PatientRepository patientRepository,
                              PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    // ---- Create ----

    @Override
    public PatientResponse createPatient(PatientRequest request) {
        validateUniquePhone(request.getPhone(), null);
        validateUniqueEmail(request.getEmail(), null);

        Patient patient = patientMapper.toEntity(request);
        Patient saved  = patientRepository.save(patient);
        return patientMapper.toResponse(saved);
    }

    // ---- Read ----

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        return patientRepository.findAllByIsActiveTrue(pageable)
                .map(patientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> searchPatients(String query, Pageable pageable) {
        return patientRepository.searchByName(query, pageable)
                .map(patientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(UUID id) {
        return patientMapper.toResponse(findActivePatientById(id));
    }

    // ---- Update ----

    @Override
    public PatientResponse updatePatient(UUID id, PatientRequest request) {
        Patient patient = findActivePatientById(id);

        validateUniquePhone(request.getPhone(), id);
        validateUniqueEmail(request.getEmail(), id);

        patientMapper.updateEntity(patient, request);
        Patient updated = patientRepository.save(patient);
        return patientMapper.toResponse(updated);
    }

    // ---- Delete (soft) ----

    @Override
    public void deletePatient(UUID id) {
        Patient patient = findActivePatientById(id);
        patient.setActive(false);
        patientRepository.save(patient);
    }

    // ---- Hard delete (admin) ----

    @Override
    public void hardDeletePatient(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        patientRepository.deleteById(id);
    }

    // ---- Private helpers ----

    private Patient findActivePatientById(UUID id) {
        return patientRepository.findById(id)
                .filter(Patient::isActive)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    private void validateUniquePhone(String phone, UUID excludeId) {
        if (!StringUtils.hasText(phone)) return;
        boolean duplicate = excludeId == null
                ? patientRepository.findByPhone(phone).isPresent()
                : patientRepository.existsByPhoneAndIdNot(phone, excludeId);
        if (duplicate) throw new DuplicateFieldException("phone", phone);
    }

    private void validateUniqueEmail(String email, UUID excludeId) {
        if (!StringUtils.hasText(email)) return;
        boolean duplicate = excludeId == null
                ? patientRepository.findByEmail(email).isPresent()
                : patientRepository.existsByEmailAndIdNot(email, excludeId);
        if (duplicate) throw new DuplicateFieldException("email", email);
    }
}
