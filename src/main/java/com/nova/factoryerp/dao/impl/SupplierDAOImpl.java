package com.nova.factoryerp.dao.impl;

import com.nova.factoryerp.dao.interfaces.SupplierDAO;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDAOImpl implements SupplierDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("id"));
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));
        s.setContactName(rs.getString("contact_name"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setCity(rs.getString("city"));
        s.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
        return s;
    }

    @Override
    public List<Supplier> findAll() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM suppliers ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public List<Supplier> search(String keyword) throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE name LIKE ? OR code LIKE ? OR city LIKE ? ORDER BY name";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public Optional<Supplier> findById(int id) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM suppliers WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapRow(rs)); }
        } finally { db.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public void save(Supplier s) throws SQLException {
        String sql = "INSERT INTO suppliers (code,name,contact_name,phone,email,address,city,is_active) VALUES (?,?,?,?,?,?,?,?)";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getCode()); ps.setString(2, s.getName());
            ps.setString(3, s.getContactName()); ps.setString(4, s.getPhone());
            ps.setString(5, s.getEmail()); ps.setString(6, s.getAddress());
            ps.setString(7, s.getCity()); ps.setBoolean(8, s.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) s.setId(keys.getInt(1)); }
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void update(Supplier s) throws SQLException {
        String sql = "UPDATE suppliers SET code=?,name=?,contact_name=?,phone=?,email=?,address=?,city=?,is_active=? WHERE id=?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getCode()); ps.setString(2, s.getName());
            ps.setString(3, s.getContactName()); ps.setString(4, s.getPhone());
            ps.setString(5, s.getEmail()); ps.setString(6, s.getAddress());
            ps.setString(7, s.getCity()); ps.setBoolean(8, s.isActive());
            ps.setInt(9, s.getId()); ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void delete(int id) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE suppliers SET is_active=0 WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }
}
