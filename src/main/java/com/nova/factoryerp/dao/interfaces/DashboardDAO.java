package com.nova.factoryerp.dao.interfaces;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

public interface DashboardDAO {
    BigDecimal getTotalSalesAmount() throws SQLException;
    BigDecimal getTodaySalesAmount() throws SQLException;
    int getTotalEmployees() throws SQLException;
    int getTodayPresentCount() throws SQLException;
    int getLowStockMaterialCount() throws SQLException;
    int getLowStockProductCount() throws SQLException;
    int getPendingProductionOrders() throws SQLException;
    int getPendingSalesOrders() throws SQLException;
    BigDecimal getFinishedProductStock() throws SQLException;
    Map<String, BigDecimal> getSalesByMonth(int year) throws SQLException;
}
