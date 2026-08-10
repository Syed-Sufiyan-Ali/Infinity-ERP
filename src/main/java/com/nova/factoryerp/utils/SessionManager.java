package com.nova.factoryerp.utils;

import com.nova.factoryerp.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public User getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }
    public void logout() { currentUser = null; }

    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "";
    }
    public String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : "";
    }
    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRoleName() : "";
    }
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    public boolean hasRole(String... roles) {
        if (currentUser == null) return false;
        String userRole = currentUser.getRoleName();
        for (String r : roles) {
            if (r.equalsIgnoreCase(userRole)) return true;
        }
        return false;
    }

    public boolean isAdmin() { return hasRole("ADMIN"); }
    public boolean isAdminOrManager() { return hasRole("ADMIN", "MANAGER"); }
}
