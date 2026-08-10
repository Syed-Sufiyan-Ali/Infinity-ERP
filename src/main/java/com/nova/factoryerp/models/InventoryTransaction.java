package com.nova.factoryerp.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryTransaction {
    private long id;
    private String transactionType;
    private String itemType;
    private int itemId;
    private String itemName;
    private Integer lotId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String referenceType;
    private Long referenceId;
    private String notes;
    private int performedBy;
    private String performedByName;
    private LocalDateTime createdAt;

    public InventoryTransaction() {}
    public InventoryTransaction(String txType, String itemType, int itemId,
                                BigDecimal qty, int userId, String notes) {
        this.transactionType = txType; this.itemType = itemType; this.itemId = itemId;
        this.quantity = qty; this.performedBy = userId; this.notes = notes;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String t) { this.transactionType = t; }
    public String getItemType() { return itemType; }
    public void setItemType(String t) { this.itemType = t; }
    public int getItemId() { return itemId; }
    public void setItemId(int id) { this.itemId = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String n) { this.itemName = n; }
    public Integer getLotId() { return lotId; }
    public void setLotId(Integer l) { this.lotId = l; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal q) { this.quantity = q; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal p) { this.unitPrice = p; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String r) { this.referenceType = r; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long r) { this.referenceId = r; }
    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }
    public int getPerformedBy() { return performedBy; }
    public void setPerformedBy(int p) { this.performedBy = p; }
    public String getPerformedByName() { return performedByName; }
    public void setPerformedByName(String n) { this.performedByName = n; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
