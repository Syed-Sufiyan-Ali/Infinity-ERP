package com.nova.factoryerp.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RawMaterial {
    private int id;
    private String code;
    private String name;
    private String category;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal minStock;
    private BigDecimal maxStock;
    private BigDecimal purchasePrice;
    private String location;
    private int supplierId;
    private String supplierName;
    private String status;
    private LocalDateTime createdAt;

    public RawMaterial() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String c) { this.code = c; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getCategory() { return category; }
    public void setCategory(String c) { this.category = c; }
    public String getUnit() { return unit; }
    public void setUnit(String u) { this.unit = u; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal s) { this.currentStock = s; }
    public BigDecimal getMinStock() { return minStock; }
    public void setMinStock(BigDecimal m) { this.minStock = m; }
    public BigDecimal getMaxStock() { return maxStock; }
    public void setMaxStock(BigDecimal m) { this.maxStock = m; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal p) { this.purchasePrice = p; }
    public String getLocation() { return location; }
    public void setLocation(String l) { this.location = l; }
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int s) { this.supplierId = s; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String s) { this.supplierName = s; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    public boolean isLowStock() {
        return currentStock != null && minStock != null && currentStock.compareTo(minStock) <= 0;
    }
    @Override public String toString() { return name + " [" + code + "]"; }
}
