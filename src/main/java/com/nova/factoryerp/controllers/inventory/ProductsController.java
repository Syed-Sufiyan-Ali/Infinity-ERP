package com.nova.factoryerp.controllers.inventory;

import com.nova.factoryerp.dao.impl.*;
import com.nova.factoryerp.dao.interfaces.*;
import com.nova.factoryerp.models.*;
import com.nova.factoryerp.utils.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProductsController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label countLabel;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> codeCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, String> categoryCol;
    @FXML private TableColumn<Product, String> unitCol;
    @FXML private TableColumn<Product, String> stockCol;
    @FXML private TableColumn<Product, String> minStockCol;
    @FXML private TableColumn<Product, String> sellPriceCol;
    @FXML private TableColumn<Product, String> costPriceCol;
    @FXML private TableColumn<Product, String> statusCol;
    @FXML private TableColumn<Product, Void> actionsCol;

    private final ProductDAO dao = new ProductDAOImpl();
    private final AuditLogDAO auditDAO = new AuditLogDAOImpl();
    private final ObservableList<Product> data = FXCollections.observableArrayList();
    private List<ProductCategory> allCategories;

    @FXML
    public void initialize() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        categoryCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getCategoryName() != null ? c.getValue().getCategoryName() : "-"));
        unitCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnit()));
        stockCol.setCellValueFactory(c -> new SimpleStringProperty(
            NumberUtil.formatNumber(c.getValue().getCurrentStock())));
        minStockCol.setCellValueFactory(c -> new SimpleStringProperty(
            NumberUtil.formatNumber(c.getValue().getMinStock())));
        sellPriceCol.setCellValueFactory(c -> new SimpleStringProperty(
            NumberUtil.formatCurrency(c.getValue().getSellingPrice())));
        costPriceCol.setCellValueFactory(c -> new SimpleStringProperty(
            NumberUtil.formatCurrency(c.getValue().getCostPrice())));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        productsTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty && item.isLowStock())
                    setStyle("-fx-background-color: rgba(231,76,60,0.12);");
                else setStyle("");
            }
        });

        actionsCol.setCellFactory(col -> new TableCell<>() {
            final Button edit = new Button("Edit");
            final Button del  = new Button("Delete");
            { edit.getStyleClass().add("btn-secondary"); del.getStyleClass().add("btn-danger");
              edit.setStyle("-fx-padding:4 10 4 10;-fx-font-size:11px;");
              del.setStyle("-fx-padding:4 10 4 10;-fx-font-size:11px;");
              edit.setOnAction(e -> showDialog(getTableView().getItems().get(getIndex())));
              del.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : new HBox(6, edit, del));
            }
        });

        statusFilter.setItems(FXCollections.observableArrayList("","ACTIVE","INACTIVE","DISCONTINUED"));
        productsTable.setItems(data);
        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                allCategories = dao.findAllCategories();
                String kw = searchField.getText();
                String st = statusFilter.getValue();
                String catName = categoryFilter.getValue();
                Integer catId = null;
                if (catName != null && !catName.isBlank()) {
                    catId = allCategories.stream().filter(c -> c.getName().equals(catName))
                        .map(ProductCategory::getId).findFirst().orElse(null);
                }
                List<Product> list = dao.search(kw, catId, st);
                final Integer finalCatId = catId;
                Platform.runLater(() -> {
                    categoryFilter.getItems().setAll("");
                    allCategories.forEach(c -> categoryFilter.getItems().add(c.getName()));
                    data.setAll(list);
                    countLabel.setText(list.size() + " products");
                });
            } catch (SQLException e) { Platform.runLater(() -> AlertUtil.showDatabaseError(e.getMessage())); }
        });
        t.setDaemon(true); t.start();
    }

    @FXML public void handleRefresh() { loadData(); }
    @FXML public void handleSearch() { loadData(); }
    @FXML public void handleAdd() { showDialog(null); }

    private void showDialog(Product existing) {
        boolean isNew = existing == null;
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Add Product" : "Edit Product");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/nova-erp.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-background-color:#1a1e2a;");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField codeF     = new TextField(isNew ? "" : existing.getProductCode());
        TextField nameF     = new TextField(isNew ? "" : existing.getName());
        TextField unitF     = new TextField(isNew ? "pcs" : existing.getUnit());
        TextField sellF     = new TextField(isNew ? "0" : existing.getSellingPrice().toPlainString());
        TextField costF     = new TextField(isNew ? "0" : existing.getCostPrice().toPlainString());
        TextField minStockF = new TextField(isNew ? "0" : existing.getMinStock().toPlainString());
        TextArea descF      = new TextArea(isNew ? "" : (existing.getDescription() != null ? existing.getDescription() : ""));
        descF.setPrefRowCount(2);

        ComboBox<ProductCategory> catCB = new ComboBox<>(FXCollections.observableArrayList(allCategories));
        catCB.setConverter(new StringConverter<>() {
            public String toString(ProductCategory c) { return c == null ? "" : c.getName(); }
            public ProductCategory fromString(String s) { return null; }
        });
        if (!isNew && existing.getCategoryId() > 0)
            allCategories.stream().filter(c -> c.getId() == existing.getCategoryId())
                .findFirst().ifPresent(catCB::setValue);

        ComboBox<String> statusCB = new ComboBox<>(FXCollections.observableArrayList("ACTIVE","INACTIVE","DISCONTINUED"));
        statusCB.setValue(isNew ? "ACTIVE" : existing.getStatus());

        String[] labels = {"Product Code *","Name *","Category","Unit *","Selling Price","Cost Price","Min Stock","Description","Status"};
        javafx.scene.Node[] fields = {codeF,nameF,catCB,unitF,sellF,costF,minStockF,descF,statusCB};
        for (int i = 0; i < labels.length; i++) {
            Label l = new Label(labels[i]); l.setStyle("-fx-text-fill:#9199b0;-fx-font-size:12px;");
            grid.add(l, 0, i); grid.add(fields[i], 1, i);
            GridPane.setHgrow(fields[i], Priority.ALWAYS);
        }
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            if (codeF.getText().isBlank() || nameF.getText().isBlank()) {
                AlertUtil.showWarning("Validation","Code and Name are required."); return null;
            }
            Product p = isNew ? new Product() : existing;
            p.setProductCode(codeF.getText().trim()); p.setName(nameF.getText().trim());
            p.setUnit(unitF.getText().trim()); p.setSellingPrice(NumberUtil.parseBigDecimal(sellF.getText()));
            p.setCostPrice(NumberUtil.parseBigDecimal(costF.getText()));
            p.setMinStock(NumberUtil.parseBigDecimal(minStockF.getText()));
            p.setDescription(descF.getText());
            p.setCategoryId(catCB.getValue() != null ? catCB.getValue().getId() : 0);
            p.setStatus(statusCB.getValue());
            if (isNew) p.setCurrentStock(BigDecimal.ZERO);
            return p;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(p -> {
            try {
                if (isNew) {
                    if (dao.findByCode(p.getProductCode()).isPresent()) {
                        AlertUtil.showError("Duplicate","Product code already exists: " + p.getProductCode()); return;
                    }
                    dao.save(p);
                    auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                        SessionManager.getInstance().getCurrentUsername(),"CREATE","INVENTORY","Created product: "+p.getName()));
                } else {
                    dao.update(p);
                    auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                        SessionManager.getInstance().getCurrentUsername(),"UPDATE","INVENTORY","Updated product: "+p.getName()));
                }
                loadData();
            } catch (SQLException e) { AlertUtil.showDatabaseError(e.getMessage()); }
        });
    }

    private void handleDelete(Product p) {
        if (!AlertUtil.showConfirm("Deactivate","Deactivate product: " + p.getName() + "?")) return;
        try {
            dao.delete(p.getId());
            auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                SessionManager.getInstance().getCurrentUsername(),"DELETE","INVENTORY","Deactivated product: "+p.getName()));
            loadData();
        } catch (SQLException e) { AlertUtil.showDatabaseError(e.getMessage()); }
    }
}
