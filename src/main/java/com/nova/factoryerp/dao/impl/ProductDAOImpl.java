package com.nova.factoryerp.dao.impl;

import com.nova.factoryerp.dao.interfaces.ProductDAO;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.Product;
import com.nova.factoryerp.models.ProductCategory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAOImpl implements ProductDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String SELECT_BASE =
        "SELECT p.*, pc.name AS category_name FROM products p " +
        "LEFT JOIN product_categories pc ON p.category_id = pc.id ";

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setProductCode(rs.getString("product_code"));
        p.setName(rs.getString("name"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setDescription(rs.getString("description"));
        p.setUnit(rs.getString("unit"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setCostPrice(rs.getBigDecimal("cost_price"));
        p.setCurrentStock(rs.getBigDecimal("current_stock"));
        p.setMinStock(rs.getBigDecimal("min_stock"));
        p.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        return p;
    }

    @Override
    public List<Product> findAll() throws SQLException {
        List<Product> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY p.name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public List<Product> search(String keyword, Integer categoryId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (p.name LIKE ? OR p.product_code LIKE ?) ");
            params.add("%" + keyword + "%"); params.add("%" + keyword + "%");
        }
        if (categoryId != null) { sql.append("AND p.category_id = ? "); params.add(categoryId); }
        if (status != null && !status.isBlank()) { sql.append("AND p.status = ? "); params.add(status); }
        sql.append("ORDER BY p.name");
        List<Product> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public Optional<Product> findById(int id) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE p.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapRow(rs)); }
        } finally { db.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public Optional<Product> findByCode(String code) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE p.product_code=?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapRow(rs)); }
        } finally { db.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public List<ProductCategory> findAllCategories() throws SQLException {
        List<ProductCategory> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM product_categories ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductCategory c = new ProductCategory();
                c.setId(rs.getInt("id")); c.setCode(rs.getString("code"));
                c.setName(rs.getString("name")); c.setDescription(rs.getString("description"));
                c.setActive(rs.getBoolean("is_active"));
                list.add(c);
            }
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public void save(Product p) throws SQLException {
        String sql = "INSERT INTO products (product_code,name,category_id,description,unit," +
                     "selling_price,cost_price,current_stock,min_stock,status) VALUES (?,?,?,?,?,?,?,?,?,?)";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getProductCode()); ps.setString(2, p.getName());
            if (p.getCategoryId() > 0) ps.setInt(3, p.getCategoryId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, p.getDescription()); ps.setString(5, p.getUnit());
            ps.setBigDecimal(6, p.getSellingPrice()); ps.setBigDecimal(7, p.getCostPrice());
            ps.setBigDecimal(8, p.getCurrentStock() != null ? p.getCurrentStock() : BigDecimal.ZERO);
            ps.setBigDecimal(9, p.getMinStock() != null ? p.getMinStock() : BigDecimal.ZERO);
            ps.setString(10, p.getStatus() != null ? p.getStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) p.setId(keys.getInt(1)); }
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void update(Product p) throws SQLException {
        String sql = "UPDATE products SET product_code=?,name=?,category_id=?,description=?,unit=?," +
                     "selling_price=?,cost_price=?,min_stock=?,status=? WHERE id=?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getProductCode()); ps.setString(2, p.getName());
            if (p.getCategoryId() > 0) ps.setInt(3, p.getCategoryId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, p.getDescription()); ps.setString(5, p.getUnit());
            ps.setBigDecimal(6, p.getSellingPrice()); ps.setBigDecimal(7, p.getCostPrice());
            ps.setBigDecimal(8, p.getMinStock()); ps.setString(9, p.getStatus());
            ps.setInt(10, p.getId()); ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void delete(int id) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE products SET status='INACTIVE' WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void updateStock(int id, BigDecimal newStock) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE products SET current_stock=? WHERE id=?")) {
            ps.setBigDecimal(1, newStock); ps.setInt(2, id); ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void saveCategory(ProductCategory c) throws SQLException {
        String sql = "INSERT INTO product_categories (code,name,description,is_active) VALUES (?,?,?,?)";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCode()); ps.setString(2, c.getName());
            ps.setString(3, c.getDescription()); ps.setBoolean(4, c.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) c.setId(keys.getInt(1)); }
        } finally { db.releaseConnection(conn); }
    }
}
