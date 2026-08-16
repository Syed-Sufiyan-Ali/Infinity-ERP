package com.nova.factoryerp.dao.impl;

import com.nova.factoryerp.dao.interfaces.SalesOrderDAO;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.SalesOrder;
import com.nova.factoryerp.models.SalesOrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesOrderDAOImpl implements SalesOrderDAO {

    private final DatabaseConnection db =
            DatabaseConnection.getInstance();

    private static final String SELECT_BASE =
            "SELECT so.*, " +
            "c.name AS customer_name, " +
            "c.customer_code " +
            "FROM sales_orders so " +
            "JOIN customers c ON so.customer_id = c.id ";

    private SalesOrder mapRow(ResultSet rs)
            throws SQLException {

        SalesOrder order = new SalesOrder();

        order.setId(rs.getLong("id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerCode(rs.getString("customer_code"));

        Date date = rs.getDate("order_date");
        if (date != null) {
            order.setOrderDate(date.toLocalDate());
        }

        order.setSubtotal(rs.getBigDecimal("subtotal"));
        order.setDiscount(rs.getBigDecimal("discount"));
        order.setTax(rs.getBigDecimal("tax"));
        order.setGrandTotal(rs.getBigDecimal("grand_total"));
        order.setPaidAmount(rs.getBigDecimal("paid_amount"));
        order.setRemaining(rs.getBigDecimal("remaining"));

        order.setStatus(rs.getString("status"));
        order.setNotes(rs.getString("notes"));

        order.setCreatedBy(rs.getInt("created_by"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            order.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            order.setUpdatedAt(updated.toLocalDateTime());
        }

        return order;
    }

    @Override
    public List<SalesOrder> findAll()
            throws SQLException {

        List<SalesOrder> list = new ArrayList<>();

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                             SELECT_BASE +
                             "ORDER BY so.order_date DESC, so.id DESC"
                     );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } finally {
            db.releaseConnection(conn);
        }

        return list;
    }

    @Override
    public List<SalesOrder> search(
            String keyword,
            String status
    ) throws SQLException {

        StringBuilder sql =
                new StringBuilder(SELECT_BASE);

        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {

            sql.append(
                    "AND (" +
                    "so.order_number LIKE ? OR " +
                    "c.name LIKE ? OR " +
                    "c.customer_code LIKE ?" +
                    ") "
            );

            String value = "%" + keyword.trim() + "%";

            params.add(value);
            params.add(value);
            params.add(value);
        }

        if (status != null && !status.isBlank()) {

            sql.append(
                    "AND so.status = ? "
            );

            params.add(status);
        }

        sql.append(
                "ORDER BY so.order_date DESC, so.id DESC"
        );

        List<SalesOrder> list = new ArrayList<>();

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {

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
    public Optional<SalesOrder> findById(long id)
            throws SQLException {

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                             SELECT_BASE +
                             "WHERE so.id = ?"
                     )) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } finally {
            db.releaseConnection(conn);
        }

        return Optional.empty();
    }

    @Override
    public Optional<SalesOrder> findByOrderNumber(
            String orderNumber
    ) throws SQLException {

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                             SELECT_BASE +
                             "WHERE so.order_number = ?"
                     )) {

            ps.setString(1, orderNumber);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } finally {
            db.releaseConnection(conn);
        }

        return Optional.empty();
    }

    @Override
    public long save(SalesOrder order)
            throws SQLException {

        String sql =
                "INSERT INTO sales_orders (" +
                "order_number, customer_id, order_date, " +
                "subtotal, discount, tax, grand_total, " +
                "paid_amount, remaining, status, notes, created_by" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            ps.setString(1, order.getOrderNumber());
            ps.setInt(2, order.getCustomerId());
            ps.setDate(
                    3,
                    Date.valueOf(order.getOrderDate())
            );

            ps.setBigDecimal(4, order.getSubtotal());
            ps.setBigDecimal(5, order.getDiscount());
            ps.setBigDecimal(6, order.getTax());
            ps.setBigDecimal(7, order.getGrandTotal());
            ps.setBigDecimal(8, order.getPaidAmount());
            ps.setBigDecimal(9, order.getRemaining());

            ps.setString(10, order.getStatus());
            ps.setString(11, order.getNotes());

            if (order.getCreatedBy() > 0) {
                ps.setInt(12, order.getCreatedBy());
            } else {
                ps.setNull(12, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet keys =
                         ps.getGeneratedKeys()) {

                if (keys.next()) {

                    long id = keys.getLong(1);
                    order.setId(id);

                    return id;
                }
            }

        } finally {
            db.releaseConnection(conn);
        }

        return 0;
    }

    @Override
    public void update(SalesOrder order)
            throws SQLException {

        String sql =
                "UPDATE sales_orders SET " +
                "customer_id=?, " +
                "order_date=?, " +
                "subtotal=?, " +
                "discount=?, " +
                "tax=?, " +
                "grand_total=?, " +
                "paid_amount=?, " +
                "remaining=?, " +
                "status=?, " +
                "notes=? " +
                "WHERE id=?";

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, order.getCustomerId());
            ps.setDate(
                    2,
                    Date.valueOf(order.getOrderDate())
            );

            ps.setBigDecimal(3, order.getSubtotal());
            ps.setBigDecimal(4, order.getDiscount());
            ps.setBigDecimal(5, order.getTax());
            ps.setBigDecimal(6, order.getGrandTotal());
            ps.setBigDecimal(7, order.getPaidAmount());
            ps.setBigDecimal(8, order.getRemaining());

            ps.setString(9, order.getStatus());
            ps.setString(10, order.getNotes());

            ps.setLong(11, order.getId());

            ps.executeUpdate();

        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public void cancel(long id)
            throws SQLException {

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                             "UPDATE sales_orders " +
                             "SET status='CANCELLED' " +
                             "WHERE id=?"
                     )) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } finally {
            db.releaseConnection(conn);
        }
    }
}