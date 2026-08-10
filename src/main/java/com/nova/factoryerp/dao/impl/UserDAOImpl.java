package com.nova.factoryerp.dao.impl;

import com.nova.factoryerp.dao.interfaces.UserDAO;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {
    private static final Logger log = LoggerFactory.getLogger(UserDAOImpl.class);
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String SELECT_BASE =
        "SELECT u.id, u.username, u.password, u.full_name, u.email, " +
        "u.role_id, r.name AS role_name, u.is_active, u.last_login, u.created_at " +
        "FROM users u JOIN roles r ON u.role_id = r.id ";

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setRoleId(rs.getInt("role_id"));
        u.setRoleName(rs.getString("role_name"));
        u.setActive(rs.getBoolean("is_active"));
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) u.setLastLogin(lastLogin.toLocalDateTime());
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) u.setCreatedAt(created.toLocalDateTime());
        return u;
    }

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = SELECT_BASE + "WHERE u.username = ?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } finally { db.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE u.id = ?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } finally { db.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY u.full_name";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, full_name, email, role_id, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setInt(5, user.getRoleId());
            ps.setBoolean(6, user.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name=?, email=?, role_id=?, is_active=? WHERE id=?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getRoleId());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = NOW() WHERE id = ?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "UPDATE users SET is_active = 0 WHERE id = ?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }
}
