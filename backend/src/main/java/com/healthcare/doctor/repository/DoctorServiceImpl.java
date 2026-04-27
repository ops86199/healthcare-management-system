package com.healthcare.doctor.service;

import com.healthcare.doctor.dto.*;
import com.healthcare.doctor.entity.Doctor;
import com.healthcare.doctor.entity.DoctorSchedule;
import com.healthcare.doctor.exception.DoctorNotFoundException;
import com.healthcare.doctor.exception.DuplicateFieldException;
import com.healthcare.doctor.exception.InvalidScheduleException;
import com.healthcare.doctor.mapper.DoctorMapper;
import com.healthcare.doctor.repository.DoctorRepository;
import com.healthcare.doctor.repository.DoctorScheduleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository         doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorMapper             doctorMapper;

    public DoctorServiceImpl(DoctorRepository doctorRepository,
                             DoctorScheduleRepository scheduleRepository,
                             DoctorMapper doctorMapper) {
        this.doctorRepository   = doctorRepository;
        this.scheduleRepository = scheduleRepository;
        this.doctorMapper       = doctorMapper;
    }

    // ------------------------------------------------------------------ CREATE

    @Override
    public DoctorResponse createDoctor(DoctorRequest request) {
        validateUniqueEmail(request.getEmail(), null);
        validateUniqueLicense(request.getLicenseNumber(), null);
        validateUniquePhone(request.getPhone(), null);

        Doctor doctor = doctorMapper.toEntity(request);

        if (request.getSchedules() != null) {
            validateSchedules(request.getSchedules());
            List<DoctorSchedule> schedules = request.getSchedules().stream()
                    .map(sr -> doctorMapper.toScheduleEntity(sr, doctor))
                    .collect(Collectors.toList());
            doctor.getSchedules().addAll(schedules);
        }

        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    // ------------------------------------------------------------------ READ

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(doctorMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID id) {
        return doctorMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> getAvailableDoctors(Pageable pageable) {
        return doctorRepository.findAllByIsAvailableTrue(pageable).map(doctorMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> getDoctorsBySpecialization(String specialization, Pageable pageable) {
        return doctorRepository
                .findAllBySpecializationIgnoreCase(specialization, pageable)
                .map(doctorMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> searchDoctors(String query, Pageable pageable) {
        return doctorRepository
                .searchByNameOrSpecialization(query, pageable)
                .map(doctorMapper::toSummary);
    }

    // ------------------------------------------------------------------ UPDATE

    @Override
    public DoctorResponse updateDoctor(UUID id, DoctorRequest request) {
        Doctor doctor = findById(id);

        validateUniqueEmail(request.getEmail(), id);
        validateUniqueLicense(request.getLicenseNumber(), id);
        validateUniquePhone(request.getPhone(), id);

        doctorMapper.updateEntity(doctor, request);

        // Replace schedules if provided
        if (request.getSchedules() != null) {
            validateSchedules(request.getSchedules());
            doctor.getSchedules().clear();
            request.getSchedules().stream()
                    .map(sr -> doctorMapper.toScheduleEntity(sr, doctor))
                    .forEach(doctor.getSchedules()::add);
        }

        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    @Override
    public DoctorResponse updateAvailability(UUID id, boolean available) {
        Doctor doctor = findById(id);
        doctor.setAvailable(available);
        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    // ------------------------------------------------------------------ DELETE (soft)

    @Override
    public void deleteDoctor(UUID id) {
        Doctor doctor = findById(id);
        doctor.setAvailable(false);  // mark unavailable; full deletion handled separately if needed
        doctorRepository.save(doctor);
        // For a hard delete instead: doctorRepository.delete(doctor);
    }

    // ------------------------------------------------------------------ SCHEDULES

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByDoctorId(UUID doctorId) {
        findById(doctorId); // ensure doctor exists
        return scheduleRepository.findByDoctorId(doctorId).stream()
                .map(doctorMapper::toScheduleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> replaceSchedules(UUID doctorId, List<ScheduleRequest> schedules) {
        Doctor doctor = findById(doctorId);
        validateSchedules(schedules);

        scheduleRepository.deleteByDoctorId(doctorId);
        scheduleRepository.flush();

        List<DoctorSchedule> saved = schedules.stream()
                .map(sr -> scheduleRepository.save(doctorMapper.toScheduleEntity(sr, doctor)))
                .collect(Collectors.toList());

        return saved.stream().map(doctorMapper::toScheduleResponse).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ Helpers

    private Doctor findById(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }

    private void validateUniqueEmail(String email, UUID excludeId) {
        if (!StringUtils.hasText(email)) return;
        boolean duplicate = excludeId == null
                ? doctorRepository.findByEmail(email).isPresent()
                : doctorRepository.existsByEmailAndIdNot(email, excludeId);
        if (duplicate) throw new DuplicateFieldException("email", email);
    }

    private void validateUniqueLicense(String license, UUID excludeId) {
        if (!StringUtils.hasText(license)) return;
        boolean duplicate = excludeId == null
                ? doctorRepository.findByLicenseNumber(license).isPresent()
                : doctorRepository.existsByLicenseNumberAndIdNot(license, excludeId);
        if (duplicate) throw new DuplicateFieldException("license number", license);
    }

    private void validateUniquePhone(String phone, UUID excludeId) {
        if (!StringUtils.hasText(phone)) return;
        boolean duplicate = excludeId != null
                && doctorRepository.existsByPhoneAndIdNot(phone, excludeId);
        if (duplicate) throw new DuplicateFieldException("phone", phone);
    }

    private void validateSchedules(List<ScheduleRequest> schedules) {
        if (schedules == null || schedules.isEmpty()) return;
        for (ScheduleRequest sr : schedules) {
            if (sr.getSlotStart() != null && sr.getSlotEnd() != null
                    && !sr.getSlotStart().isBefore(sr.getSlotEnd())) {
                throw new InvalidScheduleException(
                        "Slot start time must be before slot end time for day: " + sr.getDayOfWeek());
            }
        }
    }
}
