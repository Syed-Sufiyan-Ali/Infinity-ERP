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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class StockTransactionsController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> itemTypeFilter;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Label countLabel;
    @FXML private TableView<InventoryTransaction> txTable;
    @FXML private TableColumn<InventoryTransaction, String> idCol;
    @FXML private TableColumn<InventoryTransaction, String> dateCol;
    @FXML private TableColumn<InventoryTransaction, String> typeCol;
    @FXML private TableColumn<InventoryTransaction, String> itemTypeCol;
    @FXML private TableColumn<InventoryTransaction, String> itemCol;
    @FXML private TableColumn<InventoryTransaction, String> qtyCol;
    @FXML private TableColumn<InventoryTransaction, String> refCol;
    @FXML private TableColumn<InventoryTransaction, String> userCol;
    @FXML private TableColumn<InventoryTransaction, String> notesCol;

    private final InventoryTransactionDAO txDAO   = new InventoryTransactionDAOImpl();
    private final RawMaterialDAO rmDAO            = new RawMaterialDAOImpl();
    private final ProductDAO productDAO           = new ProductDAOImpl();
    private final AuditLogDAO auditDAO            = new AuditLogDAOImpl();
    private final ObservableList<InventoryTransaction> data = FXCollections.observableArrayList();
    private List<RawMaterial> allMaterials;
    private List<Product> allProducts;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().format(DT_FMT) : ""));
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionType()));
        itemTypeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getItemType()));
        itemCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getItemName() != null ? c.getValue().getItemName() : "ID:" + c.getValue().getItemId()));
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(
            NumberUtil.formatNumber(c.getValue().getQuantity())));
        refCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getReferenceType() != null ? c.getValue().getReferenceType() +
            (c.getValue().getReferenceId() != null ? " #"+c.getValue().getReferenceId() : "") : "-"));
        userCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getPerformedByName() != null ? c.getValue().getPerformedByName() : "-"));
        notesCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getNotes() != null ? c.getValue().getNotes() : ""));

        typeFilter.setItems(FXCollections.observableArrayList("","PURCHASE","PRODUCTION_IN","PRODUCTION_OUT",
            "SALE","SALE_RETURN","ADJUSTMENT_IN","ADJUSTMENT_OUT","DAMAGE","TRANSFER"));
        itemTypeFilter.setItems(FXCollections.observableArrayList("","RAW_MATERIAL","FINISHED_PRODUCT"));

        txTable.setItems(data);
        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                allMaterials = rmDAO.findAll();
                allProducts  = productDAO.findAll();
                String kw   = searchField.getText();
                String type = typeFilter.getValue();
                String itype= itemTypeFilter.getValue();
                String from = fromDate.getValue() != null ? fromDate.getValue().toString() : null;
                String to   = toDate.getValue()   != null ? toDate.getValue().toString()   : null;
                List<InventoryTransaction> list = txDAO.search(kw, type, itype, from, to);
                Platform.runLater(() -> { data.setAll(list); countLabel.setText(list.size() + " transactions"); });
            } catch (SQLException e) { Platform.runLater(() -> AlertUtil.showDatabaseError(e.getMessage())); }
        });
        t.setDaemon(true); t.start();
    }

    @FXML public void handleRefresh() { loadData(); }
    @FXML public void handleSearch() { loadData(); }

    @FXML
    public void handleAdjustment() {
        // Stock adjustment dialog
        Dialog<InventoryTransaction> dialog = new Dialog<>();
        dialog.setTitle("Stock Adjustment");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/nova-erp.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-background-color:#1a1e2a;");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(20));

        ComboBox<String> adjTypeCB = new ComboBox<>(
            FXCollections.observableArrayList("ADJUSTMENT_IN","ADJUSTMENT_OUT","DAMAGE"));
        adjTypeCB.setValue("ADJUSTMENT_IN");

        ComboBox<String> itemTypeCB = new ComboBox<>(
            FXCollections.observableArrayList("RAW_MATERIAL","FINISHED_PRODUCT"));
        itemTypeCB.setValue("RAW_MATERIAL");

        ComboBox<Object> itemCB = new ComboBox<>();
        itemCB.setConverter(new StringConverter<>() {
            public String toString(Object o) {
                if (o instanceof RawMaterial m) return m.getName() + " ["+m.getCode()+"]";
                if (o instanceof Product p) return p.getName() + " ["+p.getProductCode()+"]";
                return "";
            }
            public Object fromString(String s) { return null; }
        });

        itemTypeCB.setOnAction(e -> {
            itemCB.getItems().clear();
            if ("RAW_MATERIAL".equals(itemTypeCB.getValue()))
                itemCB.getItems().addAll(allMaterials);
            else
                itemCB.getItems().addAll(allProducts);
        });
        itemCB.getItems().addAll(allMaterials);

        TextField qtyF   = new TextField("0");
        TextArea notesF  = new TextArea();
        notesF.setPrefRowCount(2);
        notesF.setPromptText("Reason for adjustment...");

        String[] labels = {"Type *","Item Type *","Item *","Quantity *","Notes *"};
        javafx.scene.Node[] fields = {adjTypeCB, itemTypeCB, itemCB, qtyF, notesF};
        for (int i = 0; i < labels.length; i++) {
            Label l = new Label(labels[i]); l.setStyle("-fx-text-fill:#9199b0;-fx-font-size:12px;");
            grid.add(l, 0, i); grid.add(fields[i], 1, i);
            GridPane.setHgrow(fields[i], Priority.ALWAYS);
        }
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            if (itemCB.getValue() == null || qtyF.getText().isBlank() || notesF.getText().isBlank()) {
                AlertUtil.showWarning("Validation","All fields are required."); return null;
            }
            BigDecimal qty = NumberUtil.parseBigDecimal(qtyF.getText());
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                AlertUtil.showWarning("Validation","Quantity must be greater than zero."); return null;
            }
            int itemId; String iType = itemTypeCB.getValue();
            if ("RAW_MATERIAL".equals(iType)) itemId = ((RawMaterial)itemCB.getValue()).getId();
            else itemId = ((Product)itemCB.getValue()).getId();

            InventoryTransaction tx = new InventoryTransaction(
                adjTypeCB.getValue(), iType, itemId, qty,
                SessionManager.getInstance().getCurrentUserId(), notesF.getText());
            return tx;
        });

        Optional<InventoryTransaction> result = dialog.showAndWait();
        result.ifPresent(tx -> {
            try {
                // Update stock
                boolean isIn = tx.getTransactionType().endsWith("IN");
                boolean isDamage = "DAMAGE".equals(tx.getTransactionType());
                boolean isDeduct = !isIn;

                if ("RAW_MATERIAL".equals(tx.getItemType())) {
                    rmDAO.findById(tx.getItemId()).ifPresent(m -> {
                        try {
                            BigDecimal newStock = isIn
                                ? m.getCurrentStock().add(tx.getQuantity())
                                : m.getCurrentStock().subtract(tx.getQuantity());
                            if (newStock.compareTo(BigDecimal.ZERO) < 0) {
                                AlertUtil.showError("Insufficient Stock",
                                    "Cannot reduce stock below zero. Available: " +
                                    NumberUtil.formatNumber(m.getCurrentStock()));
                                return;
                            }
                            rmDAO.updateStock(m.getId(), newStock);
                            txDAO.save(tx);
                            auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                                SessionManager.getInstance().getCurrentUsername(),
                                "ADJUSTMENT","INVENTORY","Stock adjustment: "+tx.getTransactionType()+
                                " | "+m.getName()+" | Qty: "+tx.getQuantity()));
                            Platform.runLater(this::loadData);
                        } catch (SQLException e) { AlertUtil.showDatabaseError(e.getMessage()); }
                    });
                } else {
                    productDAO.findById(tx.getItemId()).ifPresent(p -> {
                        try {
                            BigDecimal newStock = isIn
                                ? p.getCurrentStock().add(tx.getQuantity())
                                : p.getCurrentStock().subtract(tx.getQuantity());
                            if (newStock.compareTo(BigDecimal.ZERO) < 0) {
                                AlertUtil.showError("Insufficient Stock",
                                    "Cannot reduce stock below zero. Available: " +
                                    NumberUtil.formatNumber(p.getCurrentStock()));
                                return;
                            }
                            productDAO.updateStock(p.getId(), newStock);
                            txDAO.save(tx);
                            auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                                SessionManager.getInstance().getCurrentUsername(),
                                "ADJUSTMENT","INVENTORY","Stock adjustment: "+tx.getTransactionType()+
                                " | "+p.getName()+" | Qty: "+tx.getQuantity()));
                            Platform.runLater(this::loadData);
                        } catch (SQLException e) { AlertUtil.showDatabaseError(e.getMessage()); }
                    });
                }
            } catch (Exception e) { AlertUtil.showDatabaseError(e.getMessage()); }
        });
    }
}
