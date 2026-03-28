package controllers;

import database.DAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Student;
import utils.SessionManager;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        loginBtn.setDisable(true);
        loginBtn.setText("Signing in...");

        Student student = DAO.loginStudent(email, password);

        if (student != null) {
            if (!student.isVerified()) {
                showError("Please verify your email before logging in.");
                loginBtn.setDisable(false);
                loginBtn.setText("Sign In");
                return;
            }
            SessionManager.setCurrentStudent(student, student.getRole());

            // Route based on role
            String view = switch (student.getRole()) {
                case "ADMIN" -> "/views/AdminDashboard.fxml";
                case "OWNER" -> "/views/OwnerDashboard.fxml";
                default -> "/views/Dashboard.fxml";
            };
            loadScene(view);
        } else {
            showError("Invalid email or password. Please try again.");
            loginBtn.setDisable(false);
            loginBtn.setText("Sign In");
        }
    }

    @FXML
    public void goToRegister() {
        loadScene("/views/Register.fxml");
    }

    @FXML
    public void goToForgotPassword() {
        loadScene("/views/ForgotPassword.fxml");
    }

    private void loadScene(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Scene scene = new Scene(root, 1000, 680);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }
}
