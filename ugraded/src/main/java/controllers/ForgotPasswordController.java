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

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private TextField otpField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button sendOtpBtn;
    @FXML private Button resetBtn;
    @FXML private VBox emailStep;
    @FXML private VBox resetStep;

    private String pendingEmail;

    @FXML
    public void handleSendOTP() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) { showError("Please enter your email."); return; }
        if (!DAO.emailExists(email)) { showError("No account found with this email."); return; }

        sendOtpBtn.setDisable(true);
        sendOtpBtn.setText("Sending...");

        String otp = PasswordUtils.generateOTP();
        DAO.saveResetOTP(email, otp);

        boolean sent = EmailService.sendPasswordResetEmail(email, email, otp);
        pendingEmail = email;

        if (sent) {
            switchToResetStep("OTP sent to " + email);
        } else {
            System.out.println("⚠️ Email not configured. Reset OTP: " + otp);
            switchToResetStep("Check console for OTP (email not configured).");
        }
    }

    private void switchToResetStep(String message) {
        emailStep.setVisible(false);
        emailStep.setManaged(false);
        resetStep.setVisible(true);
        resetStep.setManaged(true);
        successLabel.setText(message);
    }

    @FXML
    public void handleResetPassword() {
        String otp = otpField.getText().trim();
        String newPass = newPasswordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (otp.isEmpty() || newPass.isEmpty()) { showError("Please fill all fields."); return; }
        if (!newPass.equals(confirm)) { showError("Passwords do not match."); return; }
        if (newPass.length() < 6) { showError("Password must be at least 6 characters."); return; }

        boolean success = DAO.resetPassword(pendingEmail, otp, newPass);
        if (success) {
            successLabel.setText("✅ Password reset! Redirecting to login...");
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::goToLogin);
                } catch (InterruptedException ignored) {}
            }).start();
        } else {
            showError("Invalid or expired OTP.");
        }
    }

    @FXML
    public void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Scene scene = new Scene(root, 1000, 680);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showError(String msg) {
        successLabel.setText("");
        errorLabel.setText(msg);
    }
}
