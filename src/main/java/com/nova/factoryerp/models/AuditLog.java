package com.nova.factoryerp.models;

import java.time.LocalDateTime;

public class AuditLog {
    private long id;
    private int userId;
    private String username;
    private String action;
    private String module;
    private String description;
    private LocalDateTime createdAt;

    public AuditLog() {}
    public AuditLog(int userId, String username, String action, String module, String description) {
        this.userId = userId; this.username = username; this.action = action;
        this.module = module; this.description = description;
    }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int u) { this.userId = u; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getAction() { return action; }
    public void setAction(String a) { this.action = a; }
    public String getModule() { return module; }
    public void setModule(String m) { this.module = m; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
