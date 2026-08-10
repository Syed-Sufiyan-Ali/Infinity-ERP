package com.nova.factoryerp.controllers.inventory;

import com.nova.factoryerp.dao.impl.AuditLogDAOImpl;
import com.nova.factoryerp.dao.impl.SupplierDAOImpl;
import com.nova.factoryerp.dao.interfaces.AuditLogDAO;
import com.nova.factoryerp.dao.interfaces.SupplierDAO;
import com.nova.factoryerp.models.AuditLog;
import com.nova.factoryerp.models.Supplier;
import com.nova.factoryerp.utils.AlertUtil;
import com.nova.factoryerp.utils.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SuppliersController {
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private TableView<Supplier> suppliersTable;
    @FXML private TableColumn<Supplier, String> codeCol;
    @FXML private TableColumn<Supplier, String> nameCol;
    @FXML private TableColumn<Supplier, String> contactCol;
    @FXML private TableColumn<Supplier, String> phoneCol;
    @FXML private TableColumn<Supplier, String> emailCol;
    @FXML private TableColumn<Supplier, String> cityCol;
    @FXML private TableColumn<Supplier, String> statusCol;
    @FXML private TableColumn<Supplier, Void> actionsCol;

    private final SupplierDAO dao = new SupplierDAOImpl();
    private final AuditLogDAO auditDAO = new AuditLogDAOImpl();
    private final ObservableList<Supplier> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        contactCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getContactName() != null ? c.getValue().getContactName() : "-"));
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getPhone() != null ? c.getValue().getPhone() : "-"));
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getEmail() != null ? c.getValue().getEmail() : "-"));
        cityCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getCity() != null ? c.getValue().getCity() : "-"));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Active" : "Inactive"));

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
        suppliersTable.setItems(data);
        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                String kw = searchField.getText();
                List<Supplier> list = kw == null || kw.isBlank() ? dao.findAll() : dao.search(kw);
                Platform.runLater(() -> { data.setAll(list); countLabel.setText(list.size() + " suppliers"); });
            } catch (SQLException e) { Platform.runLater(() -> AlertUtil.showDatabaseError(e.getMessage())); }
        });
        t.setDaemon(true); t.start();
    }

    @FXML public void handleRefresh() { loadData(); }
    @FXML public void handleSearch() { loadData(); }
    @FXML public void handleAdd() { showDialog(null); }

    private void showDialog(Supplier existing) {
        boolean isNew = existing == null;
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Add Supplier" : "Edit Supplier");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/nova-erp.css").toExternalForm());
        dialog.getDialogPane().setStyle("-fx-background-color:#1a1e2a;");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField codeF    = new TextField(isNew ? "" : existing.getCode());
        TextField nameF    = new TextField(isNew ? "" : existing.getName());
        TextField contactF = new TextField(isNew ? "" : (existing.getContactName() != null ? existing.getContactName() : ""));
        TextField phoneF   = new TextField(isNew ? "" : (existing.getPhone() != null ? existing.getPhone() : ""));
        TextField emailF   = new TextField(isNew ? "" : (existing.getEmail() != null ? existing.getEmail() : ""));
        TextField addressF = new TextField(isNew ? "" : (existing.getAddress() != null ? existing.getAddress() : ""));
        TextField cityF    = new TextField(isNew ? "" : (existing.getCity() != null ? existing.getCity() : ""));
        ComboBox<String> statusCB = new ComboBox<>(FXCollections.observableArrayList("Active","Inactive"));
        statusCB.setValue(isNew ? "Active" : (existing.isActive() ? "Active" : "Inactive"));

        String[] labels = {"Code *","Name *","Contact Person","Phone","Email","Address","City","Status"};
        javafx.scene.Node[] fields = {codeF,nameF,contactF,phoneF,emailF,addressF,cityF,statusCB};
        for (int i = 0; i < labels.length; i++) {
            Label l = new Label(labels[i]); l.setStyle("-fx-text-fill:#9199b0;-fx-font-size:12px;");
            grid.add(l, 0, i); grid.add(fields[i], 1, i);
            GridPane.setHgrow(fields[i], Priority.ALWAYS);
        }
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (codeF.getText().isBlank() || nameF.getText().isBlank()) {
                    AlertUtil.showWarning("Validation","Code and Name are required."); return null;
                }
                Supplier s = isNew ? new Supplier() : existing;
                s.setCode(codeF.getText().trim()); s.setName(nameF.getText().trim());
                s.setContactName(contactF.getText().trim()); s.setPhone(phoneF.getText().trim());
                s.setEmail(emailF.getText().trim()); s.setAddress(addressF.getText().trim());
                s.setCity(cityF.getText().trim()); s.setActive("Active".equals(statusCB.getValue()));
                return s;
            }
            return null;
        });

        Optional<Supplier> result = dialog.showAndWait();
        result.ifPresent(s -> {
            try {
                if (isNew) { dao.save(s); auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                    SessionManager.getInstance().getCurrentUsername(),"CREATE","INVENTORY","Created supplier: "+s.getName())); }
                else { dao.update(s); auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                    SessionManager.getInstance().getCurrentUsername(),"UPDATE","INVENTORY","Updated supplier: "+s.getName())); }
                loadData();
            } catch (SQLException e) { AlertUtil.showDatabaseError(e.getMessage()); }
        });
    }

    private void handleDelete(Supplier s) {
        if (!AlertUtil.showConfirm("Deactivate","Deactivate supplier: " + s.getName() + "?")) return;
        try {
            dao.delete(s.getId());
            auditDAO.log(new AuditLog(SessionManager.getInstance().getCurrentUserId(),
                SessionManager.getInstance().getCurrentUsername(),"DELETE","INVENTORY","Deactivated supplier: "+s.getName()));
            loadData();
        } catch (SQLException e) { AlertUtil.showDatabaseError(e.getMessage()); }
    }
}
