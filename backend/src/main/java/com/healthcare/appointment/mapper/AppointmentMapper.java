package com.healthcare.appointment.mapper;

import com.healthcare.appointment.dto.*;
import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.VitalSign;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    // ---- Request → Entity ----

    public Appointment toEntity(AppointmentRequest req) {
        Appointment a = new Appointment();
        a.setPatientId(req.getPatientId());
        a.setDoctorId(req.getDoctorId());
        a.setScheduleId(req.getScheduleId());
        a.setAppointmentTime(req.getAppointmentTime());
        a.setDurationMinutes(req.getDurationMinutes());
        a.setType(req.getType());
        a.setChiefComplaint(req.getChiefComplaint());
        a.setNotes(req.getNotes());
        a.setCreatedBy(req.getCreatedBy());
        return a;
    }

    /** Applies only the mutable fields from an update request */
    public void applyUpdate(Appointment a, AppointmentUpdateRequest req) {
        if (req.getAppointmentTime() != null)  a.setAppointmentTime(req.getAppointmentTime());
        if (req.getDurationMinutes() != null)  a.setDurationMinutes(req.getDurationMinutes());
        if (req.getType()            != null)  a.setType(req.getType());
        if (req.getChiefComplaint()  != null)  a.setChiefComplaint(req.getChiefComplaint());
        if (req.getNotes()           != null)  a.setNotes(req.getNotes());
        if (req.getDiagnosis()       != null)  a.setDiagnosis(req.getDiagnosis());
        if (req.getFollowUpDate()    != null)  a.setFollowUpDate(req.getFollowUpDate());
    }

    // ---- Entity → Full Response ----

    public AppointmentResponse toResponse(Appointment a) {
        AppointmentResponse r = new AppointmentResponse();
        r.setId(a.getId());
        r.setPatientId(a.getPatientId());
        r.setDoctorId(a.getDoctorId());
        r.setScheduleId(a.getScheduleId());
        r.setAppointmentTime(a.getAppointmentTime());
        r.setDurationMinutes(a.getDurationMinutes());
        r.setStatus(a.getStatus());
        r.setType(a.getType());
        r.setChiefComplaint(a.getChiefComplaint());
        r.setNotes(a.getNotes());
        r.setDiagnosis(a.getDiagnosis());
        r.setFollowUpDate(a.getFollowUpDate());
        r.setCreatedBy(a.getCreatedBy());
        r.setCreatedAt(a.getCreatedAt());
        r.setUpdatedAt(a.getUpdatedAt());
        return r;
    }

    public AppointmentResponse toResponse(Appointment a, VitalSign vs) {
        AppointmentResponse r = toResponse(a);
        if (vs != null) r.setVitalSign(toVitalSignResponse(vs));
        return r;
    }

    // ---- Entity → Summary ----

    public AppointmentSummaryResponse toSummary(Appointment a) {
        AppointmentSummaryResponse s = new AppointmentSummaryResponse();
        s.setId(a.getId());
        s.setPatientId(a.getPatientId());
        s.setDoctorId(a.getDoctorId());
        s.setAppointmentTime(a.getAppointmentTime());
        s.setDurationMinutes(a.getDurationMinutes());
        s.setStatus(a.getStatus());
        s.setType(a.getType());
        s.setChiefComplaint(a.getChiefComplaint());
        return s;
    }

    // ---- VitalSign helpers ----

    public VitalSign toVitalSignEntity(VitalSignRequest req, Appointment appointment) {
        VitalSign vs = new VitalSign();
        vs.setAppointment(appointment);
        applyVitalSign(vs, req);
        return vs;
    }

    public void applyVitalSign(VitalSign vs, VitalSignRequest req) {
        if (req.getTemperatureC()    != null) vs.setTemperatureC(req.getTemperatureC());
        if (req.getPulseBpm()        != null) vs.setPulseBpm(req.getPulseBpm());
        if (req.getSystolicBp()      != null) vs.setSystolicBp(req.getSystolicBp());
        if (req.getDiastolicBp()     != null) vs.setDiastolicBp(req.getDiastolicBp());
        if (req.getOxygenSaturation()!= null) vs.setOxygenSaturation(req.getOxygenSaturation());
        if (req.getRespiratoryRate() != null) vs.setRespiratoryRate(req.getRespiratoryRate());
        if (req.getWeightKg()        != null) vs.setWeightKg(req.getWeightKg());
        if (req.getHeightCm()        != null) vs.setHeightCm(req.getHeightCm());
    }

    public VitalSignResponse toVitalSignResponse(VitalSign vs) {
        VitalSignResponse r = new VitalSignResponse();
        r.setId(vs.getId());
        r.setTemperatureC(vs.getTemperatureC());
        r.setPulseBpm(vs.getPulseBpm());
        r.setSystolicBp(vs.getSystolicBp());
        r.setDiastolicBp(vs.getDiastolicBp());
        r.setOxygenSaturation(vs.getOxygenSaturation());
        r.setRespiratoryRate(vs.getRespiratoryRate());
        r.setWeightKg(vs.getWeightKg());
        r.setHeightCm(vs.getHeightCm());
        r.setRecordedAt(vs.getRecordedAt());
        return r;
    }
}
