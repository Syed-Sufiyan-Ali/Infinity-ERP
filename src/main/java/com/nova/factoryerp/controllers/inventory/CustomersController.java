package com.nova.factoryerp.controllers.inventory;

import com.nova.factoryerp.dao.impl.AuditLogDAOImpl;
import com.nova.factoryerp.dao.impl.CustomerDAOImpl;
import com.nova.factoryerp.dao.interfaces.AuditLogDAO;
import com.nova.factoryerp.dao.interfaces.CustomerDAO;
import com.nova.factoryerp.models.AuditLog;
import com.nova.factoryerp.models.Customer;
import com.nova.factoryerp.utils.AlertUtil;
import com.nova.factoryerp.utils.NumberUtil;
import com.nova.factoryerp.utils.SessionManager;
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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CustomersController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label countLabel;

    @FXML private TableView<Customer> customersTable;

    @FXML private TableColumn<Customer, String> codeCol;
    @FXML private TableColumn<Customer, String> nameCol;
    @FXML private TableColumn<Customer, String> phoneCol;
    @FXML private TableColumn<Customer, String> emailCol;
    @FXML private TableColumn<Customer, String> cityCol;
    @FXML private TableColumn<Customer, String> balanceCol;
    @FXML private TableColumn<Customer, String> statusCol;
    @FXML private TableColumn<Customer, Void> actionsCol;

    private final CustomerDAO dao = new CustomerDAOImpl();
    private final AuditLogDAO auditDAO = new AuditLogDAOImpl();

    private final ObservableList<Customer> data =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        codeCol.setCellValueFactory(
            c -> new SimpleStringProperty(
                c.getValue().getCustomerCode()));

        nameCol.setCellValueFactory(
            c -> new SimpleStringProperty(
                c.getValue().getName()));

        phoneCol.setCellValueFactory(
            c -> new SimpleStringProperty(
                valueOrDash(c.getValue().getPhone())));

        emailCol.setCellValueFactory(
            c -> new SimpleStringProperty(
                valueOrDash(c.getValue().getEmail())));

        cityCol.setCellValueFactory(
            c -> new SimpleStringProperty(
                valueOrDash(c.getValue().getCity())));

        balanceCol.setCellValueFactory(
            c -> new SimpleStringProperty(
                NumberUtil.formatCurrency(
                    c.getValue().getCurrentBalance())));

        statusCol.setCellValueFactory(
            c -> new SimpleStringProperty(
                c.getValue().getStatus()));

        statusFilter.setItems(
            FXCollections.observableArrayList(
                "",
                "ACTIVE",
                "INACTIVE",
                "BLOCKED"
            )
        );

        customersTable.setItems(data);
codeCol.setMinWidth(110);
nameCol.setMinWidth(150);
phoneCol.setMinWidth(120);
emailCol.setMinWidth(180);
cityCol.setMinWidth(100);
balanceCol.setMinWidth(130);
statusCol.setMinWidth(100);
actionsCol.setMinWidth(280);
actionsCol.setPrefWidth(280);
customersTable.setFixedCellSize(42);
      actionsCol.setCellFactory(col ->
    new TableCell<>() {

        private final Button history =
                new Button("History");

        private final Button edit =
                new Button("Edit");

        private final Button deactivate =
                new Button("Deactivate");

        {
            history.getStyleClass().add("btn-secondary");
            edit.getStyleClass().add("btn-secondary");
            deactivate.getStyleClass().add("btn-danger");

            history.setStyle(
                    "-fx-padding:4 10 4 10;" +
                    "-fx-font-size:11px;"
            );

            edit.setStyle(
                    "-fx-padding:4 10 4 10;" +
                    "-fx-font-size:11px;"
            );

            deactivate.setStyle(
                    "-fx-padding:4 10 4 10;" +
                    "-fx-font-size:11px;"
            );

            history.setOnAction(e ->
                    showCustomerHistory(
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

            deactivate.setOnAction(e ->
                    handleDeactivate(
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

            super.updateItem(
                    item,
                    empty
            );

            if (empty) {

                setGraphic(null);

            } else {

                Customer customer =
                        getTableView()
                                .getItems()
                                .get(getIndex());

                deactivate.setDisable(
                        !"ACTIVE".equals(
                                customer.getStatus()
                        )
                );

               HBox actions = new HBox(8);

actions.setAlignment(
    javafx.geometry.Pos.CENTER_LEFT
);

history.setMinWidth(70);
history.setPrefWidth(70);

edit.setMinWidth(55);
edit.setPrefWidth(55);

deactivate.setMinWidth(90);
deactivate.setPrefWidth(90);

actions.getChildren().addAll(
    history,
    edit,
    deactivate
);

setGraphic(actions);
            }
        }
    }
);

        loadData();
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank()
                ? "-"
                : value;
    }

    private void loadData() {

        Thread thread = new Thread(() -> {

            try {

                String keyword =
                    searchField.getText();

                String status =
                    statusFilter.getValue();

                List<Customer> customers =
                    dao.search(keyword, status);

                Platform.runLater(() -> {

                    data.setAll(customers);

                    countLabel.setText(
                        customers.size() + " customers"
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

    private void showDialog(Customer existing) {

        boolean isNew = existing == null;

        Dialog<Customer> dialog =
            new Dialog<>();

        dialog.setTitle(
            isNew
                ? "Add Customer"
                : "Edit Customer"
        );

        dialog.getDialogPane()
            .getButtonTypes()
            .addAll(
                ButtonType.OK,
                ButtonType.CANCEL
            );

        dialog.getDialogPane()
            .getStylesheets()
            .add(
                getClass()
                    .getResource(
                        "/css/nova-erp.css"
                    )
                    .toExternalForm()
            );

        dialog.getDialogPane().setStyle(
            "-fx-background-color:#1a1e2a;"
        );

        GridPane grid = new GridPane();

        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField codeF =
            new TextField(
                isNew
                    ? ""
                    : existing.getCustomerCode()
            );

        TextField nameF =
            new TextField(
                isNew
                    ? ""
                    : existing.getName()
            );

        TextField phoneF =
            new TextField(
                isNew
                    ? ""
                    : valueOrEmpty(existing.getPhone())
            );

        TextField emailF =
            new TextField(
                isNew
                    ? ""
                    : valueOrEmpty(existing.getEmail())
            );

        TextField cityF =
            new TextField(
                isNew
                    ? ""
                    : valueOrEmpty(existing.getCity())
            );

        TextField openingBalanceF =
            new TextField(
                isNew
                    ? "0"
                    : existing.getOpeningBalance()
                        .toPlainString()
            );

        TextArea addressF =
            new TextArea(
                isNew
                    ? ""
                    : valueOrEmpty(existing.getAddress())
            );

        addressF.setPrefRowCount(3);

        ComboBox<String> statusCB =
            new ComboBox<>(
                FXCollections.observableArrayList(
                    "ACTIVE",
                    "INACTIVE",
                    "BLOCKED"
                )
            );

        statusCB.setValue(
            isNew
                ? "ACTIVE"
                : existing.getStatus()
        );

        String[] labels = {
            "Customer Code *",
            "Name *",
            "Phone",
            "Email",
            "Address",
            "City",
            "Opening Balance",
            "Status"
        };

        Node[] fields = {
            codeF,
            nameF,
            phoneF,
            emailF,
            addressF,
            cityF,
            openingBalanceF,
            statusCB
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

            if (codeF.getText().isBlank()
                    || nameF.getText().isBlank()) {

                AlertUtil.showWarning(
                    "Validation",
                    "Customer Code and Name are required."
                );

                return null;
            }

            Customer customer =
                isNew
                    ? new Customer()
                    : existing;

            customer.setCustomerCode(
                codeF.getText().trim()
            );

            customer.setName(
                nameF.getText().trim()
            );

            customer.setPhone(
                valueOrNull(phoneF.getText())
            );

            customer.setEmail(
                valueOrNull(emailF.getText())
            );

            customer.setAddress(
                valueOrNull(addressF.getText())
            );

            customer.setCity(
                valueOrNull(cityF.getText())
            );

            BigDecimal opening =
                NumberUtil.parseBigDecimal(
                    openingBalanceF.getText()
                );

            customer.setOpeningBalance(
                opening != null
                    ? opening
                    : BigDecimal.ZERO
            );

            customer.setStatus(
                statusCB.getValue()
            );

            if (isNew) {
                customer.setCurrentBalance(
                    customer.getOpeningBalance()
                );
            }

            return customer;
        });

        Optional<Customer> result =
            dialog.showAndWait();

        result.ifPresent(customer -> {

            try {

                if (isNew) {

                    if (dao.findByCode(
                            customer.getCustomerCode()
                        ).isPresent()) {

                        AlertUtil.showError(
                            "Duplicate",
                            "Customer code already exists: "
                                + customer.getCustomerCode()
                        );

                        return;
                    }

                    dao.save(customer);

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

                            "Created customer: "
                                + customer.getName()
                        )
                    );

                } else {

                    dao.update(customer);

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

                            "Updated customer: "
                                + customer.getName()
                        )
                    );
                }

                loadData();

            } catch (SQLException e) {

                AlertUtil.showDatabaseError(
                    e.getMessage()
                );
            }
        });
    }

    private void handleDeactivate(Customer customer) {

        if (!AlertUtil.showConfirm(
                "Deactivate Customer",
                "Deactivate customer: "
                    + customer.getName()
                    + "?"
            )) {

            return;
        }

        try {

            dao.deactivate(
                customer.getId()
            );

            auditDAO.log(
                new AuditLog(
                    SessionManager
                        .getInstance()
                        .getCurrentUserId(),

                    SessionManager
                        .getInstance()
                        .getCurrentUsername(),

                    "DELETE",
                    "SALES",

                    "Deactivated customer: "
                        + customer.getName()
                )
            );

            loadData();

        } catch (SQLException e) {

            AlertUtil.showDatabaseError(
                e.getMessage()
            );
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String valueOrNull(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
    private void showCustomerHistory(
        Customer customer
) {

    try {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/fxml/inventory/CustomerHistoryView.fxml"
                        )
                );

        javafx.scene.Parent root =
                loader.load();

        CustomerHistoryController controller =
                loader.getController();

        controller.setCustomer(
                customer
        );

        Stage stage =
                new Stage();

        stage.setTitle(
                "Customer History - "
                        + customer.getName()
        );

        stage.initModality(
                Modality.APPLICATION_MODAL
        );

        stage.setMinWidth(900);
        stage.setMinHeight(650);

        stage.setWidth(1050);
        stage.setHeight(750);

        Scene scene =
                new Scene(
                        root
                );

        stage.setScene(scene);

        stage.showAndWait();

    } catch (Exception e) {

        e.printStackTrace();

        AlertUtil.showError(
                "Customer History",
                "Unable to open customer history:\n"
                        + e.getMessage()
        );
    }
}
}