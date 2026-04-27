package com.healthcare.appointment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class VitalSignRequest {

    @DecimalMin(value = "30.0", message = "Temperature must be at least 30.0°C")
    @DecimalMax(value = "45.0", message = "Temperature must not exceed 45.0°C")
    private BigDecimal temperatureC;

    @Min(value = 20,  message = "Pulse must be at least 20 bpm")
    @Max(value = 300, message = "Pulse must not exceed 300 bpm")
    private Integer pulseBpm;

    @Min(value = 50,  message = "Systolic BP must be at least 50 mmHg")
    @Max(value = 300, message = "Systolic BP must not exceed 300 mmHg")
    private Integer systolicBp;

    @Min(value = 20,  message = "Diastolic BP must be at least 20 mmHg")
    @Max(value = 200, message = "Diastolic BP must not exceed 200 mmHg")
    private Integer diastolicBp;

    @DecimalMin(value = "50.0",  message = "O₂ saturation must be at least 50%")
    @DecimalMax(value = "100.0", message = "O₂ saturation must not exceed 100%")
    private BigDecimal oxygenSaturation;

    @Min(value = 1,   message = "Respiratory rate must be at least 1")
    @Max(value = 100, message = "Respiratory rate must not exceed 100")
    private Integer respiratoryRate;

    @DecimalMin(value = "0.5",   message = "Weight must be at least 0.5 kg")
    @DecimalMax(value = "700.0", message = "Weight must not exceed 700 kg")
    private BigDecimal weightKg;

    @DecimalMin(value = "20.0",  message = "Height must be at least 20 cm")
    @DecimalMax(value = "300.0", message = "Height must not exceed 300 cm")
    private BigDecimal heightCm;

    // ---- Getters & Setters ----

    public BigDecimal getTemperatureC() { return temperatureC; }
    public void setTemperatureC(BigDecimal temperatureC) { this.temperatureC = temperatureC; }

    public Integer getPulseBpm() { return pulseBpm; }
    public void setPulseBpm(Integer pulseBpm) { this.pulseBpm = pulseBpm; }

    public Integer getSystolicBp() { return systolicBp; }
    public void setSystolicBp(Integer systolicBp) { this.systolicBp = systolicBp; }

    public Integer getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(Integer diastolicBp) { this.diastolicBp = diastolicBp; }

    public BigDecimal getOxygenSaturation() { return oxygenSaturation; }
    public void setOxygenSaturation(BigDecimal oxygenSaturation) { this.oxygenSaturation = oxygenSaturation; }

    public Integer getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(Integer respiratoryRate) { this.respiratoryRate = respiratoryRate; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }
}
