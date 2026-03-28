package controllers;

import database.DAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import security.PasswordUtils;
import utils.EmailService;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField universityField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button registerBtn;

    // OTP verification panel
    @FXML private VBox registerForm;
    @FXML private VBox otpForm;
    @FXML private TextField otpField;
    @FXML private Label otpEmailLabel;

    private String pendingEmail;
    private String pendingName;

    @FXML
    public void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String university = universityField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Name, email, and password are required."); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address."); return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match."); return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters."); return;
        }
        if (DAO.emailExists(email)) {
            showError("This email is already registered."); return;
        }

        registerBtn.setDisable(true);
        registerBtn.setText("Creating account...");

        String otp = PasswordUtils.generateOTP();
        boolean saved = DAO.registerStudent(name, email, password, phone, university, otp);

        if (saved) {
            pendingEmail = email;
            pendingName = name;

            // Try to send OTP email
            boolean emailSent = EmailService.sendVerificationEmail(email, name, otp);

            if (emailSent) {
                showOTPForm("We sent a verification code to " + email);
            } else {
                // If email fails, show OTP in console for testing and auto-verify
                System.out.println("⚠️ Email not configured. OTP for testing: " + otp);
                showOTPForm("Email not configured. Check console for OTP (for testing).");
            }
        } else {
            showError("Registration failed. Please try again.");
            registerBtn.setDisable(false);
            registerBtn.setText("Create Account");
        }
    }

    private void showOTPForm(String message) {
        registerForm.setVisible(false);
        registerForm.setManaged(false);
        otpForm.setVisible(true);
        otpForm.setManaged(true);
        otpEmailLabel.setText(message);
    }

    @FXML
    public void handleVerifyOTP() {
        String otp = otpField.getText().trim();
        if (otp.isEmpty()) {
            showError("Please enter the OTP code."); return;
        }

        boolean verified = DAO.verifyEmail(pendingEmail, otp);
        if (verified) {
            successLabel.setText("✅ Email verified! Redirecting to login...");
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::goToLogin);
                } catch (InterruptedException ignored) {}
            }).start();
        } else {
            showError("Invalid or expired OTP. Please try again.");
        }
    }

    @FXML
    public void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Scene scene = new Scene(root, 1000, 680);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showError(String msg) {
        successLabel.setText("");
        errorLabel.setText(msg);
    }
}
