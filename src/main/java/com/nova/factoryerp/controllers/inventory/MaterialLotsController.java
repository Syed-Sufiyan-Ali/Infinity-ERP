package com.nova.factoryerp.controllers.inventory;

import com.nova.factoryerp.dao.impl.*;
import com.nova.factoryerp.dao.interfaces.*;
import com.nova.factoryerp.models.*;
import com.nova.factoryerp.utils.AlertUtil;
import com.nova.factoryerp.utils.DateUtil;
import com.nova.factoryerp.utils.NumberUtil;
import com.nova.factoryerp.utils.SessionManager;
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
import java.util.List;
import java.util.Optional;

public class MaterialLotsController {
    @FXML private TextField searchField;
    @FXML private ComboBox<RawMaterial> materialFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label countLabel;
    @FXML private TableView<MaterialLot> lotsTable;
    @FXML private TableColumn<MaterialLot, String> lotNumberCol;
    @FXML private TableColumn<MaterialLot, String> materialCol;
    @FXML private TableColumn<MaterialLot, String> supplierCol;
    @FXML private TableColumn<MaterialLot, String> originalQtyCol;
    @FXML private TableColumn<MaterialLot, String> remainingQtyCol;
    @FXML private TableColumn<MaterialLot, String> receivedDateCol;
    @FXML private TableColumn<MaterialLot, String> expiryDateCol;
    @FXML private TableColumn<MaterialLot, String> priceCol;
    @FXML private TableColumn<MaterialLot, String> statusCol;
    @FXML private TableColumn<MaterialLot, Void> actionsCol;

    private final MaterialLotDAO lotDAO = new MaterialLotDAOImpl();
    private final RawMaterialDAO rmDAO = new RawMaterialDAOImpl();
    private final SupplierDAO supplierDAO = new SupplierDAOImpl();
    private final InventoryTransactionDAO txDAO = new InventoryTransactionDAOImpl();
    private final AuditLogDAO auditDAO = new AuditLogDAOImpl();
    private final ObservableList<MaterialLot> data = FXCollections.observableArrayList();
    private List<RawMaterial> allMaterials;
    private List<Supplier> allSuppliers;

    @FXML
    public void initialize() {
        lotNumberCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLotNumber()));
        materialCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getMaterialName() + " [" + c.getValue().getMaterialCode() + "]"));
        supplierCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getSupplierName() != null ? c.getValue().getSupplierName() : "-"));
        originalQtyCol.setCellValueFactory(c -> new SimpleStringProperty(
            NumberUtil.formatNumber(c.getValue().getOriginalQty())));
        remainingQtyCol.setCellValueFactory(c -> new SimpleStringProperty(
            NumberUtil.formatNumber(c.getValue().getRemainingQty())));
        receivedDateCol.setCellValueFactory(c -> new SimpleStringProperty(
            DateUtil.format(c.getValue().getReceivedDate())));
        expiryDateCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getExpiryDate() != null ? DateUtil.format(c.getValue().getExpiryDate()) : "-"));
        priceCol.setCellValueFactory(c -> new SimpleStringProperty(
            NumberUtil.formatCurrency(c.getValue().getPurchasePrice())));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        actionsCol.setCellFactory(col -> new TableCell<>() {
            final Button viewBtn = new Button("View");
            { viewBtn.getStyleClass().add("btn-secondary");
              viewBtn.setStyle("-fx-padding:4 10 4 10;-fx-font-size:11px;");
              viewBtn.setOnAction(e -> showDetails(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });

        statusFilter.setItems(FXCollections.observableArrayList("","AVAILABLE","PARTIALLY_USED","EXHAUSTED","QUARANTINE","REJECTED"));
        materialFilter.setConverter(new StringConverter<>() {
            public String toString(RawMaterial m) { return m == null ? "" : m.getName() + " ["+m.getCode()+"]"; }
            public RawMaterial fromString(String s) { return null; }
        });

        lotsTable.setItems(data);
        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                allMaterials = rmDAO.findAll();
                allSuppliers = supplierDAO.findAll();
                Integer matId = materialFilter.getValue() != null ? materialFilter.getValue().getId() : null;
                String kw = searchField.getText();
                String st = statusFilter.getValue();
                List<MaterialLot> list = lotDAO.search(kw, matId, st);
                Platform.runLater(() -> {
                    materialFilter.setItems(FXCollections.observableArrayList(allMaterials));
                    data.setAll(list);
                    countLabel.setText(list.size() + " lots");
                });
            } catch (SQLException e) { Platform.runLater(() -> AlertUtil.showDatabaseError(e.getMessage())); }
        });
        t.setDaemon(true); t.start();
    }

    @FXML public void handleRefresh() { loadData(); }
    @FXML public void handleSearch() { loadData(); }

    @FXML
    public void handleAdd() {
        // Receive a new lot — also updates raw material stock
        Dialog<MaterialLot> dialog = new Dialog<>();
        dialog.setTitle("Receive Material Lot");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/nova-erp.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-background-color:#1a1e2a;");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField lotNumF  = new TextField("LOT-" + java.time.Year.now().getValue() + "-");
        ComboBox<RawMaterial> matCB = new ComboBox<>(FXCollections.observableArrayList(allMaterials));
        matCB.setConverter(new StringConverter<>() {
            public String toString(RawMaterial m) { return m == null ? "" : m.getName() + " ["+m.getCode()+"]"; }
            public RawMaterial fromString(String s) { return null; }
        });
        ComboBox<Supplier> supCB = new ComboBox<>(FXCollections.observableArrayList(allSuppliers));
        supCB.setConverter(new StringConverter<>() {
            public String toString(Supplier s) { return s == null ? "" : s.getName(); }
            public Supplier fromString(String s) { return null; }
        });
        TextField qtyF     = new TextField("0");
        TextField priceF   = new TextField("0");
        DatePicker recDate = new DatePicker(LocalDate.now());
        DatePicker expDate = new DatePicker();
        TextArea notesF    = new TextArea();
        notesF.setPrefRowCount(2);

        String[] labels = {"Lot Number *","Material *","Supplier","Quantity *","Purchase Price","Received Date","Expiry Date","Notes"};
        javafx.scene.Node[] fields = {lotNumF,matCB,supCB,qtyF,priceF,recDate,expDate,notesF};
        for (int i = 0; i < labels.length; i++) {
            Label l = new Label(labels[i]); l.setStyle("-fx-text-fill:#9199b0;-fx-font-size:12px;");
            grid.add(l, 0, i); grid.add(fields[i], 1, i);
            GridPane.setHgrow(fields[i], Priority.ALWAYS);
        }
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            if (lotNumF.getText().isBlank() || matCB.getValue() == null || qtyF.getText().isBlank()) {
                AlertUtil.showWarning("Validation","Lot Number, Material, and Quantity are required."); return null;
            }
            BigDecimal qty = NumberUtil.parseBigDecimal(qtyF.getText());
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                AlertUtil.showWarning("Validation","Quantity must be greater than zero."); return null;
            }
            MaterialLot lot = new MaterialLot();
            lot.setLotNumber(lotNumF.getText().trim());
            lot.setMaterialId(matCB.getValue().getId());
            lot.setSupplierId(supCB.getValue() != null ? supCB.getValue().getId() : 0);
            lot.setOriginalQty(qty); lot.setRemainingQty(qty);
            lot.setReceivedDate(recDate.getValue());
            lot.setExpiryDate(expDate.getValue());
            lot.setPurchasePrice(NumberUtil.parseBigDecimal(priceF.getText()));
            lot.setNotes(notesF.getText()); lot.setStatus("AVAILABLE");
            return lot;
        });

        Optional<MaterialLot> result = dialog.showAndWait();
        result.ifPresent(lot -> {
            try {
                // Save lot
                lotDAO.save(lot);
                // Update raw material stock
                Optional<RawMaterial> rm = rmDAO.findById(lot.getMaterialId());
                rm.ifPresent(m -> {
                    try {
                        BigDecimal newStock = m.getCurrentStock().add(lot.getOriginalQty());
                        rmDAO.updateStock(m.getId(), newStock);
                        // Record inventory transaction
                        InventoryTransaction tx = new InventoryTransaction(
                            "PURCHASE", "RAW_MATERIAL", m.getId(),
                            lot.getOriginalQty(), SessionManager.getInstance().getCurrentUserId(),
                            "Received lot: " + lot.getLotNumber());
                        tx.setLotId(lot.getId());
                        tx.setUnitPrice(lot.getPurchasePrice());
                        tx.setReferenceType("MATERIAL_LOT");
                        tx.setReferenceId((long) lot.getId());
                        txDAO.save(tx);
                    } catch (SQLException e) { AlertUtil.showDatabaseError(e.getMessage()); }
                });
                auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                    SessionManager.getInstance().getCurrentUsername(),
                    "CREATE","INVENTORY","Received material lot: " + lot.getLotNumber()));
                loadData();
            } catch (SQLException e) { AlertUtil.showDatabaseError(e.getMessage()); }
        });
    }

    private void showDetails(MaterialLot lot) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Lot Details");
        info.setHeaderText(lot.getLotNumber());
        info.setContentText(
            "Material:    " + lot.getMaterialName() + "\n" +
            "Supplier:    " + (lot.getSupplierName() != null ? lot.getSupplierName() : "-") + "\n" +
            "Original:    " + NumberUtil.formatNumber(lot.getOriginalQty()) + "\n" +
            "Remaining:   " + NumberUtil.formatNumber(lot.getRemainingQty()) + "\n" +
            "Received:    " + DateUtil.format(lot.getReceivedDate()) + "\n" +
            "Expiry:      " + (lot.getExpiryDate() != null ? DateUtil.format(lot.getExpiryDate()) : "-") + "\n" +
            "Price/Unit:  " + NumberUtil.formatCurrency(lot.getPurchasePrice()) + "\n" +
            "Status:      " + lot.getStatus() + "\n" +
            "Notes:       " + (lot.getNotes() != null ? lot.getNotes() : "-")
        );
        info.showAndWait();
    }
}
