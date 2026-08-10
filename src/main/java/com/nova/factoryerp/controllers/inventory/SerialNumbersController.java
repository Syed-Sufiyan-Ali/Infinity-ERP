package com.nova.factoryerp.controllers.inventory;

import com.nova.factoryerp.dao.impl.ProductDAOImpl;
import com.nova.factoryerp.dao.interfaces.ProductDAO;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.models.Product;
import com.nova.factoryerp.utils.AlertUtil;
import com.nova.factoryerp.utils.DateUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SerialNumbersController {
    @FXML private TextField searchField;
    @FXML private ComboBox<Product> productFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label countLabel;
    @FXML private TableView<SerialRow> serialsTable;
    @FXML private TableColumn<SerialRow, String> serialCol;
    @FXML private TableColumn<SerialRow, String> productCol;
    @FXML private TableColumn<SerialRow, String> batchCol;
    @FXML private TableColumn<SerialRow, String> dateCol;
    @FXML private TableColumn<SerialRow, String> statusCol;
    @FXML private TableColumn<SerialRow, String> notesCol;

    private final ProductDAO productDAO = new ProductDAOImpl();
    private final DatabaseConnection db = DatabaseConnection.getInstance();
    private final ObservableList<SerialRow> data = FXCollections.observableArrayList();

    public static class SerialRow {
        public String serial, product, batch, date, status, notes;
        SerialRow(String serial, String product, String batch, String date, String status, String notes) {
            this.serial=serial; this.product=product; this.batch=batch;
            this.date=date; this.status=status; this.notes=notes;
        }
    }

    @FXML
    public void initialize() {
        serialCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().serial));
        productCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().product));
        batchCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().batch));
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().date));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status));
        notesCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().notes));

        statusFilter.setItems(FXCollections.observableArrayList("","IN_STOCK","SOLD","DAMAGED","RETURNED"));
        productFilter.setConverter(new StringConverter<>() {
            public String toString(Product p) { return p == null ? "" : p.getName(); }
            public Product fromString(String s) { return null; }
        });

        serialsTable.setItems(data);
        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                List<Product> products = productDAO.findAll();
                String kw  = searchField.getText();
                String st  = statusFilter.getValue();
                Product selectedProduct = productFilter.getValue();

                StringBuilder sql = new StringBuilder(
                    "SELECT sn.serial_number, p.name AS product_name, " +
                    "pb.batch_number, sn.manufactured_date, sn.status, sn.notes " +
                    "FROM product_serial_numbers sn " +
                    "JOIN products p ON sn.product_id = p.id " +
                    "LEFT JOIN production_batches pb ON sn.batch_id = pb.id WHERE 1=1 ");
                List<Object> params = new ArrayList<>();
                if (kw != null && !kw.isBlank()) {
                    sql.append("AND sn.serial_number LIKE ? "); params.add("%" + kw + "%");
                }
                if (st != null && !st.isBlank()) { sql.append("AND sn.status=? "); params.add(st); }
                if (selectedProduct != null) { sql.append("AND sn.product_id=? "); params.add(selectedProduct.getId()); }
                sql.append("ORDER BY sn.created_at DESC LIMIT 500");

                List<SerialRow> rows = new ArrayList<>();
                Connection conn = db.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Date mfgDate = rs.getDate("manufactured_date");
                            rows.add(new SerialRow(
                                rs.getString("serial_number"),
                                rs.getString("product_name"),
                                rs.getString("batch_number") != null ? rs.getString("batch_number") : "-",
                                mfgDate != null ? mfgDate.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "-",
                                rs.getString("status"),
                                rs.getString("notes") != null ? rs.getString("notes") : ""
                            ));
                        }
                    }
                } finally { db.releaseConnection(conn); }

                Platform.runLater(() -> {
                    productFilter.setItems(FXCollections.observableArrayList(products));
                    data.setAll(rows);
                    countLabel.setText(rows.size() + " serial numbers");
                });
            } catch (SQLException e) { Platform.runLater(() -> AlertUtil.showDatabaseError(e.getMessage())); }
        });
        t.setDaemon(true); t.start();
    }

    @FXML public void handleRefresh() { loadData(); }
    @FXML public void handleSearch() { loadData(); }
}
