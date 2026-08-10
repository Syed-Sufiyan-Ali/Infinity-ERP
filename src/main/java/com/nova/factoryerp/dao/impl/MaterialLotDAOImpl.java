package com.nova.factoryerp.dao.impl;

import com.nova.factoryerp.dao.interfaces.MaterialLotDAO;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.MaterialLot;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaterialLotDAOImpl implements MaterialLotDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String SELECT_BASE =
        "SELECT ml.*, rm.name AS material_name, rm.code AS material_code, s.name AS supplier_name " +
        "FROM material_lots ml " +
        "JOIN raw_materials rm ON ml.material_id = rm.id " +
        "LEFT JOIN suppliers s ON ml.supplier_id = s.id ";

    private MaterialLot mapRow(ResultSet rs) throws SQLException {
        MaterialLot l = new MaterialLot();
        l.setId(rs.getInt("id"));
        l.setLotNumber(rs.getString("lot_number"));
        l.setMaterialId(rs.getInt("material_id"));
        l.setMaterialName(rs.getString("material_name"));
        l.setMaterialCode(rs.getString("material_code"));
        l.setSupplierId(rs.getInt("supplier_id"));
        l.setSupplierName(rs.getString("supplier_name"));
        l.setOriginalQty(rs.getBigDecimal("original_qty"));
        l.setRemainingQty(rs.getBigDecimal("remaining_qty"));
        Date rd = rs.getDate("received_date");
        if (rd != null) l.setReceivedDate(rd.toLocalDate());
        Date ed = rs.getDate("expiry_date");
        if (ed != null) l.setExpiryDate(ed.toLocalDate());
        l.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        l.setStatus(rs.getString("status"));
        l.setNotes(rs.getString("notes"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) l.setCreatedAt(ts.toLocalDateTime());
        return l;
    }

    @Override
    public List<MaterialLot> findAll() throws SQLException {
        List<MaterialLot> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY ml.received_date DESC, ml.lot_number");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public List<MaterialLot> search(String keyword, Integer materialId, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (ml.lot_number LIKE ? OR rm.name LIKE ? OR rm.code LIKE ?) ");
            params.add("%" + keyword + "%"); params.add("%" + keyword + "%"); params.add("%" + keyword + "%");
        }
        if (materialId != null) { sql.append("AND ml.material_id = ? "); params.add(materialId); }
        if (status != null && !status.isBlank()) { sql.append("AND ml.status = ? "); params.add(status); }
        sql.append("ORDER BY ml.received_date DESC");
        List<MaterialLot> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public List<MaterialLot> findByMaterial(int materialId) throws SQLException {
        List<MaterialLot> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE ml.material_id=? ORDER BY ml.received_date DESC")) {
            ps.setInt(1, materialId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public List<MaterialLot> findAvailableByMaterial(int materialId) throws SQLException {
        List<MaterialLot> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                SELECT_BASE + "WHERE ml.material_id=? AND ml.status IN ('AVAILABLE','PARTIALLY_USED') AND ml.remaining_qty > 0 ORDER BY ml.received_date ASC")) {
            ps.setInt(1, materialId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public Optional<MaterialLot> findById(int id) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE ml.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapRow(rs)); }
        } finally { db.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public void save(MaterialLot lot) throws SQLException {
        String sql = "INSERT INTO material_lots (lot_number,material_id,supplier_id,original_qty,remaining_qty," +
                     "received_date,expiry_date,purchase_price,status,notes,created_by) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, lot.getLotNumber());
            ps.setInt(2, lot.getMaterialId());
            if (lot.getSupplierId() > 0) ps.setInt(3, lot.getSupplierId()); else ps.setNull(3, Types.INTEGER);
            ps.setBigDecimal(4, lot.getOriginalQty());
            ps.setBigDecimal(5, lot.getRemainingQty());
            ps.setDate(6, lot.getReceivedDate() != null ? Date.valueOf(lot.getReceivedDate()) : Date.valueOf(java.time.LocalDate.now()));
            ps.setDate(7, lot.getExpiryDate() != null ? Date.valueOf(lot.getExpiryDate()) : null);
            ps.setBigDecimal(8, lot.getPurchasePrice());
            ps.setString(9, lot.getStatus() != null ? lot.getStatus() : "AVAILABLE");
            ps.setString(10, lot.getNotes());
            ps.setObject(11, null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) lot.setId(keys.getInt(1)); }
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void update(MaterialLot lot) throws SQLException {
        String sql = "UPDATE material_lots SET lot_number=?,supplier_id=?,original_qty=?,remaining_qty=?," +
                     "received_date=?,expiry_date=?,purchase_price=?,status=?,notes=? WHERE id=?";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lot.getLotNumber());
            if (lot.getSupplierId() > 0) ps.setInt(2, lot.getSupplierId()); else ps.setNull(2, Types.INTEGER);
            ps.setBigDecimal(3, lot.getOriginalQty()); ps.setBigDecimal(4, lot.getRemainingQty());
            ps.setDate(5, lot.getReceivedDate() != null ? Date.valueOf(lot.getReceivedDate()) : null);
            ps.setDate(6, lot.getExpiryDate() != null ? Date.valueOf(lot.getExpiryDate()) : null);
            ps.setBigDecimal(7, lot.getPurchasePrice()); ps.setString(8, lot.getStatus());
            ps.setString(9, lot.getNotes()); ps.setInt(10, lot.getId());
            ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void updateRemainingQty(int id, BigDecimal qty) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE material_lots SET remaining_qty=? WHERE id=?")) {
            ps.setBigDecimal(1, qty); ps.setInt(2, id); ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public void updateStatus(int id, String status) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE material_lots SET status=? WHERE id=?")) {
            ps.setString(1, status); ps.setInt(2, id); ps.executeUpdate();
        } finally { db.releaseConnection(conn); }
    }
}
