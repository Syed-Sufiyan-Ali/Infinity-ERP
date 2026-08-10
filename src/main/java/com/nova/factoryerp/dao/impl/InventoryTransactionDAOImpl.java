package com.nova.factoryerp.dao.impl;

import com.nova.factoryerp.dao.interfaces.InventoryTransactionDAO;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.InventoryTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryTransactionDAOImpl implements InventoryTransactionDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String SELECT_BASE =
        "SELECT it.*, u.full_name AS performed_by_name, " +
        "CASE it.item_type WHEN 'RAW_MATERIAL' THEN rm.name ELSE p.name END AS item_name " +
        "FROM inventory_transactions it " +
        "LEFT JOIN users u ON it.performed_by = u.id " +
        "LEFT JOIN raw_materials rm ON it.item_type='RAW_MATERIAL' AND it.item_id=rm.id " +
        "LEFT JOIN products p ON it.item_type='FINISHED_PRODUCT' AND it.item_id=p.id ";

    private InventoryTransaction mapRow(ResultSet rs) throws SQLException {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setId(rs.getLong("id"));
        tx.setTransactionType(rs.getString("transaction_type"));
        tx.setItemType(rs.getString("item_type"));
        tx.setItemId(rs.getInt("item_id"));
        tx.setItemName(rs.getString("item_name"));
        tx.setQuantity(rs.getBigDecimal("quantity"));
        tx.setUnitPrice(rs.getBigDecimal("unit_price"));
        tx.setReferenceType(rs.getString("reference_type"));
        long refId = rs.getLong("reference_id");
        if (!rs.wasNull()) tx.setReferenceId(refId);
        tx.setNotes(rs.getString("notes"));
        tx.setPerformedBy(rs.getInt("performed_by"));
        tx.setPerformedByName(rs.getString("performed_by_name"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) tx.setCreatedAt(ts.toLocalDateTime());
        return tx;
    }

    @Override
    public void save(InventoryTransaction tx) throws SQLException {
        String sql = "INSERT INTO inventory_transactions " +
                     "(transaction_type,item_type,item_id,lot_id,quantity,unit_price," +
                     "reference_type,reference_id,notes,performed_by) VALUES (?,?,?,?,?,?,?,?,?,?)";
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tx.getTransactionType());
            ps.setString(2, tx.getItemType());
            ps.setInt(3, tx.getItemId());
            if (tx.getLotId() != null) ps.setInt(4, tx.getLotId()); else ps.setNull(4, Types.INTEGER);
            ps.setBigDecimal(5, tx.getQuantity());
            ps.setBigDecimal(6, tx.getUnitPrice() != null ? tx.getUnitPrice() : java.math.BigDecimal.ZERO);
            ps.setString(7, tx.getReferenceType());
            if (tx.getReferenceId() != null) ps.setLong(8, tx.getReferenceId()); else ps.setNull(8, Types.BIGINT);
            ps.setString(9, tx.getNotes());
            ps.setInt(10, tx.getPerformedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) tx.setId(keys.getLong(1)); }
        } finally { db.releaseConnection(conn); }
    }

    @Override
    public List<InventoryTransaction> findAll() throws SQLException {
        List<InventoryTransaction> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY it.created_at DESC LIMIT 500");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public List<InventoryTransaction> search(String keyword, String type, String itemType,
                                             String fromDate, String toDate) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (type != null && !type.isBlank()) { sql.append("AND it.transaction_type=? "); params.add(type); }
        if (itemType != null && !itemType.isBlank()) { sql.append("AND it.item_type=? "); params.add(itemType); }
        if (fromDate != null && !fromDate.isBlank()) { sql.append("AND DATE(it.created_at) >= ? "); params.add(fromDate); }
        if (toDate != null && !toDate.isBlank()) { sql.append("AND DATE(it.created_at) <= ? "); params.add(toDate); }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (rm.name LIKE ? OR p.name LIKE ? OR it.notes LIKE ?) ");
            String kw = "%" + keyword + "%";
            params.add(kw); params.add(kw); params.add(kw);
        }
        sql.append("ORDER BY it.created_at DESC LIMIT 500");
        List<InventoryTransaction> list = new ArrayList<>();
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
        } finally { db.releaseConnection(conn); }
        return list;
    }
}
