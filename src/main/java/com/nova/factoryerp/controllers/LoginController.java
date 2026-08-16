package com.nova.factoryerp.controllers;

import com.nova.factoryerp.security.AuthService;
import com.nova.factoryerp.security.AuthService.LoginResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        errorLabel.setText("");
        usernameField.requestFocus();
        // Enter key on username moves to password
        usernameField.setOnAction(e -> passwordField.requestFocus());
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        errorLabel.setText("");

        if (username.isEmpty()) {
            showError("Username is required");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Password is required");
            passwordField.requestFocus();
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("LOGGING IN...");

        // Run in background thread to avoid freezing UI
        Thread loginThread = new Thread(() -> {
            LoginResult result = authService.login(username, password);
            Platform.runLater(() -> {
                loginButton.setDisable(false);
                loginButton.setText("LOG IN");
                handleLoginResult(result);
            });
        });
        loginThread.setDaemon(true);
        loginThread.start();
    }

    private void handleLoginResult(LoginResult result) {
        switch (result) {
            case SUCCESS -> openMainWindow();
            case INVALID_CREDENTIALS -> {
                showError("Invalid username or password");
                passwordField.clear();
                passwordField.requestFocus();
            }
            case ACCOUNT_DISABLED -> showError("Your account has been disabled.\nPlease contact administrator.");
            case DATABASE_ERROR -> showError("Database connection error.\nPlease check your configuration.");
        }
    }

 private void openMainWindow() {
    try {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/main.fxml"));

        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Nova Factory ERP");

        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);

        stage.setMinWidth(1024);
        stage.setMinHeight(680);

        // Open as a REAL maximized window.
        // This keeps the normal Windows title bar and
        // minimize / maximize / close buttons visible.
        stage.setMaximized(true);

        stage.show();

        // Close login window
        Stage loginStage =
            (Stage) loginButton.getScene().getWindow();

        loginStage.close();

        log.info("Main window opened");

    } catch (Exception e) {
        log.error("Failed to open main window", e);
        showError("Failed to open application. Check logs.");
    }
}

    private void showError(String msg) {
        errorLabel.setText(msg);
    }
}
