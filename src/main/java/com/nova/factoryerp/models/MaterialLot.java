package com.nova.factoryerp.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MaterialLot {
    private int id;
    private String lotNumber;
    private int materialId;
    private String materialName;
    private String materialCode;
    private int supplierId;
    private String supplierName;
    private BigDecimal originalQty;
    private BigDecimal remainingQty;
    private LocalDate receivedDate;
    private LocalDate expiryDate;
    private BigDecimal purchasePrice;
    private String status;
    private String notes;
    private LocalDateTime createdAt;

    public MaterialLot() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String l) { this.lotNumber = l; }
    public int getMaterialId() { return materialId; }
    public void setMaterialId(int m) { this.materialId = m; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String m) { this.materialName = m; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String m) { this.materialCode = m; }
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int s) { this.supplierId = s; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String s) { this.supplierName = s; }
    public BigDecimal getOriginalQty() { return originalQty; }
    public void setOriginalQty(BigDecimal q) { this.originalQty = q; }
    public BigDecimal getRemainingQty() { return remainingQty; }
    public void setRemainingQty(BigDecimal q) { this.remainingQty = q; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate d) { this.receivedDate = d; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate d) { this.expiryDate = d; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal p) { this.purchasePrice = p; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    @Override public String toString() { return lotNumber + " - " + materialName; }
}
