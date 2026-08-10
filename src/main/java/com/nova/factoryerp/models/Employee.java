package com.nova.factoryerp.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Employee {
    private int id;
    private String employeeCode;
    private String fullName;
    private String fatherName;
    private String cnic;
    private String phone;
    private String email;
    private String address;
    private int departmentId;
    private String departmentName;
    private String designation;
    private LocalDate joiningDate;
    private String employmentType;
    private BigDecimal basicSalary;
    private String status;
    private LocalDateTime createdAt;

    public Employee() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String c) { this.employeeCode = c; }
    public String getFullName() { return fullName; }
    public void setFullName(String n) { this.fullName = n; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String n) { this.fatherName = n; }
    public String getCnic() { return cnic; }
    public void setCnic(String c) { this.cnic = c; }
    public String getPhone() { return phone; }
    public void setPhone(String p) { this.phone = p; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getAddress() { return address; }
    public void setAddress(String a) { this.address = a; }
    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int d) { this.departmentId = d; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String d) { this.departmentName = d; }
    public String getDesignation() { return designation; }
    public void setDesignation(String d) { this.designation = d; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate j) { this.joiningDate = j; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String e) { this.employmentType = e; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal s) { this.basicSalary = s; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
    @Override public String toString() { return fullName + " [" + employeeCode + "]"; }
}
