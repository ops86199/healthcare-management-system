package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.*;
import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.VitalSign;
import com.healthcare.appointment.enums.AppointmentStatus;
import com.healthcare.appointment.exception.*;
import com.healthcare.appointment.mapper.AppointmentMapper;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.VitalSignRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    /**
     * Allowed status transitions.
     * Key   = current status
     * Value = set of statuses the current one may transition TO
     */
    private static final Map<AppointmentStatus, Set<AppointmentStatus>> ALLOWED_TRANSITIONS = Map.of(
        AppointmentStatus.SCHEDULED, EnumSet.of(
            AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED),
        AppointmentStatus.CONFIRMED, EnumSet.of(
            AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW),
        AppointmentStatus.COMPLETED, EnumSet.noneOf(AppointmentStatus.class),
        AppointmentStatus.CANCELLED, EnumSet.noneOf(AppointmentStatus.class),
        AppointmentStatus.NO_SHOW,   EnumSet.noneOf(AppointmentStatus.class)
    );

    private final AppointmentRepository appointmentRepository;
    private final VitalSignRepository   vitalSignRepository;
    private final AppointmentMapper     mapper;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  VitalSignRepository vitalSignRepository,
                                  AppointmentMapper mapper) {
        this.appointmentRepository = appointmentRepository;
        this.vitalSignRepository   = vitalSignRepository;
        this.mapper                = mapper;
    }

    // ================================================================ CREATE

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        OffsetDateTime start = request.getAppointmentTime();
        OffsetDateTime end   = start.plusMinutes(request.getDurationMinutes());

        // Conflict: doctor double-booking
        checkDoctorConflict(request.getDoctorId(), start, end, null);

        // Conflict: patient double-booking
        checkPatientConflict(request.getPatientId(), start, end, null);

        Appointment saved = appointmentRepository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    // ================================================================ READ

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentSummaryResponse> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(UUID id) {
        Appointment appt = findById(id);
        VitalSign   vs   = vitalSignRepository.findByAppointmentId(id).orElse(null);
        return mapper.toResponse(appt, vs);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentSummaryResponse> getByPatient(UUID patientId,
                                                          AppointmentStatus status,
                                                          Pageable pageable) {
        Page<Appointment> page = (status != null)
                ? appointmentRepository.findByPatientIdAndStatus(patientId, status, pageable)
                : appointmentRepository.findByPatientId(patientId, pageable);
        return page.map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentSummaryResponse> getByDoctor(UUID doctorId,
                                                         AppointmentStatus status,
                                                         Pageable pageable) {
        Page<Appointment> page = (status != null)
                ? appointmentRepository.findByDoctorIdAndStatus(doctorId, status, pageable)
                : appointmentRepository.findByDoctorId(doctorId, pageable);
        return page.map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentSummaryResponse> getByStatus(AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findByStatus(status, pageable).map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentSummaryResponse> getByDateRange(OffsetDateTime start,
                                                            OffsetDateTime end,
                                                            Pageable pageable) {
        return appointmentRepository.findByDateRange(start, end, pageable).map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSummaryResponse> getTodaysAppointments() {
        return appointmentRepository.findTodaysAppointments()
                .stream().map(mapper::toSummary).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSummaryResponse> getUpcomingByDoctor(UUID doctorId) {
        return appointmentRepository.findUpcomingByDoctor(doctorId, OffsetDateTime.now())
                .stream().map(mapper::toSummary).collect(Collectors.toList());
    }

    // ================================================================ UPDATE

    @Override
    public AppointmentResponse updateAppointment(UUID id, AppointmentUpdateRequest request) {
        Appointment appt = findById(id);
        guardTerminal(appt);

        // Re-check conflict only if time is changing
        if (request.getAppointmentTime() != null) {
            OffsetDateTime start = request.getAppointmentTime();
            int duration = (request.getDurationMinutes() != null)
                    ? request.getDurationMinutes()
                    : appt.getDurationMinutes();
            OffsetDateTime end = start.plusMinutes(duration);

            checkDoctorConflict(appt.getDoctorId(),  start, end, id);
            checkPatientConflict(appt.getPatientId(), start, end, id);
        }

        mapper.applyUpdate(appt, request);
        return mapper.toResponse(appointmentRepository.save(appt));
    }

    // ================================================================ CANCEL

    @Override
    public AppointmentResponse cancelAppointment(UUID id, String reason) {
        Appointment appt = findById(id);
        validateTransition(appt.getStatus(), AppointmentStatus.CANCELLED);
        appt.setStatus(AppointmentStatus.CANCELLED);
        if (reason != null && !reason.isBlank()) {
            String existing = appt.getNotes() != null ? appt.getNotes() + "\n" : "";
            appt.setNotes(existing + "Cancellation reason: " + reason);
        }
        return mapper.toResponse(appointmentRepository.save(appt));
    }

    // ================================================================ STATUS

    @Override
    public AppointmentResponse updateStatus(UUID id, StatusUpdateRequest request) {
        Appointment appt = findById(id);
        validateTransition(appt.getStatus(), request.getStatus());
        appt.setStatus(request.getStatus());

        if (request.getReason() != null && !request.getReason().isBlank()) {
            String existing = appt.getNotes() != null ? appt.getNotes() + "\n" : "";
            appt.setNotes(existing + "Status note: " + request.getReason());
        }

        return mapper.toResponse(appointmentRepository.save(appt));
    }

    // ================================================================ VITAL SIGNS

    @Override
    public VitalSignResponse recordVitalSigns(UUID appointmentId, VitalSignRequest request) {
        Appointment appt = findById(appointmentId);

        if (vitalSignRepository.existsByAppointmentId(appointmentId)) {
            throw new VitalSignAlreadyExistsException(appointmentId);
        }

        VitalSign vs = mapper.toVitalSignEntity(request, appt);
        return mapper.toVitalSignResponse(vitalSignRepository.save(vs));
    }

    @Override
    public VitalSignResponse updateVitalSigns(UUID appointmentId, VitalSignRequest request) {
        findById(appointmentId); // ensure appointment exists
        VitalSign vs = vitalSignRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
        mapper.applyVitalSign(vs, request);
        return mapper.toVitalSignResponse(vitalSignRepository.save(vs));
    }

    @Override
    @Transactional(readOnly = true)
    public VitalSignResponse getVitalSigns(UUID appointmentId) {
        findById(appointmentId);
        return vitalSignRepository.findByAppointmentId(appointmentId)
                .map(mapper::toVitalSignResponse)
                .orElse(null);
    }

    // ================================================================ Helpers

    private Appointment findById(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    private void validateTransition(AppointmentStatus from, AppointmentStatus to) {
        Set<AppointmentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(
                from, EnumSet.noneOf(AppointmentStatus.class));
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }

    private void guardTerminal(Appointment appt) {
        if (appt.getStatus() == AppointmentStatus.CANCELLED
                || appt.getStatus() == AppointmentStatus.COMPLETED
                || appt.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new InvalidStatusTransitionException(appt.getStatus(), appt.getStatus());
        }
    }

    private void checkDoctorConflict(UUID doctorId,
                                     OffsetDateTime start,
                                     OffsetDateTime end,
                                     UUID excludeId) {
        List<Appointment> overlaps = appointmentRepository
                .findOverlappingForDoctor(doctorId, start, end);
        boolean conflict = overlaps.stream()
                .anyMatch(a -> excludeId == null || !a.getId().equals(excludeId));
        if (conflict) {
            throw new AppointmentConflictException(
                    "Doctor already has an appointment during this time slot");
        }
    }

    private void checkPatientConflict(UUID patientId,
                                      OffsetDateTime start,
                                      OffsetDateTime end,
                                      UUID excludeId) {
        List<Appointment> overlaps = appointmentRepository
                .findOverlappingForPatient(patientId, start, end);
        boolean conflict = overlaps.stream()
                .anyMatch(a -> excludeId == null || !a.getId().equals(excludeId));
        if (conflict) {
            throw new AppointmentConflictException(
                    "Patient already has an appointment during this time slot");
        }
    }
}
