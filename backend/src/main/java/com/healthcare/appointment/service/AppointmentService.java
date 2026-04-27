package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.*;
import com.healthcare.appointment.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    // ---- CRUD ----
    AppointmentResponse createAppointment(AppointmentRequest request);
    Page<AppointmentSummaryResponse> getAllAppointments(Pageable pageable);
    AppointmentResponse getAppointmentById(UUID id);
    AppointmentResponse updateAppointment(UUID id, AppointmentUpdateRequest request);
    AppointmentResponse cancelAppointment(UUID id, String reason);

    // ---- Status transitions ----
    AppointmentResponse updateStatus(UUID id, StatusUpdateRequest request);

    // ---- Filtered listings ----
    Page<AppointmentSummaryResponse> getByPatient(UUID patientId, AppointmentStatus status, Pageable pageable);
    Page<AppointmentSummaryResponse> getByDoctor(UUID doctorId, AppointmentStatus status, Pageable pageable);
    Page<AppointmentSummaryResponse> getByStatus(AppointmentStatus status, Pageable pageable);
    Page<AppointmentSummaryResponse> getByDateRange(OffsetDateTime start, OffsetDateTime end, Pageable pageable);

    // ---- Dashboard helpers ----
    List<AppointmentSummaryResponse> getTodaysAppointments();
    List<AppointmentSummaryResponse> getUpcomingByDoctor(UUID doctorId);

    // ---- Vital signs sub-resource ----
    VitalSignResponse recordVitalSigns(UUID appointmentId, VitalSignRequest request);
    VitalSignResponse updateVitalSigns(UUID appointmentId, VitalSignRequest request);
    VitalSignResponse getVitalSigns(UUID appointmentId);
}
