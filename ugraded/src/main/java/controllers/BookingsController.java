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
import models.Booking;
import utils.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BookingsController implements Initializable {

    @FXML private VBox bookingsContainer;
    @FXML private Label userGreeting;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (SessionManager.getCurrentStudent() != null) {
            String firstName = SessionManager.getCurrentStudent().getFullName().split(" ")[0];
            userGreeting.setText("Hello, " + firstName + " 👋");
        }
        loadBookings();
    }

    private void loadBookings() {
        bookingsContainer.getChildren().clear();
        List<Booking> bookings = DAO.getStudentBookings(SessionManager.getCurrentStudent().getId());

        if (bookings.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            empty.setPadding(new Insets(60));
            Label icon = new Label("📋");
            icon.setStyle("-fx-font-size: 48px;");
            Label msg = new Label("No bookings yet");
            msg.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
            Label sub = new Label("Search for available rooms and make your first booking!");
            sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
            empty.getChildren().addAll(icon, msg, sub);
            bookingsContainer.getChildren().add(empty);
            return;
        }

        for (Booking booking : bookings) {
            bookingsContainer.getChildren().add(createBookingCard(booking));
        }
    }

    private HBox createBookingCard(Booking booking) {
        HBox card = new HBox(20);
        card.getStyleClass().add("hostel-card");
        card.setPadding(new Insets(20));
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Status icon
        VBox iconBox = new VBox();
        iconBox.setAlignment(javafx.geometry.Pos.CENTER);
        iconBox.setPrefWidth(70);
        iconBox.setPrefHeight(70);
        String bgColor = booking.getStatus().equals("CONFIRMED") ? "#F0FFF4" :
                booking.getStatus().equals("CANCELLED") ? "#FFF5F5" : "#FFFAF0";
        iconBox.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12;");
        Label icon = new Label(booking.getStatus().equals("CONFIRMED") ? "✅" :
                booking.getStatus().equals("CANCELLED") ? "❌" : "⏳");
        icon.setStyle("-fx-font-size: 28px;");
        iconBox.getChildren().add(icon);

        // Info
        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label hostelName = new Label(booking.getHostelName());
        hostelName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");

        Label roomInfo = new Label("Room " + booking.getRoomNumber() + " (" + booking.getRoomType() + ")");
        roomInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");

        Label dates = new Label("📅 " + booking.getCheckInDate() + "  →  " + booking.getCheckOutDate());
        dates.setStyle("-fx-font-size: 13px; -fx-text-fill: #4A5568;");

        Label price = new Label(booking.getFormattedPrice());
        price.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2F855A;");

        // Status badge
        String statusColor = booking.getStatus().equals("CONFIRMED") ? "#C6F6D5; -fx-text-fill: #276749" :
                booking.getStatus().equals("CANCELLED") ? "#FED7D7; -fx-text-fill: #9B2C2C" :
                        "#FEFCBF; -fx-text-fill: #744210";
        Label statusLabel = new Label(booking.getStatus());
        statusLabel.setStyle("-fx-background-color: " + statusColor + "; -fx-background-radius: 20; -fx-padding: 3 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        info.getChildren().addAll(hostelName, roomInfo, dates, price, statusLabel);

        // Cancel button
        VBox actions = new VBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER);

        if (booking.getStatus().equals("CONFIRMED")) {
            Button cancelBtn = new Button("Cancel");
            cancelBtn.setStyle("-fx-background-color: #FED7D7; -fx-text-fill: #9B2C2C; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand; -fx-font-weight: bold;");
            cancelBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Cancel Booking");
                confirm.setHeaderText("Are you sure?");
                confirm.setContentText("This will cancel your booking and make the room available again.");
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    DAO.cancelBooking(booking.getId(), booking.getRoomId());
                    loadBookings();
                }
            });
            actions.getChildren().add(cancelBtn);
        }

        card.getChildren().addAll(iconBox, info, actions);
        return card;
    }

    @FXML
    public void showBookings() {}

    @FXML
    public void goToSearch() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Dashboard.fxml"));
            Scene scene = new Scene(root, 1000, 680);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            Stage stage = (Stage) bookingsContainer.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Scene scene = new Scene(root, 1000, 680);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            Stage stage = (Stage) bookingsContainer.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
