package com.nova.factoryerp.models;

public class Role {
    public static final String ADMIN      = "ADMIN";
    public static final String MANAGER    = "MANAGER";
    public static final String INVENTORY  = "INVENTORY";
    public static final String PRODUCTION = "PRODUCTION";
    public static final String SALES      = "SALES";
    public static final String HR         = "HR";

    private int id;
    private String name;
    private String description;

    public Role() {}
    public Role(int id, String name, String description) {
        this.id = id; this.name = name; this.description = description;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    @Override public String toString() { return name; }
}
