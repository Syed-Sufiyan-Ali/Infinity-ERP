package com.nova.factoryerp.models;

public class ProductCategory {
    private int id;
    private String code;
    private String name;
    private String description;
    private boolean active;

    public ProductCategory() {}
    public ProductCategory(int id, String code, String name) {
        this.id = id; this.code = code; this.name = name;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String c) { this.code = c; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
    @Override public String toString() { return name; }
}
