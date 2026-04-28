package com.healthcare.inventory.dto;

import java.util.UUID;

public class LowStockAlertResponse {

    private UUID medicineId;
    private String medicineName;
    private String category;
    private int reorderLevel;
    private int currentStock;
    private int deficit;          // reorderLevel - currentStock (always >= 0)

    // ---- Getters & Setters ----

    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getDeficit() { return deficit; }
    public void setDeficit(int deficit) { this.deficit = deficit; }
}
