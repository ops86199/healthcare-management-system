package com.healthcare.doctor.service;

import com.healthcare.doctor.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DoctorService {

    // ---- CRUD ----
    DoctorResponse   createDoctor(DoctorRequest request);
    Page<DoctorSummaryResponse> getAllDoctors(Pageable pageable);
    DoctorResponse   getDoctorById(UUID id);
    DoctorResponse   updateDoctor(UUID id, DoctorRequest request);
    void             deleteDoctor(UUID id);             // soft delete

    // ---- filtered listings ----
    Page<DoctorSummaryResponse> getAvailableDoctors(Pageable pageable);
    Page<DoctorSummaryResponse> getDoctorsBySpecialization(String specialization, Pageable pageable);
    Page<DoctorSummaryResponse> searchDoctors(String query, Pageable pageable);

    // ---- availability toggle ----
    DoctorResponse updateAvailability(UUID id, boolean available);

    // ---- schedule sub-resource ----
    List<ScheduleResponse> getSchedulesByDoctorId(UUID doctorId);
    List<ScheduleResponse> replaceSchedules(UUID doctorId, List<ScheduleRequest> schedules);
}
