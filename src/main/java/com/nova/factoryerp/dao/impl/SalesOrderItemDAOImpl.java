package com.nova.factoryerp.dao.impl;

import com.nova.factoryerp.dao.interfaces.SalesOrderItemDAO;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.SalesOrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderItemDAOImpl
        implements SalesOrderItemDAO {

    private final DatabaseConnection db =
            DatabaseConnection.getInstance();

    private static final String SELECT_BASE =
            "SELECT soi.*, " +
            "p.name AS product_name, " +
            "p.product_code " +
            "FROM sales_order_items soi " +
            "JOIN products p ON soi.product_id = p.id ";

    private SalesOrderItem mapRow(ResultSet rs)
            throws SQLException {

        SalesOrderItem item =
                new SalesOrderItem();

        item.setId(rs.getLong("id"));
        item.setSalesOrderId(
                rs.getLong("sales_order_id")
        );

        item.setProductId(
                rs.getInt("product_id")
        );

        item.setProductName(
                rs.getString("product_name")
        );

        item.setProductCode(
                rs.getString("product_code")
        );

        item.setQuantity(
                rs.getInt("quantity")
        );

        item.setUnitPrice(
                rs.getBigDecimal("unit_price")
        );

        item.setDiscount(
                rs.getBigDecimal("discount")
        );

        item.setLineTotal(
                rs.getBigDecimal("line_total")
        );

        /*
         * bulbsPerBox is intentionally not read from DB yet.
         * The current schema does not have that field.
         */
        item.setBulbsPerBox(1);

        return item;
    }

    @Override
    public List<SalesOrderItem> findByOrderId(
            long salesOrderId
    ) throws SQLException {

        List<SalesOrderItem> list =
                new ArrayList<>();

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                             SELECT_BASE +
                             "WHERE soi.sales_order_id=? " +
                             "ORDER BY soi.id"
                     )) {

            ps.setLong(1, salesOrderId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } finally {
            db.releaseConnection(conn);
        }

        return list;
    }

    @Override
    public long save(SalesOrderItem item)
            throws SQLException {

        String sql =
                "INSERT INTO sales_order_items (" +
                "sales_order_id, product_id, quantity, " +
                "unit_price, discount, line_total" +
                ") VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            ps.setLong(1, item.getSalesOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());
            ps.setBigDecimal(5, item.getDiscount());
            ps.setBigDecimal(6, item.getLineTotal());

            ps.executeUpdate();

            try (ResultSet keys =
                         ps.getGeneratedKeys()) {

                if (keys.next()) {

                    long id =
                            keys.getLong(1);

                    item.setId(id);

                    return id;
                }
            }

        } finally {
            db.releaseConnection(conn);
        }

        return 0;
    }

    @Override
    public void deleteByOrderId(
            long salesOrderId
    ) throws SQLException {

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                             "DELETE FROM sales_order_items " +
                             "WHERE sales_order_id=?"
                     )) {

            ps.setLong(1, salesOrderId);
            ps.executeUpdate();

        } finally {
            db.releaseConnection(conn);
        }
    }
}