package com.nova.factoryerp;

import com.nova.factoryerp.config.AppConfig;
import com.nova.factoryerp.database.DatabaseConnection;
import com.nova.factoryerp.database.DatabaseSeeder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class Main extends Application {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        // 1. Test DB connection
        try {
            DatabaseConnection.getInstance().testConnection();
        } catch (SQLException e) {
            showFatalError("Database Connection Failed", e.getMessage());
            Platform.exit();
            return;
        }

        // 2. Auto-fix seed passwords if needed (handles incorrect BCrypt in seed.sql)
        DatabaseSeeder.ensureAdminPassword();

        // 3. Launch Login screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 500);
            primaryStage.setTitle("Nova Factory ERP — Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.setOnCloseRequest(e -> {
                DatabaseConnection.getInstance().closeAll();
                Platform.exit();
            });
            primaryStage.show();
            log.info("Nova Factory ERP v{} started", AppConfig.getInstance().getAppVersion());
        } catch (Exception e) {
            log.error("Failed to start application", e);
            showFatalError("Startup Error", "Failed to load login screen:\n" + e.getMessage());
            Platform.exit();
        }
    }

    private void showFatalError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        DatabaseConnection.getInstance().closeAll();
        log.info("Application stopped");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
