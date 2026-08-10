package com.nova.factoryerp.controllers;

import com.nova.factoryerp.security.AuthService;
import com.nova.factoryerp.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private VBox navContainer;
    @FXML private StackPane contentArea;

    private final AuthService authService = new AuthService();
    private final Map<String, Node> pageCache = new HashMap<>();
    private Button activeNavBtn;

    @FXML
    public void initialize() {
        SessionManager sm = SessionManager.getInstance();
        userLabel.setText(sm.getCurrentUserFullName());
        roleLabel.setText("[" + sm.getCurrentUserRole() + "]");
        buildSidebar();
        navigateTo("dashboard");
    }

    private void buildSidebar() {
        navContainer.getChildren().clear();
        SessionManager sm = SessionManager.getInstance();
        String role = sm.getCurrentUserRole();

        addSectionLabel("MAIN");
        addNavButton("📊  Dashboard",     "dashboard",   true);

        // Sales — ADMIN, MANAGER, SALES
        if (hasAccess(role, "ADMIN","MANAGER","SALES")) {
            addSectionLabel("SALES");
            addNavButton("🛒  Sales Orders",  "sales_orders",  false);
            addNavButton("👥  Customers",     "customers",     false);
            addNavButton("🧾  Invoices",      "invoices",      false);
            addNavButton("💳  Payments",      "payments",      false);
        }

        // Manufacturing — ADMIN, MANAGER, PRODUCTION
        if (hasAccess(role, "ADMIN","MANAGER","PRODUCTION")) {
            addSectionLabel("MANUFACTURING");
            addNavButton("⚙️  Production Orders", "production_orders", false);
            addNavButton("📦  Production Batches", "production_batches", false);
            addNavButton("📋  Bill of Materials",  "bom",               false);
        }

        // Inventory — ADMIN, MANAGER, INVENTORY, PRODUCTION
        if (hasAccess(role, "ADMIN","MANAGER","INVENTORY","PRODUCTION")) {
            addSectionLabel("INVENTORY");
            addNavButton("🔩  Raw Materials",     "raw_materials",    false);
            addNavButton("🏷️  Material Lots",     "material_lots",    false);
            addNavButton("💡  Finished Products",  "products",         false);
            addNavButton("🔢  Serial Numbers",     "serial_numbers",   false);
            addNavButton("📈  Stock Transactions", "stock_transactions",false);
            addNavButton("🏭  Suppliers",          "suppliers",        false);
        }

        // HR — ADMIN, MANAGER, HR
        if (hasAccess(role, "ADMIN","MANAGER","HR")) {
            addSectionLabel("HUMAN RESOURCES");
            addNavButton("👤  Employees",   "employees",  false);
            addNavButton("🏢  Departments", "departments",false);
            addNavButton("📅  Attendance",  "attendance", false);
            addNavButton("💰  Payroll",     "payroll",    false);
        }

        // Settings — ADMIN only
        if (hasAccess(role, "ADMIN")) {
            addSectionLabel("SYSTEM");
            addNavButton("👥  Users",       "users",      false);
            addNavButton("📋  Audit Log",   "audit_log",  false);
        }
    }

    private boolean hasAccess(String userRole, String... allowedRoles) {
        for (String r : allowedRoles) {
            if (r.equalsIgnoreCase(userRole)) return true;
        }
        return false;
    }

    private void addSectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("sidebar-section-label");
        lbl.setMaxWidth(Double.MAX_VALUE);
        navContainer.getChildren().add(lbl);
    }

    private void addNavButton(String text, String page, boolean isActive) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            setActiveButton(btn);
            navigateTo(page);
        });
        navContainer.getChildren().add(btn);
        if (isActive) {
            activeNavBtn = btn;
            btn.getStyleClass().add("active");
        }
    }

    private void setActiveButton(Button btn) {
        if (activeNavBtn != null) activeNavBtn.getStyleClass().remove("active");
        activeNavBtn = btn;
        btn.getStyleClass().add("active");
    }

    public void navigateTo(String page) {
        try {
            Node node = pageCache.computeIfAbsent(page, this::loadPage);
            if (node != null) {
                contentArea.getChildren().setAll(node);
            }
        } catch (Exception e) {
            log.error("Navigation error for page: {}", page, e);
        }
    }

    private Node loadPage(String page) {
        String fxmlFile = switch (page) {
            case "dashboard"          -> "/fxml/dashboard.fxml";
            case "sales_orders"       -> "/fxml/inventory/SalesOrdersView.fxml";
            case "customers"          -> "/fxml/inventory/CustomersView.fxml";
            case "invoices"           -> "/fxml/inventory/InvoicesView.fxml";
            case "payments"           -> "/fxml/inventory/PaymentsView.fxml";
            case "production_orders"  -> "/fxml/inventory/ProductionOrdersView.fxml";
            case "production_batches" -> "/fxml/inventory/ProductionBatchesView.fxml";
            case "bom"                -> "/fxml/inventory/BomView.fxml";
            case "raw_materials"      -> "/fxml/inventory/RawMaterialsView.fxml";
            case "material_lots"      -> "/fxml/inventory/MaterialLotsView.fxml";
            case "products"           -> "/fxml/inventory/ProductsView.fxml";
            case "serial_numbers"     -> "/fxml/inventory/SerialNumbersView.fxml";
            case "stock_transactions" -> "/fxml/inventory/StockTransactionsView.fxml";
            case "suppliers"          -> "/fxml/inventory/SuppliersView.fxml";
            case "employees"          -> "/fxml/inventory/EmployeesView.fxml";
            case "departments"        -> "/fxml/inventory/DepartmentsView.fxml";
            case "attendance"         -> "/fxml/inventory/AttendanceView.fxml";
            case "payroll"            -> "/fxml/inventory/PayrollView.fxml";
            case "users"              -> "/fxml/inventory/UsersView.fxml";
            case "audit_log"          -> "/fxml/inventory/AuditLogView.fxml";
            default                   -> "/fxml/dashboard.fxml";
        };

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent p = loader.load();
            p.prefWidth(Double.MAX_VALUE);
            return p;
        } catch (IOException e) {
            log.warn("FXML not yet implemented: {} — showing placeholder", fxmlFile);
            return buildPlaceholder(page);
        }
    }

    private Node buildPlaceholder(String page) {
        VBox box = new VBox();
        box.setStyle("-fx-alignment: center; -fx-spacing: 12; -fx-padding: 60;");
        Label icon = new Label("🚧");
        icon.setStyle("-fx-font-size: 48px;");
        Label title = new Label(page.replace("_", " ").toUpperCase());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4e8ef7;");
        Label sub = new Label("This module will be implemented in the next phase.");
        sub.setStyle("-fx-text-fill: #9199b0;");
        box.getChildren().addAll(icon, title, sub);
        return box;
    }

    @FXML
    public void handleLogout() {
        authService.logout();
        pageCache.clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nova Factory ERP — Login");
            stage.setScene(new javafx.scene.Scene(root, 600, 500));
            stage.setResizable(false);
            stage.show();
            Stage mainStage = (Stage) contentArea.getScene().getWindow();
            mainStage.close();
        } catch (IOException e) {
            log.error("Failed to open login window", e);
        }
    }
}
