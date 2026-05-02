package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.DoctorSchedule;
import com.healthcare.doctor.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {

    List<DoctorSchedule> findByDoctorId(UUID doctorId);

    List<DoctorSchedule> findByDoctorIdAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek);

    List<DoctorSchedule> findByDoctorIdAndIsActiveTrue(UUID doctorId);

    void deleteByDoctorId(UUID doctorId);
}