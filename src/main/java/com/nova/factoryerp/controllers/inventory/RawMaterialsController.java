package com.nova.factoryerp.controllers.inventory;

import com.nova.factoryerp.dao.impl.AuditLogDAOImpl;
import com.nova.factoryerp.dao.impl.RawMaterialDAOImpl;
import com.nova.factoryerp.dao.impl.SupplierDAOImpl;
import com.nova.factoryerp.dao.interfaces.AuditLogDAO;
import com.nova.factoryerp.dao.interfaces.RawMaterialDAO;
import com.nova.factoryerp.dao.interfaces.SupplierDAO;
import com.nova.factoryerp.models.AuditLog;
import com.nova.factoryerp.models.RawMaterial;
import com.nova.factoryerp.models.Supplier;
import com.nova.factoryerp.utils.AlertUtil;
import com.nova.factoryerp.utils.NumberUtil;
import com.nova.factoryerp.utils.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RawMaterialsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label countLabel;
    @FXML private TableView<RawMaterial> materialsTable;
    @FXML private TableColumn<RawMaterial, String> codeCol;
    @FXML private TableColumn<RawMaterial, String> nameCol;
    @FXML private TableColumn<RawMaterial, String> categoryCol;
    @FXML private TableColumn<RawMaterial, String> unitCol;
    @FXML private TableColumn<RawMaterial, String> stockCol;
    @FXML private TableColumn<RawMaterial, String> minStockCol;
    @FXML private TableColumn<RawMaterial, String> priceCol;
    @FXML private TableColumn<RawMaterial, String> supplierCol;
    @FXML private TableColumn<RawMaterial, String> locationCol;
    @FXML private TableColumn<RawMaterial, String> statusCol;
    @FXML private TableColumn<RawMaterial, Void> actionsCol;

    private final RawMaterialDAO rmDAO = new RawMaterialDAOImpl();
    private final SupplierDAO supplierDAO = new SupplierDAOImpl();
    private final AuditLogDAO auditDAO = new AuditLogDAOImpl();
    private final ObservableList<RawMaterial> data = FXCollections.observableArrayList();
    private List<Supplier> allSuppliers;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
    }

    private void setupTable() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        categoryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        unitCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnit()));
        stockCol.setCellValueFactory(c -> new SimpleStringProperty(
            NumberUtil.formatNumber(c.getValue().getCurrentStock())));
        minStockCol.setCellValueFactory(c -> new SimpleStringProperty(
            NumberUtil.formatNumber(c.getValue().getMinStock())));
        priceCol.setCellValueFactory(c -> new SimpleStringProperty(
            NumberUtil.formatCurrency(c.getValue().getPurchasePrice())));
        supplierCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getSupplierName() != null ? c.getValue().getSupplierName() : "-"));
        locationCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getLocation() != null ? c.getValue().getLocation() : "-"));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        // Color-code low stock rows
        materialsTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(RawMaterial item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty && item.isLowStock())
                    setStyle("-fx-background-color: rgba(231,76,60,0.12);");
                else setStyle("");
            }
        });

        // Actions column
        actionsCol.setCellFactory(col -> new TableCell<>() {
            final Button editBtn = new Button("Edit");
            final Button delBtn  = new Button("Delete");
            { editBtn.getStyleClass().add("btn-secondary");
              delBtn.getStyleClass().add("btn-danger");
              editBtn.setStyle("-fx-padding:4 10 4 10; -fx-font-size:11px;");
              delBtn.setStyle("-fx-padding:4 10 4 10; -fx-font-size:11px;");
              editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
              delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) setGraphic(null);
                else { HBox box = new HBox(6, editBtn, delBtn); setGraphic(box); }
            }
        });

        materialsTable.setItems(data);
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("", "ACTIVE","INACTIVE","DISCONTINUED"));
        categoryFilter.getItems().add("");
        try {
            rmDAO.findAllCategories().forEach(c -> categoryFilter.getItems().add(c));
        } catch (SQLException e) { /* ignore */ }
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                String kw  = searchField.getText();
                String cat = categoryFilter.getValue();
                String st  = statusFilter.getValue();
                List<RawMaterial> list = rmDAO.search(kw, cat, st);
                allSuppliers = supplierDAO.findAll();
                Platform.runLater(() -> {
                    data.setAll(list);
                    countLabel.setText(list.size() + " materials");
                });
            } catch (SQLException e) {
                Platform.runLater(() -> AlertUtil.showDatabaseError(e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML public void handleRefresh() { loadData(); }
    @FXML public void handleSearch() { loadData(); }

    @FXML
    public void handleAdd() {
        showEditDialog(null);
    }

    private void showEditDialog(RawMaterial existing) {
        boolean isNew = existing == null;
        Dialog<RawMaterial> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Add Raw Material" : "Edit Raw Material");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/nova-erp.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1e2a;");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField codeF     = new TextField(isNew ? "" : existing.getCode());
        TextField nameF     = new TextField(isNew ? "" : existing.getName());
        TextField categoryF = new TextField(isNew ? "" : existing.getCategory());
        TextField unitF     = new TextField(isNew ? "pcs" : existing.getUnit());
        TextField minStockF = new TextField(isNew ? "0" : existing.getMinStock().toPlainString());
        TextField maxStockF = new TextField(isNew ? "0" : existing.getMaxStock().toPlainString());
        TextField priceF    = new TextField(isNew ? "0" : existing.getPurchasePrice().toPlainString());
        TextField locationF = new TextField(isNew ? "" : (existing.getLocation() != null ? existing.getLocation() : ""));

        ComboBox<Supplier> supplierCB = new ComboBox<>();
        if (allSuppliers != null) supplierCB.setItems(FXCollections.observableArrayList(allSuppliers));
        supplierCB.setConverter(new StringConverter<>() {
            public String toString(Supplier s) { return s == null ? "" : s.getName(); }
            public Supplier fromString(String s) { return null; }
        });
        if (!isNew && existing.getSupplierId() > 0) {
            allSuppliers.stream().filter(s -> s.getId() == existing.getSupplierId())
                .findFirst().ifPresent(supplierCB::setValue);
        }

        ComboBox<String> statusCB = new ComboBox<>(
            FXCollections.observableArrayList("ACTIVE","INACTIVE","DISCONTINUED"));
        statusCB.setValue(isNew ? "ACTIVE" : existing.getStatus());

        String[] labels = {"Code *","Name *","Category","Unit *","Min Stock","Max Stock",
                           "Purchase Price","Location","Supplier","Status"};
        javafx.scene.Node[] fields = {codeF,nameF,categoryF,unitF,minStockF,maxStockF,
                                      priceF,locationF,supplierCB,statusCB};
        for (int i = 0; i < labels.length; i++) {
            Label lbl = new Label(labels[i]);
            lbl.setStyle("-fx-text-fill:#9199b0; -fx-font-size:12px;");
            grid.add(lbl, 0, i); grid.add(fields[i], 1, i);
            GridPane.setHgrow(fields[i], Priority.ALWAYS);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (codeF.getText().isBlank() || nameF.getText().isBlank() || unitF.getText().isBlank()) {
                    AlertUtil.showWarning("Validation", "Code, Name, and Unit are required.");
                    return null;
                }
                RawMaterial m = isNew ? new RawMaterial() : existing;
                m.setCode(codeF.getText().trim());
                m.setName(nameF.getText().trim());
                m.setCategory(categoryF.getText().trim());
                m.setUnit(unitF.getText().trim());
                m.setMinStock(NumberUtil.parseBigDecimal(minStockF.getText()));
                m.setMaxStock(NumberUtil.parseBigDecimal(maxStockF.getText()));
                m.setPurchasePrice(NumberUtil.parseBigDecimal(priceF.getText()));
                m.setLocation(locationF.getText().trim());
                m.setSupplierId(supplierCB.getValue() != null ? supplierCB.getValue().getId() : 0);
                m.setStatus(statusCB.getValue());
                if (isNew) m.setCurrentStock(BigDecimal.ZERO);
                return m;
            }
            return null;
        });

        Optional<RawMaterial> result = dialog.showAndWait();
        result.ifPresent(m -> {
            try {
                if (isNew) {
                    // Check duplicate code
                    if (rmDAO.findByCode(m.getCode()).isPresent()) {
                        AlertUtil.showError("Duplicate", "Material code already exists: " + m.getCode());
                        return;
                    }
                    rmDAO.save(m);
                    auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                        SessionManager.getInstance().getCurrentUsername(),
                        "CREATE", "INVENTORY", "Created raw material: " + m.getName()));
                } else {
                    rmDAO.update(m);
                    auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                        SessionManager.getInstance().getCurrentUsername(),
                        "UPDATE", "INVENTORY", "Updated raw material: " + m.getName()));
                }
                loadData();
            } catch (SQLException e) {
                AlertUtil.showDatabaseError(e.getMessage());
            }
        });
    }

    private void handleDelete(RawMaterial m) {
        if (!AlertUtil.showConfirm("Delete Material",
                "Deactivate material: " + m.getName() + "?\nStock data will be preserved.")) return;
        try {
            rmDAO.delete(m.getId());
            auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                SessionManager.getInstance().getCurrentUsername(),
                "DELETE", "INVENTORY", "Deactivated raw material: " + m.getName()));
            loadData();
        } catch (SQLException e) { AlertUtil.showDatabaseError(e.getMessage()); }
    }
}
