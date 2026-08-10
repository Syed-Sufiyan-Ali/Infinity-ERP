package com.nova.factoryerp.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Customer {
    private int id;
    private String customerCode;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String city;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private String status;
    private LocalDateTime createdAt;

    public Customer() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String c) { this.customerCode = c; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getPhone() { return phone; }
    public void setPhone(String p) { this.phone = p; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getAddress() { return address; }
    public void setAddress(String a) { this.address = a; }
    public String getCity() { return city; }
    public void setCity(String c) { this.city = c; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal b) { this.openingBalance = b; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal b) { this.currentBalance = b; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    @Override public String toString() { return name + " [" + customerCode + "]"; }
}
