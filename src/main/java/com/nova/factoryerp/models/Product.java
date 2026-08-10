package com.nova.factoryerp.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private int id;
    private String productCode;
    private String name;
    private int categoryId;
    private String categoryName;
    private String description;
    private String unit;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private BigDecimal currentStock;
    private BigDecimal minStock;
    private String status;
    private LocalDateTime createdAt;

    public Product() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String c) { this.productCode = c; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int c) { this.categoryId = c; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String c) { this.categoryName = c; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getUnit() { return unit; }
    public void setUnit(String u) { this.unit = u; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal p) { this.sellingPrice = p; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal p) { this.costPrice = p; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal s) { this.currentStock = s; }
    public BigDecimal getMinStock() { return minStock; }
    public void setMinStock(BigDecimal m) { this.minStock = m; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    public boolean isLowStock() {
        return currentStock != null && minStock != null && currentStock.compareTo(minStock) <= 0;
    }
    @Override public String toString() { return name + " [" + productCode + "]"; }
}
