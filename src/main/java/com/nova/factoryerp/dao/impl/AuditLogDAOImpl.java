package com.nova.factoryerp.dao.impl;

import com.nova.factoryerp.dao.interfaces.AuditLogDAO;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl implements AuditLogDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog a = new AuditLog();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setUsername(rs.getString("username"));
        a.setAction(rs.getString("action"));
        a.setModule(rs.getString("module"));
        a.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        return a;
    }

    @Override
    public void log(AuditLog entry) throws SQLException {
        String sql = "INSERT INTO audit_logs (user_id, username, action, module, description) VALUES (?,?,?,?,?)";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entry.getUserId());
            ps.setString(2, entry.getUsername());
            ps.setString(3, entry.getAction());
            ps.setString(4, entry.getModule());
            ps.setString(5, entry.getDescription());
            ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public List<AuditLog> findRecent(int limit) throws SQLException {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT ?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public List<AuditLog> findByUser(int userId) throws SQLException {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id=? ORDER BY created_at DESC LIMIT 100";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } finally { db.releaseConnection(conn); }
        return list;
    }
}
