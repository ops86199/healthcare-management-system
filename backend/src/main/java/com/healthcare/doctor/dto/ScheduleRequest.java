package com.healthcare.doctor.dto;

import com.healthcare.doctor.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class ScheduleRequest {

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Slot start time is required")
    private LocalTime slotStart;

    @NotNull(message = "Slot end time is required")
    private LocalTime slotEnd;

    private boolean isActive = true;

    // ---- Getters & Setters ----

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getSlotStart() { return slotStart; }
    public void setSlotStart(LocalTime slotStart) { this.slotStart = slotStart; }

    public LocalTime getSlotEnd() { return slotEnd; }
    public void setSlotEnd(LocalTime slotEnd) { this.slotEnd = slotEnd; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
