package com.healthcare.doctor.entity;

import jakarta.persistence.*;
import com.healthcare.doctor.enums.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;
@Entity
@Table(name = "doctor_schedules",
       uniqueConstraints = @UniqueConstraint(
           name = "idx_schedule_unique",
           columnNames = {"doctor_id", "day_of_week", "slot_start"}
       ))
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    @Column(name = "slot_start", nullable = false)
    private LocalTime slotStart;

    @Column(name = "slot_end", nullable = false)
    private LocalTime slotEnd;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // ---- Constructors ----

    public DoctorSchedule() {}

    // ---- Getters & Setters ----

    public UUID getId() { return id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getSlotStart() { return slotStart; }
    public void setSlotStart(LocalTime slotStart) { this.slotStart = slotStart; }

    public LocalTime getSlotEnd() { return slotEnd; }
    public void setSlotEnd(LocalTime slotEnd) { this.slotEnd = slotEnd; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
