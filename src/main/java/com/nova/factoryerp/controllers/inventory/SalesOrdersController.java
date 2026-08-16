package com.nova.factoryerp.controllers.inventory;

import com.nova.factoryerp.dao.impl.AuditLogDAOImpl;
import com.nova.factoryerp.dao.impl.CustomerDAOImpl;
import com.nova.factoryerp.dao.impl.ProductDAOImpl;
import com.nova.factoryerp.dao.impl.SalesOrderDAOImpl;
import com.nova.factoryerp.dao.impl.SalesOrderItemDAOImpl;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import java.math.RoundingMode;
import java.net.URL;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import com.nova.factoryerp.dao.interfaces.AuditLogDAO;
import com.nova.factoryerp.dao.interfaces.CustomerDAO;
import com.nova.factoryerp.dao.interfaces.ProductDAO;
import com.nova.factoryerp.dao.interfaces.SalesOrderDAO;
import com.nova.factoryerp.dao.interfaces.SalesOrderItemDAO;

import com.nova.factoryerp.models.AuditLog;
import com.nova.factoryerp.models.Customer;
import com.nova.factoryerp.models.Product;
import com.nova.factoryerp.models.SalesOrder;
import com.nova.factoryerp.models.SalesOrderItem;

import com.nova.factoryerp.utils.AlertUtil;
import com.nova.factoryerp.utils.NumberUtil;
import com.nova.factoryerp.utils.SessionManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SalesOrdersController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label countLabel;

    @FXML private TableView<SalesOrder> ordersTable;

    @FXML private TableColumn<SalesOrder, String> orderNoCol;
    @FXML private TableColumn<SalesOrder, String> customerCol;
    @FXML private TableColumn<SalesOrder, String> dateCol;
    @FXML private TableColumn<SalesOrder, String> totalCol;
    @FXML private TableColumn<SalesOrder, String> paidCol;
    @FXML private TableColumn<SalesOrder, String> remainingCol;
    @FXML private TableColumn<SalesOrder, String> statusCol;
    @FXML private TableColumn<SalesOrder, Void> actionsCol;

    private final SalesOrderDAO orderDAO =
            new SalesOrderDAOImpl();

    private final SalesOrderItemDAO itemDAO =
            new SalesOrderItemDAOImpl();

    private final CustomerDAO customerDAO =
            new CustomerDAOImpl();

    private final ProductDAO productDAO =
            new ProductDAOImpl();

    private final AuditLogDAO auditDAO =
            new AuditLogDAOImpl();

    private final ObservableList<SalesOrder> data =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (ordersTable != null) {
        ordersTable.getStylesheets().add(
                getClass()
                        .getResource("/css/sales-orders.css")
                        .toExternalForm()
        );
    }
        orderNoCol.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getOrderNumber()
                )
        );

        customerCol.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getCustomerName()
                )
        );

        dateCol.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getOrderDate() != null
                                ? c.getValue().getOrderDate().toString()
                                : "-"
                )
        );

        totalCol.setCellValueFactory(
                c -> new SimpleStringProperty(
                        NumberUtil.formatCurrency(
                                c.getValue().getGrandTotal()
                        )
                )
        );

        paidCol.setCellValueFactory(
                c -> new SimpleStringProperty(
                        NumberUtil.formatCurrency(
                                c.getValue().getPaidAmount()
                        )
                )
        );

        remainingCol.setCellValueFactory(
                c -> new SimpleStringProperty(
                        NumberUtil.formatCurrency(
                                c.getValue().getRemaining()
                        )
                )
        );

        statusCol.setCellValueFactory(
                c -> new SimpleStringProperty(
                        c.getValue().getStatus()
                )
        );

        statusFilter.setItems(
                FXCollections.observableArrayList(
                        "",
                        "DRAFT",
                        "CONFIRMED",
                        "DELIVERED",
                        "CANCELLED",
                        "RETURNED"
                )
        );

        ordersTable.setItems(data);

        actionsCol.setCellFactory(col ->
                new TableCell<>() {

                    private final Button view =
                            new Button("View");

                    private final Button edit =
                            new Button("Edit");

                    private final Button cancel =
                            new Button("Cancel");

                    {
                        view.getStyleClass()
                                .add("btn-secondary");

                        edit.getStyleClass()
                                .add("btn-secondary");

                        cancel.getStyleClass()
                                .add("btn-danger");

                        view.setStyle(
                                "-fx-padding:4 10 4 10;" +
                                "-fx-font-size:11px;"
                        );

                        edit.setStyle(
                                "-fx-padding:4 10 4 10;" +
                                "-fx-font-size:11px;"
                        );

                        cancel.setStyle(
                                "-fx-padding:4 10 4 10;" +
                                "-fx-font-size:11px;"
                        );

                        view.setOnAction(e ->
                                showOrderDetails(
                                        getTableView()
                                                .getItems()
                                                .get(getIndex())
                                )
                        );

                        edit.setOnAction(e ->
                                showDialog(
                                        getTableView()
                                                .getItems()
                                                .get(getIndex())
                                )
                        );

                        cancel.setOnAction(e ->
                                handleCancel(
                                        getTableView()
                                                .getItems()
                                                .get(getIndex())
                                )
                        );
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty
                    ) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                            return;
                        }

                        SalesOrder order =
                                getTableView()
                                        .getItems()
                                        .get(getIndex());

                        cancel.setDisable(
                                "CANCELLED".equals(
                                        order.getStatus()
                                )
                        );

                        setGraphic(
                                new HBox(
                                        6,
                                        view,
                                        edit,
                                        cancel
                                )
                        );
                    }
                }
        );

        loadData();
         ordersTable.setColumnResizePolicy(
        TableView.UNCONSTRAINED_RESIZE_POLICY
);
    }

    private void loadData() {

        Thread thread = new Thread(() -> {

            try {

                List<SalesOrder> orders =
                        orderDAO.search(
                                searchField.getText(),
                                statusFilter.getValue()
                        );

                Platform.runLater(() -> {

                    data.setAll(orders);

                    countLabel.setText(
                            orders.size() +
                            " sales orders"
                    );
                });

            } catch (SQLException e) {

                Platform.runLater(() ->
                        AlertUtil.showDatabaseError(
                                e.getMessage()
                        )
                );
            }

        });

        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleRefresh() {
        loadData();
    }

    @FXML
    public void handleSearch() {
        loadData();
    }

    @FXML
    public void handleAdd() {
        showDialog(null);
    }

    private void showDialog(SalesOrder existing) {

    boolean isNew = existing == null;

    try {

        List<Customer> customers =
                customerDAO.search("", "ACTIVE");

        List<Product> products =
                productDAO.search("", null, "ACTIVE");

        Dialog<SalesOrder> dialog =
                new Dialog<>();

        dialog.setTitle(
                isNew
                        ? "Create Sales Order"
                        : "Edit Sales Order"
        );

        dialog.setResizable(true);
        dialog.setResizable(true);

DialogPane pane = dialog.getDialogPane();

pane.setPrefWidth(1050);
pane.setPrefHeight(720);

pane.setMinWidth(900);
pane.setMinHeight(600);

        /*
         * ---------------------------------------------------------
         * DIALOG CSS
         * ---------------------------------------------------------
         */

        URL css =
                getClass().getResource(
                        "/css/sales-order-dialog.css"
                );

        if (css != null) {
            pane.getStylesheets().add(
                    css.toExternalForm()
            );
        }
        ButtonType saveButtonType =
        new ButtonType(
                isNew ? "Create Order" : "Save Changes",
                ButtonBar.ButtonData.OK_DONE
        );

ButtonType cancelButtonType =
        new ButtonType(
                "Cancel",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

pane.getButtonTypes().addAll(
        saveButtonType,
        cancelButtonType
);
        pane.getStyleClass().add(
                "sales-order-dialog"
        );

        /*
         * ---------------------------------------------------------
         * HEADER
         * ---------------------------------------------------------
         */

        VBox header =
                new VBox(4);

        header.getStyleClass().add(
                "sales-dialog-header"
        );

        Label title =
                new Label(
                        isNew
                                ? "Create Sales Order"
                                : "Edit Sales Order"
                );

        title.getStyleClass().add(
                "sales-dialog-title"
        );

        Label subtitle =
                new Label(
                        isNew
                                ? "Create a new customer sales order"
                                : "Update the selected sales order"
                );

        subtitle.getStyleClass().add(
                "sales-dialog-subtitle"
        );

        header.getChildren().addAll(
                title,
                subtitle
        );

        /*
         * ---------------------------------------------------------
         * CUSTOMER / DATE
         * ---------------------------------------------------------
         */

        ComboBox<Customer> customerCB =
                new ComboBox<>(
                        FXCollections.observableArrayList(
                                customers
                        )
                );

        customerCB.setMaxWidth(
                Double.MAX_VALUE
        );

        customerCB.setPromptText(
                "Select customer..."
        );

        customerCB.setConverter(
                new StringConverter<>() {

                    @Override
                    public String toString(
                            Customer c
                    ) {
                        if (c == null) {
                            return "";
                        }

                        return c.getName()
                                + " ["
                                + c.getCustomerCode()
                                + "]";
                    }

                    @Override
                    public Customer fromString(
                            String s
                    ) {
                        return null;
                    }
                }
        );

        if (!isNew) {

            customers.stream()
                    .filter(c ->
                            c.getId()
                                    == existing
                                    .getCustomerId()
                    )
                    .findFirst()
                    .ifPresent(
                            customerCB::setValue
                    );
        }

        DatePicker orderDate =
                new DatePicker(
                        isNew
                                ? LocalDate.now()
                                : existing.getOrderDate()
                );

        orderDate.setMaxWidth(
                Double.MAX_VALUE
        );

        /*
         * ---------------------------------------------------------
         * TOP INFORMATION CARD
         * ---------------------------------------------------------
         */

        GridPane infoGrid =
                new GridPane();

        infoGrid.setHgap(18);
        infoGrid.setVgap(7);

        ColumnConstraints infoColumn1 =
                new ColumnConstraints();

        infoColumn1.setPercentWidth(50);

        ColumnConstraints infoColumn2 =
                new ColumnConstraints();

        infoColumn2.setPercentWidth(50);

        infoGrid.getColumnConstraints()
                .addAll(
                        infoColumn1,
                        infoColumn2
                );

        VBox customerBox =
                buildDialogField(
                        "CUSTOMER *",
                        customerCB
                );

        VBox dateBox =
                buildDialogField(
                        "ORDER DATE",
                        orderDate
                );

        infoGrid.add(
                customerBox,
                0,
                0
        );

        infoGrid.add(
                dateBox,
                1,
                0
        );

        /*
         * ---------------------------------------------------------
         * PRODUCT ROWS
         * ---------------------------------------------------------
         */

        ObservableList<OrderItemRow> rows =
                FXCollections.observableArrayList();

        if (!isNew) {

            List<SalesOrderItem> existingItems =
                    itemDAO.findByOrderId(
                            existing.getId()
                    );

            for (SalesOrderItem item :
                    existingItems) {

                Product product =
                        products.stream()
                                .filter(p ->
                                        p.getId()
                                                == item
                                                .getProductId()
                                )
                                .findFirst()
                                .orElse(null);

                if (product != null) {

                    rows.add(
                            new OrderItemRow(
                                    product,
                                    item.getQuantity(),
                                    item.getBulbsPerBox(),
                                    product.getSellingPrice(),
                                    BigDecimal.ZERO
                            )
                    );
                }
            }
        }

        if (rows.isEmpty()) {
            rows.add(
                    new OrderItemRow()
            );
        }

        VBox productsContainer =
                new VBox(8);

        productsContainer
                .getStyleClass()
                .add(
                        "sales-products-container"
                );

        Runnable refreshItems =
                () -> {

                    productsContainer
                            .getChildren()
                            .clear();

                    productsContainer
                            .getChildren()
                            .add(
                                    buildProductHeader()
                            );

                    for (OrderItemRow row :
                            rows) {

                        productsContainer
                                .getChildren()
                                .add(
                                        createStyledItemRow(
                                                row,
                                                rows,
                                                productsContainer,
                                                products
                                        )
                                );
                    }
                };

        refreshItems.run();

        /*
         * ---------------------------------------------------------
         * ADD PRODUCT
         * ---------------------------------------------------------
         */

        Button addItem =
                new Button(
                        "+ Add Product"
                );

        addItem.getStyleClass().add(
                "sales-dialog-add-button"
        );

        addItem.setOnAction(e -> {

            rows.add(
                    new OrderItemRow()
            );

            refreshItems.run();
        });

        /*
         * ---------------------------------------------------------
         * DISCOUNT / TAX / PAID
         * ---------------------------------------------------------
         */

        TextField discountF =
                new TextField(
                        isNew
                                ? "0"
                                : "0"
                );

        discountF.setPromptText(
                "0.00"
        );

        TextField taxF =
                new TextField(
                        isNew
                                ? "0"
                                : "0"
                );

        taxF.setPromptText(
                "0.00"
        );

        TextField paidF =
                new TextField(
                        isNew
                                ? "0"
                                : existing
                                .getPaidAmount()
                                .toPlainString()
                );

        paidF.setPromptText(
                "0.00"
        );

        addPercentageSuffix(
                discountF
        );

        addPercentageSuffix(
                taxF
        );

        /*
         * ---------------------------------------------------------
         * NOTES
         * ---------------------------------------------------------
         */

        TextArea notesF =
                new TextArea(
                        isNew
                                ? ""
                                : existing.getNotes() != null
                                ? existing.getNotes()
                                : ""
                );

        notesF.setPromptText(
                "Add any notes or special instructions..."
        );

        notesF.setPrefRowCount(4);

        notesF.setWrapText(true);

        notesF.getStyleClass().add(
                "sales-dialog-notes"
        );

        /*
         * ---------------------------------------------------------
         * TOTAL LABELS
         * ---------------------------------------------------------
         */

        Label subtotalValue =
                createMoneyValue();

        Label discountValue =
                createMoneyValue();

        Label taxValue =
                createMoneyValue();

        Label grandTotalValue =
                createGrandTotalValue();

        Label remainingValue =
                createMoneyValue();

        /*
         * ---------------------------------------------------------
         * LIVE TOTAL CALCULATION
         * ---------------------------------------------------------
         */

        Runnable updateTotals =
                () -> {

                    BigDecimal subtotal =
                            BigDecimal.ZERO;

                    for (OrderItemRow row :
                            rows) {

                        if (row.product == null) {
                            continue;
                        }

                        row.unitPrice =
                                row.product
                                        .getSellingPrice();

                        BigDecimal line =
                                row.unitPrice.multiply(
                                        BigDecimal.valueOf(
                                                Math.max(
                                                        0,
                                                        row.boxes
                                                )
                                        )
                                );

                        row.lineTotal =
                                line;

                        subtotal =
                                subtotal.add(line);
                    }

                    BigDecimal discountPercent =
                            percentageValue(
                                    discountF.getText()
                            );

                    BigDecimal taxPercent =
                            percentageValue(
                                    taxF.getText()
                            );

                    BigDecimal discountAmount =
                            subtotal.multiply(
                                    discountPercent
                                            .divide(
                                                    BigDecimal.valueOf(
                                                            100
                                                    ),
                                                    8,
                                                    RoundingMode.HALF_UP
                                            )
                            );

                    BigDecimal taxableAmount =
                            subtotal.subtract(
                                    discountAmount
                            );

                    if (taxableAmount.compareTo(
                            BigDecimal.ZERO
                    ) < 0) {
                        taxableAmount =
                                BigDecimal.ZERO;
                    }

                    BigDecimal taxAmount =
                            taxableAmount.multiply(
                                    taxPercent
                                            .divide(
                                                    BigDecimal.valueOf(
                                                            100
                                                    ),
                                                    8,
                                                    RoundingMode.HALF_UP
                                            )
                            );

                    BigDecimal grandTotal =
                            taxableAmount.add(
                                    taxAmount
                            );

                    BigDecimal paid =
                            safeDecimal(
                                    paidF.getText()
                            );

                    BigDecimal remaining =
                            grandTotal.subtract(
                                    paid
                            );

                    if (remaining.compareTo(
                            BigDecimal.ZERO
                    ) < 0) {
                        remaining =
                                BigDecimal.ZERO;
                    }

                    subtotalValue.setText(
                            NumberUtil.formatCurrency(
                                    subtotal
                            )
                    );

                    discountValue.setText(
                            NumberUtil.formatCurrency(
                                    discountAmount
                            )
                    );

                    taxValue.setText(
                            NumberUtil.formatCurrency(
                                    taxAmount
                            )
                    );

                    grandTotalValue.setText(
                            NumberUtil.formatCurrency(
                                    grandTotal
                            )
                    );

                    remainingValue.setText(
                            NumberUtil.formatCurrency(
                                    remaining
                            )
                    );
                };

        discountF.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateTotals.run()
                );

        taxF.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateTotals.run()
                );

        paidF.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateTotals.run()
                );

        /*
         * ---------------------------------------------------------
         * SUMMARY CARD
         * ---------------------------------------------------------
         */

        GridPane totalsGrid =
                new GridPane();

        totalsGrid.setHgap(20);
        totalsGrid.setVgap(10);

        VBox paymentBox =
                new VBox(10);

        paymentBox.getStyleClass().add(
                "sales-payment-section"
        );

        paymentBox.getChildren().addAll(

                createSectionTitle(
                        "PAYMENT"
                ),

                buildDialogField(
                        "DISCOUNT",
                        discountF
                ),

                buildDialogField(
                        "TAX",
                        taxF
                ),

                buildDialogField(
                        "PAID AMOUNT",
                        paidF
                ),

                buildDialogField(
                        "NOTES",
                        notesF
                )
        );

        VBox totalsBox =
                new VBox(10);

        totalsBox.getStyleClass().add(
                "sales-total-section"
        );

        totalsBox.getChildren().addAll(

                createSectionTitle(
                        "ORDER SUMMARY"
                ),

                createSummaryLine(
                        "Subtotal",
                        subtotalValue
                ),

                createSummaryLine(
                        "Discount",
                        discountValue
                ),

                createSummaryLine(
                        "Tax",
                        taxValue
                ),

                new Separator(),

                createSummaryLine(
                        "Grand Total",
                        grandTotalValue
                ),

                createSummaryLine(
                        "Remaining",
                        remainingValue
                )
        );

        totalsGrid.add(
                paymentBox,
                0,
                0
        );

        totalsGrid.add(
                totalsBox,
                1,
                0
        );

        ColumnConstraints paymentColumn =
                new ColumnConstraints();

        paymentColumn.setPercentWidth(55);

        ColumnConstraints totalColumn =
                new ColumnConstraints();

        totalColumn.setPercentWidth(45);

        totalsGrid.getColumnConstraints()
                .addAll(
                        paymentColumn,
                        totalColumn
                );

        /*
         * ---------------------------------------------------------
         * MAIN CONTENT
         * ---------------------------------------------------------
         */

        VBox content =
                new VBox(18);

        content.getStyleClass().add(
                "sales-dialog-content"
        );

        Label productsTitle =
                createSectionTitle(
                        "PRODUCTS"
                );

        content.getChildren().addAll(

                header,

                infoGrid,

                new Separator(),

                productsTitle,

                productsContainer,

                addItem,

                new Separator(),

                totalsGrid
        );

        ScrollPane scroll =
                new ScrollPane(
                        content
                );

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.getStyleClass().add(
                "sales-dialog-scroll"
        );

        pane.setContent(
                scroll
        );

        /*
         * ---------------------------------------------------------
         * INITIAL CALCULATION
         * ---------------------------------------------------------
         */

        updateTotals.run();

        /*
         * ---------------------------------------------------------
         * RESULT CONVERTER
         * ---------------------------------------------------------
         */

        dialog.setResultConverter(
                button -> {

                    if (button != saveButtonType) {
                        return null;
                    }

                    if (customerCB.getValue()
                            == null) {

                        AlertUtil.showWarning(
                                "Validation",
                                "Please select a customer."
                        );

                        return null;
                    }

                    if (orderDate.getValue()
                            == null) {

                        AlertUtil.showWarning(
                                "Validation",
                                "Please select an order date."
                        );

                        return null;
                    }

                    boolean hasProduct =
                            rows.stream()
                                    .anyMatch(
                                            r ->
                                                    r.product
                                                            != null
                                    );

                    if (!hasProduct) {

                        AlertUtil.showWarning(
                                "Validation",
                                "Add at least one product."
                        );

                        return null;
                    }

                    BigDecimal subtotal =
                            BigDecimal.ZERO;

                    for (OrderItemRow row :
                            rows) {

                        if (row.product == null) {
                            continue;
                        }

                        if (row.boxes <= 0) {

                            AlertUtil.showWarning(
                                    "Validation",
                                    "Boxes must be greater than zero."
                            );

                            return null;
                        }

                        /*
                         * PRICE ALWAYS COMES FROM PRODUCT.
                         */
                        row.unitPrice =
                                row.product
                                        .getSellingPrice();

                        row.lineTotal =
                                row.unitPrice.multiply(
                                        BigDecimal.valueOf(
                                                row.boxes
                                        )
                                );

                        subtotal =
                                subtotal.add(
                                        row.lineTotal
                                );
                    }

                    BigDecimal discountPercent =
                            percentageValue(
                                    discountF.getText()
                            );

                    BigDecimal taxPercent =
                            percentageValue(
                                    taxF.getText()
                            );

                    if (discountPercent.compareTo(
                            BigDecimal.ZERO
                    ) < 0 ||
                            discountPercent.compareTo(
                                    BigDecimal.valueOf(100)
                            ) > 0) {

                        AlertUtil.showWarning(
                                "Validation",
                                "Discount must be between 0% and 100%."
                        );

                        return null;
                    }

                    if (taxPercent.compareTo(
                            BigDecimal.ZERO
                    ) < 0) {

                        AlertUtil.showWarning(
                                "Validation",
                                "Tax cannot be negative."
                        );

                        return null;
                    }

                    BigDecimal discountAmount =
                            subtotal.multiply(
                                    discountPercent
                                            .divide(
                                                    BigDecimal.valueOf(100),
                                                    8,
                                                    RoundingMode.HALF_UP
                                            )
                            );

                    BigDecimal taxable =
                            subtotal.subtract(
                                    discountAmount
                            );

                    BigDecimal taxAmount =
                            taxable.multiply(
                                    taxPercent
                                            .divide(
                                                    BigDecimal.valueOf(100),
                                                    8,
                                                    RoundingMode.HALF_UP
                                            )
                            );

                    BigDecimal grandTotal =
                            taxable.add(
                                    taxAmount
                            );

                    BigDecimal paid =
                            safeDecimal(
                                    paidF.getText()
                            );

                    BigDecimal remaining =
                            grandTotal.subtract(
                                    paid
                            );

                    if (remaining.compareTo(
                            BigDecimal.ZERO
                    ) < 0) {
                        remaining =
                                BigDecimal.ZERO;
                    }

                    SalesOrder order =
                            isNew
                                    ? new SalesOrder()
                                    : existing;

                    order.setCustomerId(
                            customerCB.getValue()
                                    .getId()
                    );

                    order.setOrderDate(
                            orderDate.getValue()
                    );

                    /*
                     * DATABASE STORES AMOUNTS.
                     * UI ACCEPTS PERCENTAGES.
                     */
                    order.setDiscount(
                            discountAmount
                    );

                    order.setTax(
                            taxAmount
                    );

                    order.setPaidAmount(
                            paid
                    );

                    order.setNotes(
                            notesF.getText()
                    );

                    order.setSubtotal(
                            subtotal
                    );

                    order.setGrandTotal(
                            grandTotal
                    );

                    order.setRemaining(
                            remaining
                    );

                    order.setStatus(
                            isNew
                                    ? "DRAFT"
                                    : existing.getStatus()
                    );

                    order.setItems(
                            FXCollections
                                    .<SalesOrderItem>
                                            observableArrayList()
                    );

                    return order;
                }
        );

        /*
         * ---------------------------------------------------------
         * SHOW
         * ---------------------------------------------------------
         */

        Optional<SalesOrder> result =
                dialog.showAndWait();

        result.ifPresent(
                order -> {

                    try {

                        if (isNew) {

                            order.setOrderNumber(
                                    generateOrderNumber()
                            );

                            order.setCreatedBy(
                                    SessionManager
                                            .getInstance()
                                            .getCurrentUserId()
                            );

                            orderDAO.save(
                                    order
                            );

                            saveItems(
                                    order,
                                    rows
                            );

                            auditDAO.log(
                                    new AuditLog(
                                            SessionManager
                                                    .getInstance()
                                                    .getCurrentUserId(),

                                            SessionManager
                                                    .getInstance()
                                                    .getCurrentUsername(),

                                            "CREATE",

                                            "SALES",

                                            "Created sales order: "
                                                    + order
                                                    .getOrderNumber()
                                    )
                            );

                        } else {

                            orderDAO.update(
                                    order
                            );

                            itemDAO.deleteByOrderId(
                                    order.getId()
                            );

                            saveItems(
                                    order,
                                    rows
                            );

                            auditDAO.log(
                                    new AuditLog(
                                            SessionManager
                                                    .getInstance()
                                                    .getCurrentUserId(),

                                            SessionManager
                                                    .getInstance()
                                                    .getCurrentUsername(),

                                            "UPDATE",

                                            "SALES",

                                            "Updated sales order: "
                                                    + order
                                                    .getOrderNumber()
                                    )
                            );
                        }

                        loadData();

                    } catch (SQLException e) {

                        AlertUtil.showDatabaseError(
                                e.getMessage()
                        );
                    }
                }
        );

    } catch (SQLException e) {

        AlertUtil.showDatabaseError(
                e.getMessage()
        );
    }
}

    private void saveItems(
            SalesOrder order,
            ObservableList<OrderItemRow> rows
    ) throws SQLException {

        for (OrderItemRow row : rows) {

            if (row.product == null) {
                continue;
            }

            SalesOrderItem item =
                    new SalesOrderItem();

            item.setSalesOrderId(
                    order.getId()
            );

            item.setProductId(
                    row.product.getId()
            );

            item.setQuantity(
                    row.boxes
            );

            item.setBulbsPerBox(
                    row.bulbsPerBox
            );

            item.setUnitPrice(
                    row.unitPrice
            );

            item.setDiscount(
                    row.discount
            );

            item.setLineTotal(
                    row.lineTotal
            );

            itemDAO.save(item);
        }
    }

    private HBox createStyledItemRow(
        OrderItemRow row,
        ObservableList<OrderItemRow> rows,
        VBox parent,
        List<Product> products
) {

    ComboBox<Product> productCB =
            new ComboBox<>(
                    FXCollections.observableArrayList(
                            products
                    )
            );

    productCB.setMaxWidth(
            Double.MAX_VALUE
    );

    productCB.setPromptText(
            "Select product"
    );

    productCB.setConverter(
            new StringConverter<>() {

                @Override
                public String toString(
                        Product p
                ) {

                    if (p == null) {
                        return "";
                    }

                    return p.getName()
                            + " ["
                            + p.getProductCode()
                            + "]";
                }

                @Override
                public Product fromString(
                        String s
                ) {
                    return null;
                }
            }
    );

    productCB.setValue(
            row.product
    );


    Label price =
            new Label(
                    row.product == null
                            ? "Rs. 0.00"
                            : NumberUtil.formatCurrency(
                                    row.product
                                            .getSellingPrice()
                            )
            );

    price.getStyleClass().add(
            "sales-product-price"
    );


    TextField boxesF =
            new TextField(
                    String.valueOf(
                            row.boxes
                    )
            );

    boxesF.getStyleClass().add(
            "sales-number-field"
    );


    TextField bulbsF =
            new TextField(
                    String.valueOf(
                            row.bulbsPerBox
                    )
            );

    bulbsF.getStyleClass().add(
            "sales-number-field"
    );


    Label totalBulbs =
            new Label(
                    String.valueOf(
                            row.boxes
                                    * row.bulbsPerBox
                    )
            );

    totalBulbs.getStyleClass().add(
            "sales-total-bulbs"
    );


    Button remove =
            new Button("×");

    remove.setTooltip(
            new Tooltip(
                    "Remove product"
            )
    );

    remove.getStyleClass().add(
            "sales-remove-button"
    );


    /*
     * Product selection
     */

    productCB.setOnAction(e -> {

        Product selected =
                productCB.getValue();

        row.product =
                selected;

        if (selected != null) {

            /*
             * STRICT REQUIREMENT:
             * PRICE ALWAYS COMES FROM
             * PRODUCT DATABASE RECORD.
             */

            row.unitPrice =
                    selected.getSellingPrice();

            price.setText(
                    NumberUtil.formatCurrency(
                            row.unitPrice
                    )
            );

        } else {

            row.unitPrice =
                    BigDecimal.ZERO;

            price.setText(
                    "Rs. 0.00"
            );
        }
    });


    /*
     * Boxes
     */

    boxesF.textProperty()
            .addListener(
                    (obs, oldValue, newValue) -> {

                        try {

                            row.boxes =
                                    Integer.parseInt(
                                            newValue
                                    );

                        } catch (Exception ex) {

                            row.boxes = 0;
                        }

                        totalBulbs.setText(
                                String.valueOf(
                                        Math.max(
                                                0,
                                                row.boxes
                                        )
                                                * Math.max(
                                                0,
                                                row.bulbsPerBox
                                        )
                                )
                        );
                    }
            );


    /*
     * Bulbs per box
     */

    bulbsF.textProperty()
            .addListener(
                    (obs, oldValue, newValue) -> {

                        try {

                            row.bulbsPerBox =
                                    Integer.parseInt(
                                            newValue
                                    );

                        } catch (Exception ex) {

                            row.bulbsPerBox = 1;
                        }

                        totalBulbs.setText(
                                String.valueOf(
                                        Math.max(
                                                0,
                                                row.boxes
                                        )
                                                * Math.max(
                                                0,
                                                row.bulbsPerBox
                                        )
                                )
                        );
                    }
            );


    /*
     * Remove
     */

    remove.setOnAction(e -> {

        if (rows.size() <= 1) {
            return;
        }

        rows.remove(row);

        parent.getChildren()
                .removeIf(
                        node ->
                                node.getUserData()
                                        == row
                );

        /*
         * Rebuild rows.
         */

        parent.getChildren().clear();

        for (OrderItemRow r :
                rows) {

            parent.getChildren().add(
                    createStyledItemRow(
                            r,
                            rows,
                            parent,
                            products
                    )
            );
        }
    });


    GridPane rowGrid =
            new GridPane();

    rowGrid.setHgap(10);

    rowGrid.setVgap(0);

    rowGrid.setAlignment(
            Pos.CENTER_LEFT
    );

    rowGrid.getStyleClass().add(
            "sales-product-row"
    );

    /*
     * Column widths
     */

    ColumnConstraints productColumn =
            new ColumnConstraints();

    productColumn.setPercentWidth(31);

    ColumnConstraints priceColumn =
            new ColumnConstraints();

    priceColumn.setPercentWidth(16);

    ColumnConstraints boxesColumn =
            new ColumnConstraints();

    boxesColumn.setPercentWidth(11);

    ColumnConstraints bulbsColumn =
            new ColumnConstraints();

    bulbsColumn.setPercentWidth(13);

    ColumnConstraints totalColumn =
            new ColumnConstraints();

    totalColumn.setPercentWidth(16);

    ColumnConstraints actionColumn =
            new ColumnConstraints();

    actionColumn.setPercentWidth(7);

    rowGrid.getColumnConstraints()
            .addAll(
                    productColumn,
                    priceColumn,
                    boxesColumn,
                    bulbsColumn,
                    totalColumn,
                    actionColumn
            );

    rowGrid.add(
            productCB,
            0,
            0
    );

    rowGrid.add(
            price,
            1,
            0
    );

    rowGrid.add(
            boxesF,
            2,
            0
    );

    rowGrid.add(
            bulbsF,
            3,
            0
    );

    rowGrid.add(
            totalBulbs,
            4,
            0
    );

    rowGrid.add(
            remove,
            5,
            0
    );

    rowGrid.setUserData(
            row
    );

    return new HBox(
            rowGrid
    );
}
private GridPane buildProductHeader() {

    GridPane header =
            new GridPane();

    header.setHgap(10);

    header.getStyleClass().add(
            "sales-product-header"
    );

    String[] labels = {
            "PRODUCT",
            "UNIT PRICE",
            "BOXES",
            "BULBS / BOX",
            "TOTAL BULBS",
            ""
    };

    double[] widths = {
            31,
            16,
            11,
            13,
            16,
            7
    };

    for (int i = 0; i < labels.length; i++) {

        Label label =
                new Label(
                        labels[i]
                );

        label.getStyleClass().add(
                "sales-product-header-label"
        );

        ColumnConstraints column =
                new ColumnConstraints();

        column.setPercentWidth(
                widths[i]
        );

        header.getColumnConstraints()
                .add(column);

        header.add(
                label,
                i,
                0
        );
    }

    return header;
}
    private void showOrderDetails(
            SalesOrder order
    ) {

        try {

            List<SalesOrderItem> items =
                    itemDAO.findByOrderId(
                            order.getId()
                    );

            Dialog<Void> dialog =
                    new Dialog<>();

            dialog.setTitle(
                    "Sales Order - "
                            + order.getOrderNumber()
            );

            dialog.getDialogPane()
                    .getButtonTypes()
                    .add(ButtonType.CLOSE);

            VBox root =
                    new VBox(12);

            root.setPadding(
                    new Insets(20)
            );

            Label title =
                    new Label(
                            order.getOrderNumber()
                    );

            title.setStyle(
                    "-fx-font-size:20px;" +
                    "-fx-font-weight:bold;" +
                    "-fx-text-fill:#4e8ef7;"
            );

            Label customer =
                    new Label(
                            "Customer: "
                                    + order.getCustomerName()
                                    + " ["
                                    + order.getCustomerCode()
                                    + "]"
                    );

            Label date =
                    new Label(
                            "Order Date: "
                                    + order.getOrderDate()
                    );

            root.getChildren().addAll(
                    title,
                    customer,
                    date,
                    new Separator()
            );

            for (SalesOrderItem item :
                    items) {

                VBox itemBox =
                        new VBox(4);

                Label product =
                        new Label(
                                item.getProductName()
                        );

                product.setStyle(
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:#ffffff;"
                );

                Label quantity =
                        new Label(
                                item.getQuantity()
                                        + " Boxes / "
                                        + item.getTotalBulbs()
                                        + " Bulbs"
                        );

                itemBox.getChildren().addAll(
                        product,
                        quantity
                );

                root.getChildren().add(
                        itemBox
                );
            }

            root.getChildren().add(
                    new Separator()
            );

            root.getChildren().addAll(
                    new Label(
                            "Subtotal: "
                                    + NumberUtil.formatCurrency(
                                            order.getSubtotal()
                                    )
                    ),
                    new Label(
                            "Discount: "
                                    + NumberUtil.formatCurrency(
                                            order.getDiscount()
                                    )
                    ),
                    new Label(
                            "Tax: "
                                    + NumberUtil.formatCurrency(
                                            order.getTax()
                                    )
                    ),
                    new Label(
                            "Grand Total: "
                                    + NumberUtil.formatCurrency(
                                            order.getGrandTotal()
                                    )
                    ),
                    new Label(
                            "Paid: "
                                    + NumberUtil.formatCurrency(
                                            order.getPaidAmount()
                                    )
                    ),
                    new Label(
                            "Remaining: "
                                    + NumberUtil.formatCurrency(
                                            order.getRemaining()
                                    )
                    )
            );

            dialog.getDialogPane()
                    .setContent(root);

            dialog.showAndWait();

        } catch (SQLException e) {

            AlertUtil.showDatabaseError(
                    e.getMessage()
            );
        }
    }

    private void handleCancel(
            SalesOrder order
    ) {

        if ("CANCELLED".equals(
                order.getStatus()
        )) {
            return;
        }

        if (!AlertUtil.showConfirm(
                "Cancel Sales Order",
                "Cancel sales order "
                        + order.getOrderNumber()
                        + "?"
        )) {
            return;
        }

        try {

            orderDAO.cancel(
                    order.getId()
            );

            auditDAO.log(
                    new AuditLog(
                            SessionManager
                                    .getInstance()
                                    .getCurrentUserId(),
                            SessionManager
                                    .getInstance()
                                    .getCurrentUsername(),
                            "UPDATE",
                            "SALES",
                            "Cancelled sales order: "
                                    + order.getOrderNumber()
                    )
            );

            loadData();

        } catch (SQLException e) {

            AlertUtil.showDatabaseError(
                    e.getMessage()
            );
        }
    }

    private String generateOrderNumber()
            throws SQLException {

        int year =
                Year.now().getValue();

        List<SalesOrder> orders =
                orderDAO.findAll();

        int max = 0;

        String prefix =
                "SO-" + year + "-";

        for (SalesOrder order : orders) {

            String number =
                    order.getOrderNumber();

            if (number != null &&
                    number.startsWith(prefix)) {

                try {

                    int sequence =
                            Integer.parseInt(
                                    number.substring(
                                            prefix.length()
                                    )
                            );

                    max =
                            Math.max(
                                    max,
                                    sequence
                            );

                } catch (NumberFormatException ignored) {
                }
            }
        }

        return String.format(
                "%s%05d",
                prefix,
                max + 1
        );
    }

    private BigDecimal safeDecimal(
            String value
    ) {

        try {

            BigDecimal result =
                    NumberUtil.parseBigDecimal(
                            value
                    );

            return result != null
                    ? result
                    : BigDecimal.ZERO;

        } catch (Exception e) {

            return BigDecimal.ZERO;
        }
    }
private VBox buildDialogField(
        String labelText,
        Node control
) {

    Label label =
            new Label(labelText);

    label.getStyleClass().add(
            "sales-field-label"
    );

    VBox box =
            new VBox(6);

    box.getChildren().addAll(
            label,
            control
    );

    VBox.setVgrow(
            control,
            Priority.NEVER
    );

    return box;
}


private Label createSectionTitle(
        String text
) {

    Label label =
            new Label(text);

    label.getStyleClass().add(
            "sales-section-title"
    );

    return label;
}


private Label createMoneyValue() {

    Label label =
            new Label("Rs. 0.00");

    label.getStyleClass().add(
            "sales-money-value"
    );

    return label;
}


private Label createGrandTotalValue() {

    Label label =
            new Label("Rs. 0.00");

    label.getStyleClass().add(
            "sales-grand-total"
    );

    return label;
}


private HBox createSummaryLine(
        String labelText,
        Label value
) {

    Label label =
            new Label(labelText);

    label.getStyleClass().add(
            "sales-summary-label"
    );

    Region spacer =
            new Region();

    HBox.setHgrow(
            spacer,
            Priority.ALWAYS
    );

    HBox row =
            new HBox(
                    10,
                    label,
                    spacer,
                    value
            );

    row.setAlignment(
            Pos.CENTER_LEFT
    );

    return row;
}


private void addPercentageSuffix(
        TextField field
) {

    field.setPromptText("0.00");

    field.setTextFormatter(
            new TextFormatter<>(
                    change -> {

                        String text =
                                change.getControlNewText();

                        if (text.isEmpty()) {
                            return change;
                        }

                        if (text.matches(
                                "\\d{0,3}(\\.\\d{0,2})?"
                        )) {
                            return change;
                        }

                        return null;
                    }
            )
    );
}


private BigDecimal percentageValue(
        String value
) {

    BigDecimal result =
            safeDecimal(value);

    if (result.compareTo(
            BigDecimal.ZERO
    ) < 0) {

        return BigDecimal.ZERO;
    }

    return result;
}
    private static class OrderItemRow {

        Product product;

        int boxes = 1;

        int bulbsPerBox = 6;

        BigDecimal unitPrice =
                BigDecimal.ZERO;

        BigDecimal discount =
                BigDecimal.ZERO;

        BigDecimal lineTotal =
                BigDecimal.ZERO;

        OrderItemRow() {
        }

        OrderItemRow(
                Product product,
                int boxes,
                int bulbsPerBox,
                BigDecimal unitPrice,
                BigDecimal discount
        ) {

            this.product = product;
            this.boxes = boxes;
            this.bulbsPerBox =
                    bulbsPerBox > 0
                            ? bulbsPerBox
                            : 6;

            this.unitPrice =
                    unitPrice != null
                            ? unitPrice
                            : BigDecimal.ZERO;

            this.discount =
                    discount != null
                            ? discount
                            : BigDecimal.ZERO;

            calculate();
        }

        void calculate() {

            lineTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    boxes
                            )
                    ).subtract(
                            discount
                    );

            if (lineTotal.compareTo(
                    BigDecimal.ZERO
            ) < 0) {

                lineTotal =
                        BigDecimal.ZERO;
            }
        }
    }
}