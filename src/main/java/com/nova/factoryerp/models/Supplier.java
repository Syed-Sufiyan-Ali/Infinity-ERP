package com.nova.factoryerp.models;

import java.time.LocalDateTime;

public class Supplier {
    private int id;
    private String code;
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String city;
    private boolean active;
    private LocalDateTime createdAt;

    public Supplier() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String c) { this.code = c; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getContactName() { return contactName; }
    public void setContactName(String c) { this.contactName = c; }
    public String getPhone() { return phone; }
    public void setPhone(String p) { this.phone = p; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getAddress() { return address; }
    public void setAddress(String a) { this.address = a; }
    public String getCity() { return city; }
    public void setCity(String c) { this.city = c; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    @Override public String toString() { return name + " [" + code + "]"; }
}
