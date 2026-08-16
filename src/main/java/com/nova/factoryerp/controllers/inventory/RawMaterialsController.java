package com.nova.factoryerp.controllers.inventory;
import com.nova.factoryerp.dao.impl.MaterialLotDAOImpl;
import com.nova.factoryerp.dao.interfaces.MaterialLotDAO;
import com.nova.factoryerp.models.MaterialLot;
import com.nova.factoryerp.dao.impl.AuditLogDAOImpl;
import com.nova.factoryerp.dao.impl.InventoryTransactionDAOImpl;
import com.nova.factoryerp.dao.impl.RawMaterialDAOImpl;
import com.nova.factoryerp.utils.LotNumberUtil;
import com.nova.factoryerp.dao.impl.SupplierDAOImpl;
import com.nova.factoryerp.dao.interfaces.AuditLogDAO;
import com.nova.factoryerp.dao.interfaces.InventoryTransactionDAO;
import com.nova.factoryerp.dao.interfaces.RawMaterialDAO;
import com.nova.factoryerp.dao.interfaces.SupplierDAO;
import com.nova.factoryerp.models.AuditLog;
import com.nova.factoryerp.models.RawMaterial;
import com.nova.factoryerp.models.Supplier;
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
private final InventoryTransactionDAO txDAO = new InventoryTransactionDAOImpl();
private final AuditLogDAO auditDAO = new AuditLogDAOImpl();
private final MaterialLotDAO lotDAO = new MaterialLotDAOImpl();
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
       // Actions column
actionsCol.setCellFactory(col -> new TableCell<>() {

    final Button editBtn = new Button("Edit");
    final Button delBtn = new Button("Delete");
    final Button detailsBtn = new Button("View Details");

    {
        editBtn.getStyleClass().add("btn-secondary");
        delBtn.getStyleClass().add("btn-danger");
        detailsBtn.getStyleClass().add("btn-secondary");

        editBtn.setStyle("-fx-padding:4 10 4 10; -fx-font-size:11px;");
        delBtn.setStyle("-fx-padding:4 10 4 10; -fx-font-size:11px;");
        detailsBtn.setStyle("-fx-padding:4 10 4 10; -fx-font-size:11px;");

        editBtn.setOnAction(e -> {
            RawMaterial material = getTableView().getItems().get(getIndex());
            showEditDialog(material);
        });

        delBtn.setOnAction(e -> {
            RawMaterial material = getTableView().getItems().get(getIndex());
            handleDelete(material);
        });

        detailsBtn.setOnAction(e -> {
            RawMaterial material = getTableView().getItems().get(getIndex());
            showMaterialDetails(material);
        });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            HBox box = new HBox(6, editBtn, delBtn, detailsBtn);
            box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            setGraphic(box);
        }
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
       result.ifPresent(material -> {
    try {
        if (isNew) {
            // Check duplicate code
            if (rmDAO.findByCode(material.getCode()).isPresent()) {
                AlertUtil.showError(
                    "Duplicate",
                    "Material code already exists: " + material.getCode()
                );
                return;
            }

            rmDAO.save(material);

            final String materialName = material.getName();

            auditDAO.log(new AuditLog(
                SessionManager.getInstance().getCurrentUserId(),
                SessionManager.getInstance().getCurrentUsername(),
                "CREATE",
                "INVENTORY",
                "Created raw material: " + materialName
            ));

        } else {
            rmDAO.update(material);

            final String materialName = material.getName();

            auditDAO.log(new AuditLog(
                SessionManager.getInstance().getCurrentUserId(),
                SessionManager.getInstance().getCurrentUsername(),
                "UPDATE",
                "INVENTORY",
                "Updated raw material: " + materialName
            ));
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
  private void showMaterialDetails(RawMaterial material) {

    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Raw Material Details - " + material.getName());
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

    dialog.getDialogPane().getStylesheets().add(
        getClass().getResource("/css/nova-erp.css").toExternalForm()
    );

    dialog.getDialogPane().setStyle(
        "-fx-background-color:#1a1e2a;"
    );

    VBox root = new VBox(15);
    root.setPadding(new Insets(20));
    root.setPrefWidth(1050);
    root.setPrefHeight(650);

    // =========================================================
    // HEADER
    // =========================================================

    Label title = new Label(material.getName());

    title.setStyle(
        "-fx-text-fill:white;" +
        "-fx-font-size:20px;" +
        "-fx-font-weight:bold;"
    );

    Label code = new Label(
        "Code: " + material.getCode() +
        "    |    Category: " +
        (material.getCategory() != null ? material.getCategory() : "-") +
        "    |    Unit: " +
        (material.getUnit() != null ? material.getUnit() : "-")
    );

    code.setStyle(
        "-fx-text-fill:#9199b0;" +
        "-fx-font-size:13px;"
    );

    // =========================================================
    // LOT HEADER + ADD BUTTON
    // =========================================================

    Label lotsTitle = new Label("Material Lots");

    lotsTitle.setStyle(
        "-fx-text-fill:white;" +
        "-fx-font-size:16px;" +
        "-fx-font-weight:bold;"
    );

    Button addLotBtn = new Button("+ Add Lot");

    addLotBtn.getStyleClass().add("btn-primary");

    addLotBtn.setStyle(
        "-fx-padding:7 14 7 14;" +
        "-fx-font-size:12px;"
    );

    Region spacer = new Region();

    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox lotsHeader = new HBox(
        10,
        lotsTitle,
        spacer,
        addLotBtn
    );

    lotsHeader.setAlignment(
        javafx.geometry.Pos.CENTER_LEFT
    );

    // =========================================================
    // KPI AREA
    // =========================================================

    HBox kpiRow = new HBox(12);

    Label loadingKpi = new Label("Loading lot information...");

    loadingKpi.setStyle(
        "-fx-text-fill:#9199b0;" +
        "-fx-font-size:12px;"
    );

    kpiRow.getChildren().add(loadingKpi);

    // =========================================================
    // LOT TABLE
    // =========================================================

    TableView<MaterialLot> lotsTable =
        new TableView<>();

    lotsTable.setPlaceholder(
        new Label("No material lots received yet for this material.")
    );

    lotsTable.setColumnResizePolicy(
        TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
    );

    VBox.setVgrow(lotsTable, Priority.ALWAYS);

    // ---------------------------------------------------------
    // LOT NUMBER
    // ---------------------------------------------------------

    TableColumn<MaterialLot, String> lotNumberCol =
        new TableColumn<>("Lot Number");

    lotNumberCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            cell.getValue().getLotNumber()
        )
    );

    // ---------------------------------------------------------
    // SUPPLIER
    // ---------------------------------------------------------

    TableColumn<MaterialLot, String> supplierCol =
        new TableColumn<>("Supplier");

    supplierCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            cell.getValue().getSupplierName() != null
                ? cell.getValue().getSupplierName()
                : "-"
        )
    );

    // ---------------------------------------------------------
    // RECEIVED
    // ---------------------------------------------------------

    TableColumn<MaterialLot, String> receivedCol =
        new TableColumn<>("Received");

    receivedCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            NumberUtil.formatNumber(
                cell.getValue().getOriginalQty()
            )
        )
    );

    // ---------------------------------------------------------
    // CONSUMED
    // ---------------------------------------------------------

    TableColumn<MaterialLot, String> consumedCol =
        new TableColumn<>("Consumed");

    consumedCol.setCellValueFactory(cell -> {

        BigDecimal original =
            cell.getValue().getOriginalQty() != null
                ? cell.getValue().getOriginalQty()
                : BigDecimal.ZERO;

        BigDecimal remaining =
            cell.getValue().getRemainingQty() != null
                ? cell.getValue().getRemainingQty()
                : BigDecimal.ZERO;

        BigDecimal consumed =
            original.subtract(remaining);

        if (consumed.compareTo(BigDecimal.ZERO) < 0) {
            consumed = BigDecimal.ZERO;
        }

        return new SimpleStringProperty(
            NumberUtil.formatNumber(consumed)
        );
    });

    // ---------------------------------------------------------
    // REMAINING
    // ---------------------------------------------------------

    TableColumn<MaterialLot, String> remainingCol =
        new TableColumn<>("Remaining");

    remainingCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            NumberUtil.formatNumber(
                cell.getValue().getRemainingQty()
            )
        )
    );

    // ---------------------------------------------------------
    // UNIT COST
    // ---------------------------------------------------------

    TableColumn<MaterialLot, String> costCol =
        new TableColumn<>("Unit Cost");

    costCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            NumberUtil.formatCurrency(
                cell.getValue().getPurchasePrice()
            )
        )
    );

    // ---------------------------------------------------------
    // RECEIVED DATE
    // ---------------------------------------------------------

    TableColumn<MaterialLot, String> dateCol =
        new TableColumn<>("Received Date");

    dateCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            cell.getValue().getReceivedDate() != null
                ? cell.getValue().getReceivedDate().toString()
                : "-"
        )
    );

    // ---------------------------------------------------------
    // STATUS
    // ---------------------------------------------------------

    TableColumn<MaterialLot, String> statusCol =
        new TableColumn<>("Status");

    statusCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            cell.getValue().getStatus() != null
                ? cell.getValue().getStatus()
                : "-"
        )
    );

    // ---------------------------------------------------------
    // ACTIONS
    // ---------------------------------------------------------

    TableColumn<MaterialLot, Void> actionsCol =
        new TableColumn<>("Actions");

    actionsCol.setCellFactory(col ->
        new TableCell<>() {

            private final Button viewBtn =
                new Button("View");

            private final Button editBtn =
                new Button("Edit");

            private final HBox box =
                new HBox(5, viewBtn, editBtn);

            {
                viewBtn.setStyle(
                    "-fx-padding:4 8 4 8;" +
                    "-fx-font-size:11px;"
                );

                editBtn.setStyle(
                    "-fx-padding:4 8 4 8;" +
                    "-fx-font-size:11px;"
                );

                viewBtn.setOnAction(event -> {

                    if (getIndex() < 0 ||
                        getIndex() >= getTableView().getItems().size()) {
                        return;
                    }

                    MaterialLot lot =
                        getTableView()
                            .getItems()
                            .get(getIndex());

                    showLotDetailsFromRmView(
                        lot,
                        dialog.getDialogPane()
                            .getScene()
                            .getWindow()
                    );
                });

                editBtn.setOnAction(event -> {

                    if (getIndex() < 0 ||
                        getIndex() >= getTableView().getItems().size()) {
                        return;
                    }

                    MaterialLot lot =
                        getTableView()
                            .getItems()
                            .get(getIndex());

                    showEditLotDialog(
                        material,
                        lot,
                        lotsTable,
                        kpiRow
                    );
                });
            }

            @Override
            protected void updateItem(
                    Void item,
                    boolean empty) {

                super.updateItem(item, empty);

                setGraphic(
                    empty ? null : box
                );
            }
        }
    );

    lotsTable.getColumns().addAll(
        lotNumberCol,
        supplierCol,
        receivedCol,
        consumedCol,
        remainingCol,
        costCol,
        dateCol,
        statusCol,
        actionsCol
    );

    // =========================================================
    // LOAD LOTS
    // =========================================================

    Runnable loadLots = () -> {

        Thread thread = new Thread(() -> {

            try {

                List<MaterialLot> lots =
                    lotDAO.findByMaterial(material.getId());

                Platform.runLater(() -> {

                    lotsTable.setItems(
                        FXCollections.observableArrayList(lots)
                    );

                    updateLotKpis(
                        kpiRow,
                        lots,
                        material.getUnit()
                    );
                });

            } catch (SQLException e) {

                Platform.runLater(() -> {

                    lotsTable.setItems(
                        FXCollections.observableArrayList()
                    );

                    kpiRow.getChildren().setAll(
                        createKpiError(
                            "Unable to load lots: " +
                            e.getMessage()
                        )
                    );
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    };

    // =========================================================
    // ADD LOT
    // =========================================================

    addLotBtn.setOnAction(event -> {

        showEditLotDialog(
            material,
            null,
            lotsTable,
            kpiRow
        );
    });

    // =========================================================
    // BUILD
    // =========================================================

    root.getChildren().addAll(
        title,
        code,
        new Separator(),
        lotsHeader,
        kpiRow,
        lotsTable
    );

    dialog.getDialogPane().setContent(root);

    loadLots.run();

    dialog.showAndWait();
}
private void updateLotKpis(
        HBox kpiRow,
        List<MaterialLot> lots,
        String unit) {

    BigDecimal received = lots.stream()
        .map(MaterialLot::getOriginalQty)
        .filter(q -> q != null)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal remaining = lots.stream()
        .map(MaterialLot::getRemainingQty)
        .filter(q -> q != null)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal consumed =
        received.subtract(remaining);

    if (consumed.compareTo(BigDecimal.ZERO) < 0) {
        consumed = BigDecimal.ZERO;
    }

    String u = unit != null ? unit : "";

    kpiRow.getChildren().setAll(

        buildKpiMini(
            "Lots",
            String.valueOf(lots.size()),
            "#4e8ef7"
        ),

        buildKpiMini(
            "Received",
            NumberUtil.formatNumber(received) + " " + u,
            "#4e8ef7"
        ),

        buildKpiMini(
            "Consumed",
            NumberUtil.formatNumber(consumed) + " " + u,
            "#f39c12"
        ),

        buildKpiMini(
            "Remaining",
            NumberUtil.formatNumber(remaining) + " " + u,
            "#2ecc71"
        )
    );
}
private VBox buildKpiMini(
        String title,
        String value,
        String accentColor) {

    VBox box = new VBox(3);

    box.setPadding(
        new Insets(10, 16, 10, 16)
    );

    box.setMinWidth(150);

    box.setStyle(
        "-fx-background-color:#202533;" +
        "-fx-background-radius:6;" +
        "-fx-border-color:" + accentColor + ";" +
        "-fx-border-width:0 0 0 3;" +
        "-fx-border-radius:6;"
    );

    Label titleLabel =
        new Label(title);

    titleLabel.setStyle(
        "-fx-text-fill:#9199b0;" +
        "-fx-font-size:11px;"
    );

    Label valueLabel =
        new Label(value);

    valueLabel.setStyle(
        "-fx-text-fill:white;" +
        "-fx-font-size:16px;" +
        "-fx-font-weight:bold;"
    );

    box.getChildren().addAll(
        titleLabel,
        valueLabel
    );

    return box;
}
private void showLotDetailsFromRmView(
        MaterialLot lot,
        javafx.stage.Window parentWindow) {

    Dialog<Void> dialog = new Dialog<>();

    dialog.setTitle(
        "Material Lot Details - " +
        lot.getLotNumber()
    );

    dialog.getDialogPane()
        .getButtonTypes()
        .add(ButtonType.CLOSE);

    dialog.getDialogPane()
        .getStylesheets()
        .add(
            getClass()
                .getResource("/css/nova-erp.css")
                .toExternalForm()
        );

    dialog.getDialogPane().setStyle(
        "-fx-background-color:#1a1e2a;"
    );

    VBox root = new VBox(12);

    root.setPadding(
        new Insets(20)
    );

    root.setPrefWidth(500);

    Label title =
        new Label(lot.getLotNumber());

    title.setStyle(
        "-fx-text-fill:white;" +
        "-fx-font-size:20px;" +
        "-fx-font-weight:bold;"
    );

    Label material =
        new Label(
            "Material: " +
            (lot.getMaterialName() != null
                ? lot.getMaterialName()
                : "-")
        );

    Label supplier =
        new Label(
            "Supplier: " +
            (lot.getSupplierName() != null
                ? lot.getSupplierName()
                : "-")
        );

    Label supplierLot =
        new Label(
            "Supplier Lot Number: " +
            (lot.getSupplierLotNumber() != null
                ? lot.getSupplierLotNumber()
                : "-")
        );

    BigDecimal received =
        lot.getOriginalQty() != null
            ? lot.getOriginalQty()
            : BigDecimal.ZERO;

    BigDecimal remaining =
        lot.getRemainingQty() != null
            ? lot.getRemainingQty()
            : BigDecimal.ZERO;

    BigDecimal consumed =
        received.subtract(remaining);

    if (consumed.compareTo(BigDecimal.ZERO) < 0) {
        consumed = BigDecimal.ZERO;
    }

    Label receivedLabel =
        new Label(
            "Quantity Received: " +
            NumberUtil.formatNumber(received)
        );

    Label consumedLabel =
        new Label(
            "Quantity Consumed: " +
            NumberUtil.formatNumber(consumed)
        );

    Label remainingLabel =
        new Label(
            "Quantity Remaining: " +
            NumberUtil.formatNumber(remaining)
        );

    Label costLabel =
        new Label(
            "Unit Cost: " +
            NumberUtil.formatCurrency(
                lot.getPurchasePrice()
            )
        );

    Label receivedDate =
        new Label(
            "Received Date: " +
            (lot.getReceivedDate() != null
                ? lot.getReceivedDate().toString()
                : "-")
        );

    Label manufacturingDate =
        new Label(
            "Manufacturing Date: " +
            (lot.getManufacturingDate() != null
                ? lot.getManufacturingDate().toString()
                : "-")
        );

    Label expiryDate =
        new Label(
            "Expiry Date: " +
            (lot.getExpiryDate() != null
                ? lot.getExpiryDate().toString()
                : "-")
        );

    Label status =
        new Label(
            "Status: " +
            (lot.getStatus() != null
                ? lot.getStatus()
                : "-")
        );

    Label notes =
        new Label(
            "Notes: " +
            (lot.getNotes() != null &&
             !lot.getNotes().isBlank()
                ? lot.getNotes()
                : "-")
        );

    for (Label label : new Label[]{
        material,
        supplier,
        supplierLot,
        receivedLabel,
        consumedLabel,
        remainingLabel,
        costLabel,
        receivedDate,
        manufacturingDate,
        expiryDate,
        status,
        notes
    }) {

        label.setStyle(
            "-fx-text-fill:#c8cede;" +
            "-fx-font-size:13px;"
        );
    }

    root.getChildren().addAll(
        title,
        new Separator(),
        material,
        supplier,
        supplierLot,
        new Separator(),
        receivedLabel,
        consumedLabel,
        remainingLabel,
        costLabel,
        new Separator(),
        receivedDate,
        manufacturingDate,
        expiryDate,
        status,
        notes
    );

    dialog.getDialogPane()
        .setContent(root);

    dialog.showAndWait();
}

private Label createKpiError(String message) {

    Label label = new Label(message);

    label.setStyle(
        "-fx-text-fill:#e74c3c;" +
        "-fx-font-size:12px;"
    );

    return label;
}


private void showEditLotDialog(
        RawMaterial material,
        MaterialLot existing,
        TableView<MaterialLot> lotsTable,
        HBox kpiRow) {

    boolean isNew = existing == null;

    Dialog<MaterialLot> dialog =
        new Dialog<>();

    dialog.setTitle(
        isNew
            ? "Add Material Lot - " + material.getName()
            : "Edit Material Lot - " + existing.getLotNumber()
    );

    dialog.getDialogPane()
        .getButtonTypes()
        .addAll(
            ButtonType.OK,
            ButtonType.CANCEL
        );

    dialog.getDialogPane().getStylesheets().add(
        getClass()
            .getResource("/css/nova-erp.css")
            .toExternalForm()
    );

    dialog.getDialogPane().setStyle(
        "-fx-background-color:#1a1e2a;"
    );

    GridPane grid = new GridPane();

    grid.setHgap(12);
    grid.setVgap(10);
    grid.setPadding(new Insets(20));

    TextField lotNumberF = new TextField();

lotNumberF.setEditable(false);
lotNumberF.setDisable(true);
lotNumberF.setPromptText("Generated automatically");
try {
    int nextSequence = lotDAO.getNextSequence(
        material.getCode(),
        java.time.Year.now().getValue()
    );

    lotNumberF.setText(
        LotNumberUtil.generate(
            material.getCode(),
            nextSequence
        )
    );

} catch (SQLException e) {
    AlertUtil.showDatabaseError(
        "Unable to generate the next lot number: " + e.getMessage()
    );

    lotNumberF.setText("Unable to generate");
}

    TextField supplierLotF =
        new TextField(
            isNew
                ? ""
                : existing.getSupplierLotNumber()
        );

    TextField quantityF =
        new TextField(
            isNew
                ? "0"
                : existing.getOriginalQty().toPlainString()
        );

    TextField remainingF =
        new TextField(
            isNew
                ? "0"
                : existing.getRemainingQty().toPlainString()
        );

    TextField priceF =
        new TextField(
            isNew
                ? material.getPurchasePrice().toPlainString()
                : existing.getPurchasePrice().toPlainString()
        );

    DatePicker receivedDate =
        new DatePicker(
            isNew
                ? java.time.LocalDate.now()
                : existing.getReceivedDate()
        );

    DatePicker manufacturingDate =
        new DatePicker(
            isNew
                ? null
                : existing.getManufacturingDate()
        );

    DatePicker expiryDate =
        new DatePicker(
            isNew
                ? null
                : existing.getExpiryDate()
        );

    TextArea notesF =
        new TextArea(
            isNew
                ? ""
                : existing.getNotes()
        );

    notesF.setPrefRowCount(3);

    ComboBox<Supplier> supplierCB =
        new ComboBox<>();

    if (allSuppliers != null) {

        supplierCB.setItems(
            FXCollections.observableArrayList(
                allSuppliers
            )
        );
    }

    supplierCB.setConverter(
        new StringConverter<>() {

            @Override
            public String toString(Supplier s) {
                return s == null ? "" : s.getName();
            }

            @Override
            public Supplier fromString(String s) {
                return null;
            }
        }
    );

    if (!isNew &&
        existing.getSupplierId() > 0 &&
        allSuppliers != null) {

        allSuppliers.stream()
            .filter(
                s -> s.getId() ==
                     existing.getSupplierId()
            )
            .findFirst()
            .ifPresent(
                supplierCB::setValue
            );
    }

    ComboBox<String> statusCB =
        new ComboBox<>(
            FXCollections.observableArrayList(
                "AVAILABLE",
                "PARTIALLY_USED",
                "CONSUMED",
                "EXPIRED",
                "BLOCKED"
            )
        );

    statusCB.setValue(
        isNew
            ? "AVAILABLE"
            : existing.getStatus()
    );

    String[] labels = {
        "Lot Number *",
        "Supplier Lot Number",
        "Quantity Received *",
        "Remaining Quantity *",
        "Unit Cost *",
        "Received Date *",
        "Manufacturing Date",
        "Expiry Date",
        "Supplier",
        "Status",
        "Notes"
    };

    javafx.scene.Node[] fields = {
        lotNumberF,
        supplierLotF,
        quantityF,
        remainingF,
        priceF,
        receivedDate,
        manufacturingDate,
        expiryDate,
        supplierCB,
        statusCB,
        notesF
    };

    for (int i = 0; i < labels.length; i++) {

        Label label =
            new Label(labels[i]);

        label.setStyle(
            "-fx-text-fill:#9199b0;" +
            "-fx-font-size:12px;"
        );

        grid.add(label, 0, i);
        grid.add(fields[i], 1, i);

        GridPane.setHgrow(
            fields[i],
            Priority.ALWAYS
        );
    }

    dialog.getDialogPane()
        .setContent(grid);

    dialog.setResultConverter(button -> {

        if (button != ButtonType.OK) {
            return null;
        }

        if (lotNumberF.getText().isBlank()) {

            AlertUtil.showWarning(
                "Validation",
                "Lot Number is required."
            );

            return null;
        }

        BigDecimal originalQty;

        BigDecimal remainingQty;

        BigDecimal unitCost;

        try {

            originalQty =
                NumberUtil.parseBigDecimal(
                    quantityF.getText()
                );

            remainingQty =
                NumberUtil.parseBigDecimal(
                    remainingF.getText()
                );

            unitCost =
                NumberUtil.parseBigDecimal(
                    priceF.getText()
                );

        } catch (Exception e) {

            AlertUtil.showWarning(
                "Validation",
                "Quantity and Unit Cost must be valid numbers."
            );

            return null;
        }

        if (originalQty.compareTo(BigDecimal.ZERO) <= 0) {

            AlertUtil.showWarning(
                "Validation",
                "Quantity Received must be greater than zero."
            );

            return null;
        }

        if (remainingQty.compareTo(BigDecimal.ZERO) < 0 ||
            remainingQty.compareTo(originalQty) > 0) {

            AlertUtil.showWarning(
                "Validation",
                "Remaining Quantity must be between 0 and Quantity Received."
            );

            return null;
        }

        MaterialLot lot =
            isNew
                ? new MaterialLot()
                : existing;

        lot.setLotNumber(
            lotNumberF.getText().trim()
        );

        // THIS is the important relationship:
        // the lot always belongs to the material
        // whose View Details window is currently open.
        lot.setMaterialId(
            material.getId()
        );

        lot.setSupplierId(
            supplierCB.getValue() != null
                ? supplierCB.getValue().getId()
                : 0
        );

        lot.setSupplierLotNumber(
            supplierLotF.getText().trim()
        );

        lot.setOriginalQty(
            originalQty
        );

        lot.setRemainingQty(
            remainingQty
        );

        lot.setPurchasePrice(
            unitCost
        );

        lot.setReceivedDate(
            receivedDate.getValue()
        );

        lot.setManufacturingDate(
            manufacturingDate.getValue()
        );

        lot.setExpiryDate(
            expiryDate.getValue()
        );

        lot.setStatus(
            statusCB.getValue()
        );

        lot.setNotes(
            notesF.getText().trim()
        );

        return lot;
    });

    Optional<MaterialLot> result =
        dialog.showAndWait();

    result.ifPresent(lot -> {

        try {

            if (isNew) {

                lotDAO.save(lot);

                auditDAO.log(
                    new AuditLog(
                        SessionManager.getInstance()
                            .getCurrentUserId(),

                        SessionManager.getInstance()
                            .getCurrentUsername(),

                        "CREATE",

                        "INVENTORY",

                        "Created material lot: " +
                        lot.getLotNumber() +
                        " for raw material: " +
                        material.getName()
                    )
                );

            } else {

                lotDAO.update(lot);

                auditDAO.log(
                    new AuditLog(
                        SessionManager.getInstance()
                            .getCurrentUserId(),

                        SessionManager.getInstance()
                            .getCurrentUsername(),

                        "UPDATE",

                        "INVENTORY",

                        "Updated material lot: " +
                        lot.getLotNumber() +
                        " for raw material: " +
                        material.getName()
                    )
                );
            }

            // Refresh the table immediately.
            List<MaterialLot> refreshed =
                lotDAO.findByMaterial(
                    material.getId()
                );

            lotsTable.setItems(
                FXCollections.observableArrayList(
                    refreshed
                )
            );

            updateLotKpis(
                kpiRow,
                refreshed,
                material.getUnit()
            );

            // Refresh raw-material stock as well.
            loadData();

        } catch (SQLException e) {

            AlertUtil.showDatabaseError(
                e.getMessage()
            );
        }
    });
}
private void addDetailRow(
        GridPane grid,
        int row,
        String labelText,
        String valueText) {

    Label label = new Label(labelText);

    label.setStyle(
        "-fx-text-fill:#9199b0;" +
        "-fx-font-size:12px;"
    );

    Label value = new Label(
        valueText != null ? valueText : "-"
    );

    value.setStyle(
        "-fx-text-fill:#e8eaf0;" +
        "-fx-font-size:13px;" +
        "-fx-font-weight:bold;"
    );

    grid.add(label, 0, row);
    grid.add(value, 1, row);
}
private VBox buildLotSummaryCard(
        String title,
        String value) {

    VBox card = new VBox(4);

    card.setPadding(
        new Insets(10, 14, 10, 14)
    );

    card.setMinWidth(150);

    card.setStyle(
        "-fx-background-color:#202532;" +
        "-fx-background-radius:6;" +
        "-fx-border-color:#30384a;" +
        "-fx-border-radius:6;"
    );

    Label titleLabel = new Label(title);

    titleLabel.setStyle(
        "-fx-text-fill:#9199b0;" +
        "-fx-font-size:10px;" +
        "-fx-font-weight:bold;"
    );

    Label valueLabel = new Label(value);

    valueLabel.setStyle(
        "-fx-text-fill:white;" +
        "-fx-font-size:16px;" +
        "-fx-font-weight:bold;"
    );

    card.getChildren().addAll(
        titleLabel,
        valueLabel
    );

    return card;
}
private TableView<MaterialLot> buildLotsTable(
        List<MaterialLot> lots) {

    TableView<MaterialLot> table =
        new TableView<>(
            FXCollections.observableArrayList(lots)
        );

    table.setPrefHeight(300);

    table.setStyle(
        "-fx-background-color:#13161e;" +
        "-fx-border-color:#252b3d;"
    );

    // =========================================================
    // LOT NUMBER
    // =========================================================

    TableColumn<MaterialLot, String> lotNumberCol =
        new TableColumn<>("Lot Number");

    lotNumberCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            cell.getValue().getLotNumber()
        )
    );

    // =========================================================
    // SUPPLIER
    // =========================================================

    TableColumn<MaterialLot, String> supplierCol =
        new TableColumn<>("Supplier");

    supplierCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            cell.getValue().getSupplierName() != null
                ? cell.getValue().getSupplierName()
                : "-"
        )
    );

    // =========================================================
    // RECEIVED
    // =========================================================

    TableColumn<MaterialLot, String> receivedCol =
        new TableColumn<>("Received");

    receivedCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            NumberUtil.formatNumber(
                cell.getValue().getOriginalQty()
            )
        )
    );

    // =========================================================
    // CONSUMED
    // =========================================================

    TableColumn<MaterialLot, String> consumedCol =
        new TableColumn<>("Consumed");

    consumedCol.setCellValueFactory(cell -> {

        BigDecimal original =
            cell.getValue().getOriginalQty() != null
                ? cell.getValue().getOriginalQty()
                : BigDecimal.ZERO;

        BigDecimal remaining =
            cell.getValue().getRemainingQty() != null
                ? cell.getValue().getRemainingQty()
                : BigDecimal.ZERO;

        BigDecimal consumed =
            original.subtract(remaining);

        if (consumed.compareTo(BigDecimal.ZERO) < 0) {
            consumed = BigDecimal.ZERO;
        }

        return new SimpleStringProperty(
            NumberUtil.formatNumber(consumed)
        );
    });

    // =========================================================
    // REMAINING
    // =========================================================

    TableColumn<MaterialLot, String> remainingCol =
        new TableColumn<>("Remaining");

    remainingCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            NumberUtil.formatNumber(
                cell.getValue().getRemainingQty()
            )
        )
    );

    // =========================================================
    // UNIT COST
    // =========================================================

    TableColumn<MaterialLot, String> costCol =
        new TableColumn<>("Unit Cost");

    costCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            NumberUtil.formatCurrency(
                cell.getValue().getPurchasePrice()
            )
        )
    );

    // =========================================================
    // RECEIVED DATE
    // =========================================================

    TableColumn<MaterialLot, String> dateCol =
        new TableColumn<>("Received Date");

    dateCol.setCellValueFactory(cell -> {

        if (cell.getValue().getReceivedDate() == null) {
            return new SimpleStringProperty("-");
        }

        return new SimpleStringProperty(
            DateUtil.format(
                cell.getValue().getReceivedDate()
            )
        );
    });

    // =========================================================
    // STATUS
    // =========================================================

    TableColumn<MaterialLot, String> statusCol =
        new TableColumn<>("Status");

    statusCol.setCellValueFactory(cell ->
        new SimpleStringProperty(
            cell.getValue().getStatus() != null
                ? cell.getValue().getStatus()
                : "-"
        )
    );

    table.getColumns().addAll(
        lotNumberCol,
        supplierCol,
        receivedCol,
        consumedCol,
        remainingCol,
        costCol,
        dateCol,
        statusCol
    );

    table.setColumnResizePolicy(
        TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
    );

    return table;
}
}
