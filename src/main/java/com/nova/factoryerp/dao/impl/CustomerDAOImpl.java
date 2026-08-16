package com.nova.factoryerp.dao.impl;

import com.nova.factoryerp.dao.interfaces.CustomerDAO;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAOImpl implements CustomerDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();

        c.setId(rs.getInt("id"));
        c.setCustomerCode(rs.getString("customer_code"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        c.setOpeningBalance(rs.getBigDecimal("opening_balance"));
        c.setCurrentBalance(rs.getBigDecimal("current_balance"));
        c.setStatus(rs.getString("status"));

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            c.setCreatedAt(timestamp.toLocalDateTime());
        }

        return c;
    }

    @Override
    public List<Customer> search(String keyword, String status)
            throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT *
            FROM customers
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("""
                AND (
                    customer_code LIKE ?
                    OR name LIKE ?
                    OR phone LIKE ?
                    OR email LIKE ?
                    OR city LIKE ?
                )
                """);

            String search = "%" + keyword.trim() + "%";

            params.add(search);
            params.add(search);
            params.add(search);
            params.add(search);
            params.add(search);
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY name ASC");

        List<Customer> customers = new ArrayList<>();

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapRow(rs));
                }
            }

        } finally {
            db.releaseConnection(conn);
        }

        return customers;
    }

    @Override
    public Optional<Customer> findById(int id)
            throws SQLException {

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                         "SELECT * FROM customers WHERE id = ?")) {

            ps.setInt(1, id);

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
    public Optional<Customer> findByCode(String customerCode)
            throws SQLException {

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                         "SELECT * FROM customers WHERE customer_code = ?")) {

            ps.setString(1, customerCode);

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
    public void save(Customer customer)
            throws SQLException {

        String sql = """
            INSERT INTO customers
            (
                customer_code,
                name,
                phone,
                email,
                address,
                city,
                opening_balance,
                current_balance,
                status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                         sql,
                         Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getCustomerCode());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getAddress());
            ps.setString(6, customer.getCity());

            ps.setBigDecimal(
                7,
                customer.getOpeningBalance() != null
                    ? customer.getOpeningBalance()
                    : java.math.BigDecimal.ZERO
            );

            ps.setBigDecimal(
                8,
                customer.getCurrentBalance() != null
                    ? customer.getCurrentBalance()
                    : java.math.BigDecimal.ZERO
            );

            ps.setString(
                9,
                customer.getStatus() != null
                    ? customer.getStatus()
                    : "ACTIVE"
            );

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getInt(1));
                }
            }

        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public void update(Customer customer)
            throws SQLException {

        String sql = """
            UPDATE customers
            SET
                customer_code = ?,
                name = ?,
                phone = ?,
                email = ?,
                address = ?,
                city = ?,
                opening_balance = ?,
                status = ?
            WHERE id = ?
            """;

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, customer.getCustomerCode());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getAddress());
            ps.setString(6, customer.getCity());

            ps.setBigDecimal(
                7,
                customer.getOpeningBalance() != null
                    ? customer.getOpeningBalance()
                    : java.math.BigDecimal.ZERO
            );

            ps.setString(8, customer.getStatus());
            ps.setInt(9, customer.getId());

            ps.executeUpdate();

        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public void deactivate(int id)
            throws SQLException {

        Connection conn = db.getConnection();

        try (PreparedStatement ps =
                     conn.prepareStatement(
                         "UPDATE customers SET status='INACTIVE' WHERE id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } finally {
            db.releaseConnection(conn);
        }
    }
}