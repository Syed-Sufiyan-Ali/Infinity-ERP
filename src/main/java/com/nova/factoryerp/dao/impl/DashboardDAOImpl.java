package com.nova.factoryerp.dao.impl;

import com.nova.factoryerp.dao.interfaces.DashboardDAO;
import com.nova.factoryerp.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDAOImpl implements DashboardDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private BigDecimal querySingle(String sql) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal v = rs.getBigDecimal(1);
                return v == null ? BigDecimal.ZERO : v;
            }
        } finally { db.releaseConnection(conn); }
        return BigDecimal.ZERO;
    }

    private int queryCount(String sql) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } finally { db.releaseConnection(conn); }
        return 0;
    }

    @Override
    public BigDecimal getTotalSalesAmount() throws SQLException {
        return querySingle("SELECT COALESCE(SUM(grand_total),0) FROM sales_orders WHERE status != 'CANCELLED'");
    }

    @Override
    public BigDecimal getTodaySalesAmount() throws SQLException {
        return querySingle("SELECT COALESCE(SUM(grand_total),0) FROM sales_orders WHERE DATE(order_date)=CURDATE() AND status != 'CANCELLED'");
    }

    @Override
    public int getTotalEmployees() throws SQLException {
        return queryCount("SELECT COUNT(*) FROM employees WHERE status='ACTIVE'");
    }

    @Override
    public int getTodayPresentCount() throws SQLException {
        return queryCount("SELECT COUNT(*) FROM attendance WHERE date=CURDATE() AND status IN ('PRESENT','LATE')");
    }

    @Override
    public int getLowStockMaterialCount() throws SQLException {
        return queryCount("SELECT COUNT(*) FROM raw_materials WHERE current_stock <= min_stock AND status='ACTIVE'");
    }

    @Override
    public int getLowStockProductCount() throws SQLException {
        return queryCount("SELECT COUNT(*) FROM products WHERE current_stock <= min_stock AND status='ACTIVE'");
    }

    @Override
    public int getPendingProductionOrders() throws SQLException {
        return queryCount("SELECT COUNT(*) FROM production_orders WHERE status IN ('DRAFT','IN_PROGRESS')");
    }

    @Override
    public int getPendingSalesOrders() throws SQLException {
        return queryCount("SELECT COUNT(*) FROM sales_orders WHERE status IN ('DRAFT','CONFIRMED')");
    }

    @Override
    public BigDecimal getFinishedProductStock() throws SQLException {
        return querySingle("SELECT COALESCE(SUM(current_stock),0) FROM products WHERE status='ACTIVE'");
    }

    @Override
    public Map<String, BigDecimal> getSalesByMonth(int year) throws SQLException {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        String sql = "SELECT MONTH(order_date) AS m, COALESCE(SUM(grand_total),0) AS total " +
                     "FROM sales_orders WHERE YEAR(order_date)=? AND status != 'CANCELLED' " +
                     "GROUP BY MONTH(order_date) ORDER BY m";
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        for (String m : months) result.put(m, BigDecimal.ZERO);
        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int m = rs.getInt("m");
                    if (m >= 1 && m <= 12) result.put(months[m-1], rs.getBigDecimal("total"));
                }
            }
        } finally { db.releaseConnection(conn); }
        return result;
    }
}
