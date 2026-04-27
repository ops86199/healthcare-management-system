package com.healthcare.doctor.dto;

import com.healthcare.doctor.enums.DayOfWeek;

import java.time.LocalTime;
import java.util.UUID;

public class ScheduleResponse {

    private UUID id;
    private DayOfWeek dayOfWeek;
    private LocalTime slotStart;
    private LocalTime slotEnd;
    private boolean isActive;

    // ---- Getters & Setters ----

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getSlotStart() { return slotStart; }
    public void setSlotStart(LocalTime slotStart) { this.slotStart = slotStart; }

    public LocalTime getSlotEnd() { return slotEnd; }
    public void setSlotEnd(LocalTime slotEnd) { this.slotEnd = slotEnd; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
