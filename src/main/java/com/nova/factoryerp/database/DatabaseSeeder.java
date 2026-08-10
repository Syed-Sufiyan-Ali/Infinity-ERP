package com.nova.factoryerp.database;

import com.nova.factoryerp.utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Ensures the admin user has a correct BCrypt password hash.
 * Called on application startup after DB connection is verified.
 */
public class DatabaseSeeder {
    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    public static void ensureAdminPassword() {
        DatabaseConnection db = DatabaseConnection.getInstance();
        try {
            Connection conn = db.getConnection();
            try {
                // Check if admin password is valid BCrypt
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT password FROM users WHERE username = 'admin'");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String hash = rs.getString("password");
                        // If hash doesn't look like BCrypt, fix it
                        if (hash == null || !hash.startsWith("$2a$")) {
                            String newHash = PasswordUtil.hash("admin123");
                            try (PreparedStatement upd = conn.prepareStatement(
                                    "UPDATE users SET password = ? WHERE username = 'admin'")) {
                                upd.setString(1, newHash);
                                upd.executeUpdate();
                                log.info("Admin password hash fixed automatically");
                            }
                        }
                    }
                }
                // Same for manager
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT password FROM users WHERE username = 'manager'");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String hash = rs.getString("password");
                        if (hash == null || !hash.startsWith("$2a$")) {
                            String newHash = PasswordUtil.hash("manager123");
                            try (PreparedStatement upd = conn.prepareStatement(
                                    "UPDATE users SET password = ? WHERE username = 'manager'")) {
                                upd.setString(1, newHash);
                                upd.executeUpdate();
                                log.info("Manager password hash fixed automatically");
                            }
                        }
                    }
                }
            } finally {
                db.releaseConnection(conn);
            }
        } catch (SQLException e) {
            log.warn("Could not verify/fix seed passwords: {}", e.getMessage());
        }
    }
}
