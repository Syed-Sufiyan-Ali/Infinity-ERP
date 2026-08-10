package com.nova.factoryerp.models;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private int roleId;
    private String roleName;
    private boolean active;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getPassword() { return password; }
    public void setPassword(String p) { this.password = p; }
    public String getFullName() { return fullName; }
    public void setFullName(String n) { this.fullName = n; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public int getRoleId() { return roleId; }
    public void setRoleId(int r) { this.roleId = r; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String r) { this.roleName = r; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime l) { this.lastLogin = l; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }

    @Override public String toString() { return fullName + " (" + username + ")"; }
}
