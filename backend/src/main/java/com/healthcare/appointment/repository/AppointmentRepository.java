package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // ---- by patient ----
    Page<Appointment> findByPatientId(UUID patientId, Pageable pageable);
    Page<Appointment> findByPatientIdAndStatus(UUID patientId, AppointmentStatus status, Pageable pageable);

    // ---- by doctor ----
    Page<Appointment> findByDoctorId(UUID doctorId, Pageable pageable);
    Page<Appointment> findByDoctorIdAndStatus(UUID doctorId, AppointmentStatus status, Pageable pageable);

    // ---- by status ----
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    // ---- today's appointments ----
    @Query("""
        SELECT a FROM Appointment a
        WHERE CAST(a.appointmentTime AS date) = CAST(CURRENT_TIMESTAMP AS date)
        ORDER BY a.appointmentTime ASC
        """)
    List<Appointment> findTodaysAppointments();

    // ---- doctor's appointments within a time window (overlap detection) ----
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctorId = :doctorId
          AND a.status NOT IN ('CANCELLED', 'NO_SHOW')
          AND a.appointmentTime < :windowEnd
          AND (a.appointmentTime +
               CAST(CONCAT(a.durationMinutes, ' minutes') AS duration)) > :windowStart
        """)
    List<Appointment> findOverlappingForDoctor(
            @Param("doctorId")     UUID doctorId,
            @Param("windowStart")  OffsetDateTime windowStart,
            @Param("windowEnd")    OffsetDateTime windowEnd);

    // ---- patient's appointments within a time window ----
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.patientId = :patientId
          AND a.status NOT IN ('CANCELLED', 'NO_SHOW')
          AND a.appointmentTime < :windowEnd
          AND (a.appointmentTime +
               CAST(CONCAT(a.durationMinutes, ' minutes') AS duration)) > :windowStart
        """)
    List<Appointment> findOverlappingForPatient(
            @Param("patientId")    UUID patientId,
            @Param("windowStart")  OffsetDateTime windowStart,
            @Param("windowEnd")    OffsetDateTime windowEnd);

    // ---- upcoming appointments for a doctor (dashboard widget) ----
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctorId = :doctorId
          AND a.appointmentTime >= :from
          AND a.status IN ('SCHEDULED', 'CONFIRMED')
        ORDER BY a.appointmentTime ASC
        """)
    List<Appointment> findUpcomingByDoctor(
            @Param("doctorId") UUID doctorId,
            @Param("from")     OffsetDateTime from);

    // ---- date-range search ----
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.appointmentTime BETWEEN :start AND :end
        """)
    Page<Appointment> findByDateRange(
            @Param("start") OffsetDateTime start,
            @Param("end")   OffsetDateTime end,
            Pageable pageable);
}
