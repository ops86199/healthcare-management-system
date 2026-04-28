package com.healthcare.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Compact projection used for paginated list endpoints. */
public class MedicineSummaryResponse {

    private UUID id;
    private String name;
    private String genericName;
    private String category;
    private String unit;
    private String strength;
    private BigDecimal price;
    private int reorderLevel;
    private int totalStock;
    private boolean belowReorderLevel;
    private boolean isActive;

    // ---- Getters & Setters ----

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public int getTotalStock() { return totalStock; }
    public void setTotalStock(int totalStock) { this.totalStock = totalStock; }

    public boolean isBelowReorderLevel() { return belowReorderLevel; }
    public void setBelowReorderLevel(boolean belowReorderLevel) {
        this.belowReorderLevel = belowReorderLevel;
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
