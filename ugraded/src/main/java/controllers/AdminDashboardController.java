package controllers;

import database.DAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.*;
import utils.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label totalStudentsLabel;
    @FXML private Label totalHostelsLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private VBox contentArea;
    @FXML private Label userGreeting;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (SessionManager.getCurrentStudent() != null)
            userGreeting.setText("Admin: " + SessionManager.getCurrentStudent().getFirstName());
        loadStats();
        showStudents();
    }

    private void loadStats() {
        totalStudentsLabel.setText(String.valueOf(DAO.getTotalStudents()));
        totalHostelsLabel.setText(String.valueOf(DAO.getTotalHostels()));
        totalBookingsLabel.setText(String.valueOf(DAO.getTotalBookings()));
        totalRevenueLabel.setText(String.format("GH₵ %.0f", DAO.getTotalRevenue()));
    }

    @FXML
    public void showStudents() {
        contentArea.getChildren().clear();
        Label title = new Label("👥  All Students");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A202C; -fx-padding: 0 0 10 0;");
        contentArea.getChildren().add(title);

        List<Student> students = DAO.getAllStudents();
        for (Student s : students) {
            HBox row = new HBox(20);
            row.setPadding(new Insets(14));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label avatar = new Label("👤");
            avatar.setStyle("-fx-font-size: 24px; -fx-background-color: #EBF8FF; -fx-background-radius: 50; -fx-padding: 8;");

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label name = new Label(s.getFullName());
            name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
            Label email = new Label(s.getEmail() + "  |  " + (s.getUniversity() != null ? s.getUniversity() : "N/A"));
            email.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
            info.getChildren().addAll(name, email);

            Label verified = new Label(s.isVerified() ? "✅ Verified" : "⚠️ Unverified");
            verified.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (s.isVerified() ? "#38A169" : "#D69E2E") + ";");

            row.getChildren().addAll(avatar, info, verified);
            contentArea.getChildren().add(row);
        }
    }

    @FXML
    public void showAllBookings() {
        contentArea.getChildren().clear();
        Label title = new Label("📋  All Bookings");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A202C; -fx-padding: 0 0 10 0;");
        contentArea.getChildren().add(title);

        List<Booking> bookings = DAO.getAllBookings();
        for (Booking b : bookings) {
            HBox row = new HBox(20);
            row.setPadding(new Insets(14));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label hostel = new Label(b.getHostelName() + " — Room " + b.getRoomNumber());
            hostel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
            Label dates = new Label("📅 " + b.getCheckInDate() + " → " + b.getCheckOutDate());
            dates.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
            info.getChildren().addAll(hostel, dates);

            Label price = new Label(b.getFormattedPrice());
            price.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2F855A;");

            String statusColor = b.getStatus().equals("CONFIRMED") ? "#C6F6D5; -fx-text-fill: #276749" : "#FED7D7; -fx-text-fill: #9B2C2C";
            Label status = new Label(b.getStatus());
            status.setStyle("-fx-background-color: " + statusColor + "; -fx-background-radius: 20; -fx-padding: 3 12; -fx-font-size: 11px; -fx-font-weight: bold;");

            row.getChildren().addAll(info, price, status);
            contentArea.getChildren().add(row);
        }
    }

    @FXML
    public void showAllHostels() {
        contentArea.getChildren().clear();
        Label title = new Label("🏨  All Hostels");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A202C; -fx-padding: 0 0 10 0;");
        contentArea.getChildren().add(title);

        List<Hostel> hostels = DAO.getAllHostels();
        for (Hostel h : hostels) {
            HBox row = new HBox(20);
            row.setPadding(new Insets(14));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label icon = new Label("🏨");
            icon.setStyle("-fx-font-size: 28px; -fx-background-color: #EBF8FF; -fx-background-radius: 8; -fx-padding: 8;");

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label name = new Label(h.getName());
            name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
            Label loc = new Label("📍 " + h.getLocation() + "   " + h.getStarRating() + " (" + h.getTotalReviews() + " reviews)");
            loc.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
            info.getChildren().addAll(name, loc);

            Button removeBtn = new Button("Remove");
            removeBtn.setStyle("-fx-background-color: #FED7D7; -fx-text-fill: #9B2C2C; -fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                DAO.deleteHostel(h.getId());
                showAllHostels();
            });

            row.getChildren().addAll(icon, info, removeBtn);
            contentArea.getChildren().add(row);
        }
    }

    @FXML
    public void handleLogout() {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Scene scene = new Scene(root, 1000, 680);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
