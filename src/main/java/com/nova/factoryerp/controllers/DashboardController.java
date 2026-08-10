package com.nova.factoryerp.controllers;

import com.nova.factoryerp.dao.impl.AuditLogDAOImpl;
import com.nova.factoryerp.dao.impl.DashboardDAOImpl;
import com.nova.factoryerp.dao.interfaces.AuditLogDAO;
import com.nova.factoryerp.dao.interfaces.DashboardDAO;
import com.nova.factoryerp.models.AuditLog;
import com.nova.factoryerp.utils.NumberUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @FXML private Label dateLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label todaySalesLabel;
    @FXML private Label pendingSalesLabel;
    @FXML private Label pendingProductionLabel;
    @FXML private Label employeesLabel;
    @FXML private Label attendanceLabel;
    @FXML private Label lowStockMatLabel;
    @FXML private Label finishedStockLabel;
    @FXML private BarChart<String, Number> salesChart;
    @FXML private TableView<AuditLog> activityTable;
    @FXML private TableColumn<AuditLog, String> activityTimeCol;
    @FXML private TableColumn<AuditLog, String> activityUserCol;
    @FXML private TableColumn<AuditLog, String> activityModuleCol;
    @FXML private TableColumn<AuditLog, String> activityDescCol;

    private final DashboardDAO dashboardDAO = new DashboardDAOImpl();
    private final AuditLogDAO auditDAO = new AuditLogDAOImpl();

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        setupActivityTable();
        loadDashboardData();
    }

    private void setupActivityTable() {
        activityTimeCol.setCellValueFactory(c -> {
            LocalDateTime dt = c.getValue().getCreatedAt();
            String s = dt == null ? "" : dt.format(DateTimeFormatter.ofPattern("dd-MM HH:mm"));
            return new SimpleStringProperty(s);
        });
        activityUserCol.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getUsername()));
        activityModuleCol.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getModule()));
        activityDescCol.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getDescription()));
    }

    @FXML
    public void handleRefresh() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        Thread t = new Thread(() -> {
            try {
                BigDecimal totalSales    = dashboardDAO.getTotalSalesAmount();
                BigDecimal todaySales    = dashboardDAO.getTodaySalesAmount();
                int employees            = dashboardDAO.getTotalEmployees();
                int attendance           = dashboardDAO.getTodayPresentCount();
                int lowStockMat          = dashboardDAO.getLowStockMaterialCount();
                int pendingSales         = dashboardDAO.getPendingSalesOrders();
                int pendingProd          = dashboardDAO.getPendingProductionOrders();
                BigDecimal finishedStock = dashboardDAO.getFinishedProductStock();
                Map<String, BigDecimal> salesByMonth = dashboardDAO.getSalesByMonth(LocalDate.now().getYear());
                List<AuditLog> recentActivity = auditDAO.findRecent(20);

                Platform.runLater(() -> {
                    totalSalesLabel.setText(NumberUtil.formatCurrency(totalSales));
                    todaySalesLabel.setText(NumberUtil.formatCurrency(todaySales));
                    employeesLabel.setText(String.valueOf(employees));
                    attendanceLabel.setText(String.valueOf(attendance));
                    lowStockMatLabel.setText(String.valueOf(lowStockMat));
                    pendingSalesLabel.setText(String.valueOf(pendingSales));
                    pendingProductionLabel.setText(String.valueOf(pendingProd));
                    finishedStockLabel.setText(NumberUtil.formatNumber(finishedStock));
                    updateSalesChart(salesByMonth);
                    activityTable.setItems(FXCollections.observableArrayList(recentActivity));
                });
            } catch (Exception e) {
                log.error("Dashboard load error", e);
                Platform.runLater(() ->
                    log.warn("Could not load dashboard data: {}", e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void updateSalesChart(Map<String, BigDecimal> data) {
        salesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales");
        data.forEach((month, amount) ->
            series.getData().add(new XYChart.Data<>(month, amount)));
        salesChart.getData().add(series);
        // Style bars
        salesChart.lookupAll(".bar").forEach(node ->
            node.setStyle("-fx-bar-fill: #4e8ef7;"));
    }
}
