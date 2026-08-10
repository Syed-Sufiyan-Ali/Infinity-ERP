package com.nova.factoryerp.dao.impl;

import com.nova.factoryerp.dao.interfaces.RawMaterialDAO;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.RawMaterial;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RawMaterialDAOImpl implements RawMaterialDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String SELECT_BASE =
        "SELECT rm.*, s.name AS supplier_name FROM raw_materials rm " +
        "LEFT JOIN suppliers s ON rm.supplier_id = s.id ";

    private RawMaterial mapRow(ResultSet rs) throws SQLException {
        RawMaterial m = new RawMaterial();
        m.setId(rs.getInt("id"));
        m.setCode(rs.getString("code"));
        m.setName(rs.getString("name"));
        m.setCategory(rs.getString("category"));
        m.setUnit(rs.getString("unit"));
        m.setCurrentStock(rs.getBigDecimal("current_stock"));
        m.setMinStock(rs.getBigDecimal("min_stock"));
        m.setMaxStock(rs.getBigDecimal("max_stock"));
        m.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        m.setLocation(rs.getString("location"));
        m.setSupplierId(rs.getInt("supplier_id"));
        m.setSupplierName(rs.getString("supplier_name"));
        m.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) m.setCreatedAt(ts.toLocalDateTime());
        return m;
    }

    @Override
    public List<RawMaterial> findAll() throws SQLException {
        List<RawMaterial> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY rm.name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public List<RawMaterial> search(String keyword, String category, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (rm.name LIKE ? OR rm.code LIKE ?) ");
            params.add("%" + keyword + "%"); params.add("%" + keyword + "%");
        }
        if (category != null && !category.isBlank()) {
            sql.append("AND rm.category = ? ");
            params.add(category);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND rm.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY rm.name");
        List<RawMaterial> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public Optional<RawMaterial> findById(int id) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE rm.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } finally { db.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public Optional<RawMaterial> findByCode(String code) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE rm.code = ?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } finally { db.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public List<String> findAllCategories() throws SQLException {
        List<String> cats = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT DISTINCT category FROM raw_materials WHERE category IS NOT NULL ORDER BY category");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cats.add(rs.getString(1));
        } finally { db.releaseConnection(conn); }
        return cats;
    }

    @Override
    public void save(RawMaterial m) throws SQLException {
        String sql = "INSERT INTO raw_materials (code,name,category,unit,current_stock,min_stock," +
                     "max_stock,purchase_price,location,supplier_id,status) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getCode()); ps.setString(2, m.getName());
            ps.setString(3, m.getCategory()); ps.setString(4, m.getUnit());
            ps.setBigDecimal(5, m.getCurrentStock()); ps.setBigDecimal(6, m.getMinStock());
            ps.setBigDecimal(7, m.getMaxStock()); ps.setBigDecimal(8, m.getPurchasePrice());
            ps.setString(9, m.getLocation());
            if (m.getSupplierId() > 0) ps.setInt(10, m.getSupplierId()); else ps.setNull(10, Types.INTEGER);
            ps.setString(11, m.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) m.setId(keys.getInt(1)); }
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void update(RawMaterial m) throws SQLException {
        String sql = "UPDATE raw_materials SET code=?,name=?,category=?,unit=?,min_stock=?," +
                     "max_stock=?,purchase_price=?,location=?,supplier_id=?,status=? WHERE id=?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getCode()); ps.setString(2, m.getName());
            ps.setString(3, m.getCategory()); ps.setString(4, m.getUnit());
            ps.setBigDecimal(5, m.getMinStock()); ps.setBigDecimal(6, m.getMaxStock());
            ps.setBigDecimal(7, m.getPurchasePrice()); ps.setString(8, m.getLocation());
            if (m.getSupplierId() > 0) ps.setInt(9, m.getSupplierId()); else ps.setNull(9, Types.INTEGER);
            ps.setString(10, m.getStatus()); ps.setInt(11, m.getId());
            ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void delete(int id) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE raw_materials SET status='INACTIVE' WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void updateStock(int id, BigDecimal newStock) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE raw_materials SET current_stock=? WHERE id=?")) {
            ps.setBigDecimal(1, newStock); ps.setInt(2, id); ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public List<RawMaterial> findLowStock() throws SQLException {
        List<RawMaterial> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                SELECT_BASE + "WHERE rm.current_stock <= rm.min_stock AND rm.status='ACTIVE' ORDER BY rm.name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } finally { db.releaseConnection(conn); }
        return list;
    }
}
