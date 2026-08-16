package com.nova.factoryerp.models;

import java.math.BigDecimal;

public class SalesOrderItem {

    private long id;
    private long salesOrderId;

    private int productId;
    private String productName;
    private String productCode;

    /*
     * IMPORTANT:
     * quantity represents BOXES at the current stage.
     */
    private int quantity;

    /*
     * Number of bulbs contained in each box.
     * This is application-level information for now.
     */
    private int bulbsPerBox = 1;

    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal lineTotal = BigDecimal.ZERO;

    public SalesOrderItem() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(long salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getBulbsPerBox() {
        return bulbsPerBox;
    }

    public void setBulbsPerBox(int bulbsPerBox) {
        this.bulbsPerBox = bulbsPerBox;
    }

    public int getTotalBulbs() {
        return quantity * bulbsPerBox;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public void calculateLineTotal() {
        BigDecimal quantityAmount =
                unitPrice.multiply(BigDecimal.valueOf(quantity));

        lineTotal = quantityAmount.subtract(
                discount != null ? discount : BigDecimal.ZERO
        );

        if (lineTotal.compareTo(BigDecimal.ZERO) < 0) {
            lineTotal = BigDecimal.ZERO;
        }
    }
}