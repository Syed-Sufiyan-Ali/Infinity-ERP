package com.nova.factoryerp.controllers.inventory;

import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.Customer;
import com.nova.factoryerp.utils.AlertUtil;
import com.nova.factoryerp.utils.NumberUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerHistoryController {

    @FXML private Label customerNameLabel;
    @FXML private Label customerCodeLabel;

    @FXML private Label totalOrdersLabel;
    @FXML private Label completedOrdersLabel;
    @FXML private Label totalBoxesLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label totalPaidLabel;
    @FXML private Label outstandingLabel;

    @FXML private VBox ordersContainer;

    private final DatabaseConnection db =
            DatabaseConnection.getInstance();

    private Customer customer;

    /**
     * Called by CustomersController before displaying the history window.
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;

        if (customer != null) {
            customerNameLabel.setText(customer.getName());
            customerCodeLabel.setText(
                    "[" + customer.getCustomerCode() + "]"
            );

            loadHistory();
        }
    }

    @FXML
    public void initialize() {
        // Data is loaded after setCustomer() is called.
    }

    private void loadHistory() {

        if (customer == null) {
            return;
        }

        Thread thread = new Thread(() -> {

            try {

                HistoryData history =
                        loadCustomerHistory(customer.getId());

                Platform.runLater(() -> {

                    totalOrdersLabel.setText(
                            String.valueOf(history.totalOrders)
                    );

                    completedOrdersLabel.setText(
                            String.valueOf(history.completedOrders)
                    );

                    totalBoxesLabel.setText(
                            NumberUtil.formatNumber(
                                    BigDecimal.valueOf(
                                            history.totalBoxes
                                    )
                            )
                    );

                    totalAmountLabel.setText(
                            NumberUtil.formatCurrency(
                                    history.totalAmount
                            )
                    );

                    totalPaidLabel.setText(
                            NumberUtil.formatCurrency(
                                    history.totalPaid
                            )
                    );

                    outstandingLabel.setText(
                            NumberUtil.formatCurrency(
                                    history.outstanding
                            )
                    );

                    ordersContainer.getChildren().clear();

                    if (history.orders.isEmpty()) {

                        Label emptyLabel =
                                new Label(
                                        "No sales orders found for this customer."
                                );

                        emptyLabel.setStyle(
                                "-fx-text-fill:#9199b0;" +
                                "-fx-font-size:14px;" +
                                "-fx-padding:30;"
                        );

                        ordersContainer
                                .getChildren()
                                .add(emptyLabel);

                    } else {

                        for (OrderHistory order :
                                history.orders) {

                            ordersContainer
                                    .getChildren()
                                    .add(
                                            createOrderCard(order)
                                    );
                        }
                    }
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

    private HistoryData loadCustomerHistory(
            int customerId
    ) throws SQLException {

        HistoryData result =
                new HistoryData();

        String orderSQL =
                "SELECT " +
                "so.id, " +
                "so.order_number, " +
                "so.order_date, " +
                "so.subtotal, " +
                "so.discount, " +
                "so.tax, " +
                "so.grand_total, " +
                "so.paid_amount, " +
                "so.remaining, " +
                "so.status, " +
                "so.notes " +
                "FROM sales_orders so " +
                "WHERE so.customer_id = ? " +
                "ORDER BY so.order_date DESC, so.id DESC";

        Connection conn =
                db.getConnection();

        try {

            /*
             * ---------------------------------------------------------
             * Load orders
             * ---------------------------------------------------------
             */
            try (PreparedStatement ps =
                         conn.prepareStatement(orderSQL)) {

                ps.setInt(1, customerId);

                try (ResultSet rs =
                             ps.executeQuery()) {

                    while (rs.next()) {

                        OrderHistory order =
                                new OrderHistory();

                        order.id =
                                rs.getLong("id");

                        order.orderNumber =
                                rs.getString("order_number");

                        Date sqlDate =
                                rs.getDate("order_date");

                        order.orderDate =
                                sqlDate != null
                                        ? sqlDate.toLocalDate()
                                        : null;

                        order.subtotal =
                                safeDecimal(
                                        rs.getBigDecimal("subtotal")
                                );

                        order.discount =
                                safeDecimal(
                                        rs.getBigDecimal("discount")
                                );

                        order.tax =
                                safeDecimal(
                                        rs.getBigDecimal("tax")
                                );

                        order.grandTotal =
                                safeDecimal(
                                        rs.getBigDecimal("grand_total")
                                );

                        order.paidAmount =
                                safeDecimal(
                                        rs.getBigDecimal("paid_amount")
                                );

                        order.remaining =
                                safeDecimal(
                                        rs.getBigDecimal("remaining")
                                );

                        order.status =
                                rs.getString("status");

                        order.notes =
                                rs.getString("notes");

                        loadOrderItems(
                                conn,
                                order
                        );

                        result.orders.add(order);

                        result.totalOrders++;

                        /*
                         * Completed means DELIVERED.
                         */
                        if ("DELIVERED".equals(
                                order.status)) {

                            result.completedOrders++;

                            for (OrderItemHistory item :
                                    order.items) {

                                result.totalBoxes +=
                                        item.quantity;
                            }
                        }

                        /*
                         * Financial summary.
                         */
                        result.totalAmount =
                                result.totalAmount
                                        .add(
                                                order.grandTotal
                                        );

                        result.totalPaid =
                                result.totalPaid
                                        .add(
                                                order.paidAmount
                                        );
                    }
                }
            }

        } finally {

            db.releaseConnection(conn);
        }

        result.outstanding =
                result.totalAmount
                        .subtract(
                                result.totalPaid
                        );

        return result;
    }

    private void loadOrderItems(
            Connection conn,
            OrderHistory order
    ) throws SQLException {

        String sql =
                "SELECT " +
                "soi.id, " +
                "soi.product_id, " +
                "soi.quantity, " +
                "soi.unit_price, " +
                "soi.discount, " +
                "soi.line_total, " +
                "p.product_code, " +
                "p.name AS product_name " +
                "FROM sales_order_items soi " +
                "JOIN products p " +
                "ON soi.product_id = p.id " +
                "WHERE soi.sales_order_id = ? " +
                "ORDER BY soi.id";

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setLong(
                    1,
                    order.id
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    OrderItemHistory item =
                            new OrderItemHistory();

                    item.id =
                            rs.getLong("id");

                    item.productId =
                            rs.getInt("product_id");

                    item.productCode =
                            rs.getString("product_code");

                    item.productName =
                            rs.getString("product_name");

                    item.quantity =
                            rs.getInt("quantity");

                    item.unitPrice =
                            safeDecimal(
                                    rs.getBigDecimal(
                                            "unit_price"
                                    )
                            );

                    item.discount =
                            safeDecimal(
                                    rs.getBigDecimal(
                                            "discount"
                                    )
                            );

                    item.lineTotal =
                            safeDecimal(
                                    rs.getBigDecimal(
                                            "line_total"
                                    )
                            );

                    order.items.add(item);
                }
            }
        }
    }

    private VBox createOrderCard(
            OrderHistory order
    ) {

        VBox card =
                new VBox(10);

        card.setPadding(
                new Insets(18)
        );

        card.setStyle(
                "-fx-background-color:#1a1e2a;" +
                "-fx-border-color:#303646;" +
                "-fx-border-width:1;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );

        /*
         * ---------------------------------------------------------
         * Order Header
         * ---------------------------------------------------------
         */

        HBox header =
                new HBox(12);

        Label orderNumber =
                new Label(
                        order.orderNumber
                );

        orderNumber.setStyle(
                "-fx-font-size:17px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#4e8ef7;"
        );

        Label date =
                new Label(
                        order.orderDate != null
                                ? order.orderDate.toString()
                                : "-"
                );

        date.setStyle(
                "-fx-text-fill:#9199b0;" +
                "-fx-font-size:12px;"
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label status =
                new Label(
                        order.status != null
                                ? order.status
                                : "-"
                );

        status.setStyle(
                getStatusStyle(
                        order.status
                )
        );

        header.getChildren().addAll(
                orderNumber,
                date,
                spacer,
                status
        );

        card.getChildren().add(header);

        /*
         * ---------------------------------------------------------
         * Products / Boxes
         * ---------------------------------------------------------
         */

        for (OrderItemHistory item :
                order.items) {

            VBox itemBox =
                    new VBox(7);

            itemBox.setPadding(
                    new Insets(
                            10,
                            12,
                            10,
                            12
                    )
            );

            itemBox.setStyle(
                    "-fx-background-color:#202532;" +
                    "-fx-background-radius:6;"
            );

            Label product =
                    new Label(
                            item.productName
                                    + " ["
                                    + item.productCode
                                    + "]"
                    );

            product.setStyle(
                    "-fx-font-size:14px;" +
                    "-fx-font-weight:bold;" +
                    "-fx-text-fill:#ffffff;"
            );

            /*
             * Quantity currently represents BOXES.
             */
            Label quantity =
                    new Label(
                            item.quantity
                                    + " Boxes"
                    );

            quantity.setStyle(
                    "-fx-text-fill:#b7becf;" +
                    "-fx-font-size:13px;"
            );

            /*
             * Individual box serials will be displayed here
             * once the box-assignment table is implemented.
             */
            Label boxInfo =
                    new Label(
                            "Box serial numbers: " +
                            "Not assigned yet"
                    );

            boxInfo.setStyle(
                    "-fx-text-fill:#9199b0;" +
                    "-fx-font-size:12px;"
            );

            Label amount =
                    new Label(
                            "Line Total: "
                                    + NumberUtil.formatCurrency(
                                            item.lineTotal
                                    )
                    );

            amount.setStyle(
                    "-fx-text-fill:#b7becf;" +
                    "-fx-font-size:12px;"
            );

            itemBox.getChildren().addAll(
                    product,
                    quantity,
                    boxInfo,
                    amount
            );

            card.getChildren().add(itemBox);
        }

        /*
         * ---------------------------------------------------------
         * Financial summary
         * ---------------------------------------------------------
         */

        Separator separator =
                new Separator();

        card.getChildren().add(separator);

        GridPane financial =
                new GridPane();

        financial.setHgap(25);
        financial.setVgap(5);

        addFinancialRow(
                financial,
                0,
                "Total:",
                NumberUtil.formatCurrency(
                        order.grandTotal
                )
        );

        addFinancialRow(
                financial,
                1,
                "Paid:",
                NumberUtil.formatCurrency(
                        order.paidAmount
                )
        );

        addFinancialRow(
                financial,
                2,
                "Outstanding:",
                NumberUtil.formatCurrency(
                        order.remaining
                )
        );

        card.getChildren().add(
                financial
        );

        if (order.notes != null
                && !order.notes.isBlank()) {

            Label notes =
                    new Label(
                            "Notes: "
                                    + order.notes
                    );

            notes.setWrapText(true);

            notes.setStyle(
                    "-fx-text-fill:#9199b0;" +
                    "-fx-font-size:12px;"
            );

            card.getChildren().add(
                    notes
            );
        }

        return card;
    }

    private void addFinancialRow(
            GridPane grid,
            int row,
            String labelText,
            String valueText
    ) {

        Label label =
                new Label(labelText);

        label.setStyle(
                "-fx-text-fill:#9199b0;" +
                "-fx-font-size:12px;"
        );

        Label value =
                new Label(valueText);

        value.setStyle(
                "-fx-text-fill:#ffffff;" +
                "-fx-font-size:12px;" +
                "-fx-font-weight:bold;"
        );

        grid.add(
                label,
                0,
                row
        );

        grid.add(
                value,
                1,
                row
        );
    }

    private String getStatusStyle(
            String status
    ) {

        if ("DELIVERED".equals(status)) {

            return
                    "-fx-background-color:#214d37;" +
                    "-fx-text-fill:#6ee7a5;" +
                    "-fx-padding:4 10 4 10;" +
                    "-fx-background-radius:12;" +
                    "-fx-font-size:11px;";
        }

        if ("CANCELLED".equals(status)) {

            return
                    "-fx-background-color:#512b2b;" +
                    "-fx-text-fill:#ff8f8f;" +
                    "-fx-padding:4 10 4 10;" +
                    "-fx-background-radius:12;" +
                    "-fx-font-size:11px;";
        }

        if ("RETURNED".equals(status)) {

            return
                    "-fx-background-color:#514522;" +
                    "-fx-text-fill:#ffd86e;" +
                    "-fx-padding:4 10 4 10;" +
                    "-fx-background-radius:12;" +
                    "-fx-font-size:11px;";
        }

        if ("CONFIRMED".equals(status)) {

            return
                    "-fx-background-color:#203e59;" +
                    "-fx-text-fill:#6fb8ff;" +
                    "-fx-padding:4 10 4 10;" +
                    "-fx-background-radius:12;" +
                    "-fx-font-size:11px;";
        }

        return
                "-fx-background-color:#303646;" +
                "-fx-text-fill:#b7becf;" +
                "-fx-padding:4 10 4 10;" +
                "-fx-background-radius:12;" +
                "-fx-font-size:11px;";
    }

    private BigDecimal safeDecimal(
            BigDecimal value
    ) {

        return value != null
                ? value
                : BigDecimal.ZERO;
    }

    @FXML
    private void handleClose() {

        if (ordersContainer != null
                && ordersContainer.getScene() != null
                && ordersContainer.getScene()
                        .getWindow() != null) {

            ordersContainer
                    .getScene()
                    .getWindow()
                    .hide();
        }
    }

    /*
     * =============================================================
     * Data Classes
     * =============================================================
     */

    private static class HistoryData {

        int totalOrders = 0;

        int completedOrders = 0;

        int totalBoxes = 0;

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        BigDecimal totalPaid =
                BigDecimal.ZERO;

        BigDecimal outstanding =
                BigDecimal.ZERO;

        List<OrderHistory> orders =
                new ArrayList<>();
    }

    private static class OrderHistory {

        long id;

        String orderNumber;

        LocalDate orderDate;

        BigDecimal subtotal =
                BigDecimal.ZERO;

        BigDecimal discount =
                BigDecimal.ZERO;

        BigDecimal tax =
                BigDecimal.ZERO;

        BigDecimal grandTotal =
                BigDecimal.ZERO;

        BigDecimal paidAmount =
                BigDecimal.ZERO;

        BigDecimal remaining =
                BigDecimal.ZERO;

        String status;

        String notes;

        List<OrderItemHistory> items =
                new ArrayList<>();
    }

    private static class OrderItemHistory {

        long id;

        int productId;

        String productCode;

        String productName;

        int quantity;

        BigDecimal unitPrice =
                BigDecimal.ZERO;

        BigDecimal discount =
                BigDecimal.ZERO;

        BigDecimal lineTotal =
                BigDecimal.ZERO;
    }
}