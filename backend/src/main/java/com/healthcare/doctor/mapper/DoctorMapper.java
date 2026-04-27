package com.healthcare.doctor.mapper;

import com.healthcare.doctor.dto.*;
import com.healthcare.doctor.entity.Doctor;
import com.healthcare.doctor.entity.DoctorSchedule;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DoctorMapper {

    // ---- Request → Entity ----

    public Doctor toEntity(DoctorRequest req) {
        Doctor doctor = new Doctor();
        applyRequest(doctor, req);
        return doctor;
    }

    public void updateEntity(Doctor doctor, DoctorRequest req) {
        applyRequest(doctor, req);
    }

    // ---- Entity → Full Response ----

    public DoctorResponse toResponse(Doctor doctor) {
        DoctorResponse res = new DoctorResponse();
        res.setId(doctor.getId());
        res.setUserId(doctor.getUserId());
        res.setFirstName(doctor.getFirstName());
        res.setLastName(doctor.getLastName());
        res.setFullName(doctor.getFirstName() + " " + doctor.getLastName());
        res.setSpecialization(doctor.getSpecialization());
        res.setLicenseNumber(doctor.getLicenseNumber());
        res.setPhone(doctor.getPhone());
        res.setEmail(doctor.getEmail());
        res.setExperienceYears(doctor.getExperienceYears());
        res.setConsultationFee(doctor.getConsultationFee());
        res.setBio(doctor.getBio());
        res.setProfileImage(doctor.getProfileImage());
        res.setAvailable(doctor.isAvailable());
        res.setCreatedAt(doctor.getCreatedAt());
        res.setUpdatedAt(doctor.getUpdatedAt());
        res.setSchedules(toScheduleResponseList(doctor.getSchedules()));
        return res;
    }

    // ---- Entity → Summary Response ----

    public DoctorSummaryResponse toSummary(Doctor doctor) {
        DoctorSummaryResponse res = new DoctorSummaryResponse();
        res.setId(doctor.getId());
        res.setFullName(doctor.getFirstName() + " " + doctor.getLastName());
        res.setSpecialization(doctor.getSpecialization());
        res.setPhone(doctor.getPhone());
        res.setEmail(doctor.getEmail());
        res.setExperienceYears(doctor.getExperienceYears());
        res.setConsultationFee(doctor.getConsultationFee());
        res.setAvailable(doctor.isAvailable());
        return res;
    }

    // ---- Schedule helpers ----

    public DoctorSchedule toScheduleEntity(ScheduleRequest req, Doctor doctor) {
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(req.getDayOfWeek());
        schedule.setSlotStart(req.getSlotStart());
        schedule.setSlotEnd(req.getSlotEnd());
        schedule.setActive(req.isActive());
        return schedule;
    }

    public ScheduleResponse toScheduleResponse(DoctorSchedule s) {
        ScheduleResponse res = new ScheduleResponse();
        res.setId(s.getId());
        res.setDayOfWeek(s.getDayOfWeek());
        res.setSlotStart(s.getSlotStart());
        res.setSlotEnd(s.getSlotEnd());
        res.setActive(s.isActive());
        return res;
    }

    public List<ScheduleResponse> toScheduleResponseList(List<DoctorSchedule> schedules) {
        if (schedules == null) return Collections.emptyList();
        return schedules.stream().map(this::toScheduleResponse).collect(Collectors.toList());
    }

    // ---- private ----

    private void applyRequest(Doctor doctor, DoctorRequest req) {
        doctor.setUserId(req.getUserId());
        doctor.setFirstName(req.getFirstName());
        doctor.setLastName(req.getLastName());
        doctor.setSpecialization(req.getSpecialization());
        doctor.setLicenseNumber(req.getLicenseNumber());
        doctor.setPhone(req.getPhone());
        doctor.setEmail(req.getEmail());
        doctor.setExperienceYears(req.getExperienceYears());
        doctor.setConsultationFee(req.getConsultationFee());
        doctor.setBio(req.getBio());
        doctor.setProfileImage(req.getProfileImage());
        doctor.setAvailable(req.isAvailable());
    }
}
