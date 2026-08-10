package com.nova.factoryerp.database;

import com.nova.factoryerp.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple connection pool for MySQL.
 * Thread-safe, reuses connections, validates before lending.
 */
public class DatabaseConnection {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);
    private static DatabaseConnection instance;

    private final String url;
    private final String username;
    private final String password;
    private final int poolSize;

    private final List<Connection> pool = new ArrayList<>();
    private final List<Connection> inUse = new ArrayList<>();

    private DatabaseConnection() {
        AppConfig cfg = AppConfig.getInstance();
        this.url      = cfg.getDbUrl();
        this.username = cfg.getDbUsername();
        this.password = cfg.getDbPassword();
        this.poolSize = cfg.getInt("db.pool.size", 10);
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) instance = new DatabaseConnection();
        return instance;
    }

    /**
     * Test connectivity — throws SQLException with a friendly message if it fails.
     */
    public void testConnection() throws SQLException {
        try {
            Connection c = DriverManager.getConnection(url, username, password);
            c.close();
            log.info("Database connection test successful");
        } catch (SQLException e) {
            log.error("Database connection test failed: {}", e.getMessage());
            throw new SQLException(
                "Unable to connect to MySQL.\n\n" +
                "Please check:\n" +
                "  • MySQL service is running\n" +
                "  • Database 'factory_management' exists\n" +
                "  • Username/password in application.properties are correct\n" +
                "  • URL: " + url, e);
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        // Return a free, valid connection from pool
        for (Connection c : pool) {
            if (c.isValid(2)) {
                pool.remove(c);
                inUse.add(c);
                return c;
            }
        }
        // Create new connection if pool not exhausted
        if (inUse.size() < poolSize) {
            Connection c = DriverManager.getConnection(url, username, password);
            c.setAutoCommit(true);
            inUse.add(c);
            log.debug("New DB connection created (pool in-use: {})", inUse.size());
            return c;
        }
        throw new SQLException("Connection pool exhausted. Please try again.");
    }

    public synchronized void releaseConnection(Connection c) {
        if (c == null) return;
        inUse.remove(c);
        try {
            if (!c.isClosed()) {
                c.setAutoCommit(true);
                pool.add(c);
            }
        } catch (SQLException e) {
            log.warn("Error returning connection to pool: {}", e.getMessage());
        }
    }

    public synchronized void closeAll() {
        List<Connection> all = new ArrayList<>(pool);
        all.addAll(inUse);
        for (Connection c : all) {
            try { if (!c.isClosed()) c.close(); } catch (SQLException ignored) {}
        }
        pool.clear();
        inUse.clear();
        log.info("All database connections closed");
    }
}
