package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.VitalSign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VitalSignRepository extends JpaRepository<VitalSign, UUID> {

    Optional<VitalSign> findByAppointmentId(UUID appointmentId);

    boolean existsByAppointmentId(UUID appointmentId);
}
