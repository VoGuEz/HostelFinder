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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Hostel;
import models.Room;
import utils.SessionManager;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> locationFilter;
    @FXML private ComboBox<String> roomTypeFilter;
    @FXML private TextField maxPriceField;
    @FXML private VBox hostelListContainer;
    @FXML private Label resultCount;
    @FXML private Label userGreeting;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (SessionManager.getCurrentStudent() != null) {
            String firstName = SessionManager.getCurrentStudent().getFullName().split(" ")[0];
            userGreeting.setText("Hello, " + firstName + "!");
        }

        locationFilter.getItems().addAll("All Locations", "Accra", "Kumasi", "Cape Coast", "Legon", "East Legon", "Madina", "KNUST Area", "UCC Area");
        locationFilter.setValue("All Locations");

        roomTypeFilter.getItems().addAll("All Types", "Single", "Double", "Shared");
        roomTypeFilter.setValue("All Types");

        loadHostels("", "");
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText().trim();
        String location = locationFilter.getValue();
        String roomType = roomTypeFilter.getValue();
        String maxPriceStr = maxPriceField.getText().trim();

        double maxPrice = 0;
        if (!maxPriceStr.isEmpty()) {
            try { maxPrice = Double.parseDouble(maxPriceStr); }
            catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number for max price.");
                return;
            }
        }

        if (!roomType.equals("All Types") || maxPrice > 0) {
            // Search by rooms
            List<Room> rooms = DAO.searchAvailableRooms(
                    location.equals("All Locations") ? "" : location,
                    roomType.equals("All Types") ? "" : roomType,
                    0,
                    maxPrice
            );
            displayRooms(rooms);
        } else {
            loadHostels(location.equals("All Locations") ? "" : location, query);
        }
    }

    private void loadHostels(String location, String query) {
        List<Hostel> hostels = DAO.searchHostels(location, query,"rating");
        hostelListContainer.getChildren().clear();

        if (hostels.isEmpty()) {
            Label noResult = new Label("😔 No hostels found. Try different search terms.");
            noResult.setStyle("-fx-font-size: 15px; -fx-text-fill: #718096; -fx-padding: 40;");
            hostelListContainer.getChildren().add(noResult);
            resultCount.setText("");
            return;
        }

        resultCount.setText("(" + hostels.size() + " found)");

        for (Hostel hostel : hostels) {
            hostelListContainer.getChildren().add(createHostelCard(hostel));
        }
    }

    private void displayRooms(List<Room> rooms) {
        hostelListContainer.getChildren().clear();

        if (rooms.isEmpty()) {
            Label noResult = new Label("😔 No available rooms match your criteria.");
            noResult.setStyle("-fx-font-size: 15px; -fx-text-fill: #718096; -fx-padding: 40;");
            hostelListContainer.getChildren().add(noResult);
            resultCount.setText("");
            return;
        }

        resultCount.setText("(" + rooms.size() + " rooms available)");

        for (Room room : rooms) {
            hostelListContainer.getChildren().add(createRoomCard(room));
        }
    }

    private HBox createHostelCard(Hostel hostel) {
        HBox card = new HBox();
        card.getStyleClass().add("hostel-card");
        card.setSpacing(20);
        card.setPadding(new Insets(20));

        // Icon placeholder
        VBox iconBox = new VBox();
        iconBox.setAlignment(javafx.geometry.Pos.CENTER);
        iconBox.setPrefWidth(80);
        iconBox.setPrefHeight(80);
        iconBox.setStyle("-fx-background-color: #EBF8FF; -fx-background-radius: 12; -fx-min-width: 80; -fx-min-height: 80;");
        Label icon = new Label("🏨");
        icon.setStyle("-fx-font-size: 36px;");
        iconBox.getChildren().add(icon);

        // Info
        VBox info = new VBox(6);
        info.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(hostel.getName());
        nameLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");

        HBox locationBox = new HBox(5);
        Label locIcon = new Label("📍");
        locIcon.setStyle("-fx-font-size: 12px;");
        Label locLabel = new Label(hostel.getLocation());
        locLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        locationBox.getChildren().addAll(locIcon, locLabel);

        Label rating = new Label("Rating: " + hostel.getRating() + " / 5.0");
        rating.setStyle("-fx-font-size: 13px; -fx-text-fill: #D69E2E;");

        // Amenities chips
        HBox amenitiesBox = new HBox(8);
        if (hostel.getAmenities() != null) {
            String[] amenities = hostel.getAmenities().split(",");
            int count = 0;
            for (String a : amenities) {
                if (count++ >= 4) break;
                Label chip = new Label(a.trim());
                chip.setStyle("-fx-background-color: #F0FFF4; -fx-text-fill: #276749; -fx-background-radius: 20; -fx-padding: 2 10; -fx-font-size: 11px;");
                amenitiesBox.getChildren().add(chip);
            }
        }

        info.getChildren().addAll(nameLabel, locationBox, rating, amenitiesBox);

        // Actions
        VBox actions = new VBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER);

        Button viewBtn = new Button("View Rooms");
        viewBtn.setStyle("-fx-background-color: #3182CE; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 10 22; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> showHostelRooms(hostel));

        actions.getChildren().add(viewBtn);
        card.getChildren().addAll(iconBox, info, actions);
        return card;
    }

    private HBox createRoomCard(Room room) {
        HBox card = new HBox();
        card.getStyleClass().add("hostel-card");
        card.setSpacing(20);
        card.setPadding(new Insets(20));

        VBox iconBox = new VBox();
        iconBox.setAlignment(javafx.geometry.Pos.CENTER);
        iconBox.setPrefWidth(80);
        iconBox.setPrefHeight(80);
        iconBox.setStyle("-fx-background-color: #FFF5F5; -fx-background-radius: 12; -fx-min-width: 80; -fx-min-height: 80;");
        Label icon = new Label(room.getRoomType().equals("Single") ? "🛏️" : room.getRoomType().equals("Double") ? "🛏🛏" : "🏠");
        icon.setStyle("-fx-font-size: 30px;");
        iconBox.getChildren().add(icon);

        VBox info = new VBox(6);
        info.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label hostelName = new Label(room.getHostelName());
        hostelName.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");

        HBox typeBox = new HBox(10);
        Label typeLabel = new Label("Room " + room.getRoomNumber() + " • " + room.getRoomType());
        typeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        Label capacityLabel = new Label("👥 Capacity: " + room.getCapacity());
        capacityLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        typeBox.getChildren().addAll(typeLabel, capacityLabel);

        Label desc = new Label(room.getDescription() != null ? room.getDescription() : "");
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #A0AEC0;");
        desc.setWrapText(true);

        Label price = new Label(room.getFormattedPrice());
        price.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2F855A;");

        info.getChildren().addAll(hostelName, typeBox, desc, price);

        VBox actions = new VBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER);

        Button bookBtn = new Button("Book Now");
        bookBtn.setStyle("-fx-background-color: #38A169; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 10 22; -fx-cursor: hand;");
        bookBtn.setOnAction(e -> showBookingDialog(room));

        actions.getChildren().add(bookBtn);
        card.getChildren().addAll(iconBox, info, actions);
        return card;
    }

    private void showHostelRooms(Hostel hostel) {
        List<Room> rooms = DAO.getRoomsByHostel(hostel.getId());

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(hostel.getName() + " — Rooms");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #F7FAFC;");

        Label title = new Label("🏠  " + hostel.getName());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");

        Label locationLabel = new Label("📍 " + hostel.getLocation() + "   " + hostel.getStarRating());
        locationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");

        Label desc = new Label(hostel.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #4A5568;");

        Label roomsTitle = new Label("Available Rooms");
        roomsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");

        VBox roomsList = new VBox(10);
        for (Room room : rooms) {
            HBox roomRow = new HBox(16);
            roomRow.setPadding(new Insets(14));
            roomRow.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");
            roomRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            VBox roomInfo = new VBox(4);
            HBox.setHgrow(roomInfo, Priority.ALWAYS);
            Label rNum = new Label("Room " + room.getRoomNumber() + " — " + room.getRoomType());
            rNum.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
            Label rDesc = new Label(room.getDescription() != null ? room.getDescription() : "");
            rDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
            roomInfo.getChildren().addAll(rNum, rDesc);

            Label priceLabel = new Label(room.getFormattedPrice());
            priceLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2F855A;");

            Label availLabel = new Label(room.isAvailable() ? "✅ Available" : "❌ Taken");
            availLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (room.isAvailable() ? "#38A169" : "#E53E3E") + ";");

            Button bookBtn = new Button("Book");
            bookBtn.setStyle("-fx-background-color: " + (room.isAvailable() ? "#3182CE" : "#CBD5E0") + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 7 18; -fx-cursor: hand;");
            bookBtn.setDisable(!room.isAvailable());
            bookBtn.setOnAction(e -> {
                dialog.close();
                showBookingDialog(room);
            });

            roomRow.getChildren().addAll(roomInfo, priceLabel, availLabel, bookBtn);
            roomsList.getChildren().add(roomRow);
        }

        ScrollPane scrollPane = new ScrollPane(roomsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefHeight(320);

        root.getChildren().addAll(title, locationLabel, desc, roomsTitle, scrollPane);

        Scene scene = new Scene(root, 640, 520);
        scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    private void showBookingDialog(Room room) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Book Room " + room.getRoomNumber());

        VBox root = new VBox(18);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #F7FAFC;");
        root.setPrefWidth(440);

        Label title = new Label("📅  Book Your Room");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");

        Label roomInfo = new Label(room.getHostelName() + " — Room " + room.getRoomNumber() + " (" + room.getRoomType() + ")");
        roomInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #4A5568;");

        Label priceLabel = new Label(room.getFormattedPrice());
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2F855A;");

        // Check-in
        VBox checkInBox = new VBox(6);
        Label checkInLabel = new Label("Check-in Date");
        checkInLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4A5568;");
        DatePicker checkInPicker = new DatePicker(LocalDate.now());
        checkInPicker.setPrefWidth(380);
        checkInBox.getChildren().addAll(checkInLabel, checkInPicker);

        // Check-out
        VBox checkOutBox = new VBox(6);
        Label checkOutLabel = new Label("Check-out Date");
        checkOutLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4A5568;");
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusMonths(1));
        checkOutPicker.setPrefWidth(380);
        checkOutBox.getChildren().addAll(checkOutLabel, checkOutPicker);

        Label totalLabel = new Label();
        totalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");

        Runnable updateTotal = () -> {
            if (checkInPicker.getValue() != null && checkOutPicker.getValue() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(checkInPicker.getValue(), checkOutPicker.getValue());
                double months = days / 30.0;
                double total = months * room.getPricePerMonth();
                totalLabel.setText(String.format("Total: GH₵ %.2f (%.1f months)", total, months));
            }
        };

        checkInPicker.valueProperty().addListener((obs, o, n) -> updateTotal.run());
        checkOutPicker.valueProperty().addListener((obs, o, n) -> updateTotal.run());
        updateTotal.run();

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #E53E3E; -fx-font-size: 12px;");

        Button confirmBtn = new Button("✅  Confirm Booking");
        confirmBtn.setStyle("-fx-background-color: #38A169; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12 30; -fx-cursor: hand;");
        confirmBtn.setPrefWidth(380);
        confirmBtn.setOnAction(e -> {
            LocalDate checkIn = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();

            if (checkIn == null || checkOut == null) {
                errorLbl.setText("Please select both dates.");
                return;
            }
            if (!checkOut.isAfter(checkIn)) {
                errorLbl.setText("Check-out must be after check-in.");
                return;
            }

            long days = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            double total = (days / 30.0) * room.getPricePerMonth();

            boolean success = DAO.bookRoom(
                    SessionManager.getCurrentStudent().getId(),
                    room.getId(),
                    room.getHostelId(),
                    checkIn, checkOut, total,
                    "pending"
            );

            if (success) {
                dialog.close();
                showAlert("Booking Confirmed! 🎉", "Your room has been successfully booked!\n\n" +
                        "Hostel: " + room.getHostelName() + "\nRoom: " + room.getRoomNumber() +
                        "\nCheck-in: " + checkIn + "\nCheck-out: " + checkOut +
                        "\nTotal: GH₵ " + String.format("%.2f", total));
                handleSearch(); // Refresh list
            } else {
                errorLbl.setText("Booking failed. Please try again.");
            }
        });

        root.getChildren().addAll(title, roomInfo, priceLabel, checkInBox, checkOutBox, totalLabel, errorLbl, confirmBtn);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    @FXML
    public void showSearch() {}

    @FXML
    public void showBookings() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Bookings.fxml"));
            Scene scene = new Scene(root, 1000, 680);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            Stage stage = (Stage) hostelListContainer.getScene().getWindow();
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
            Stage stage = (Stage) hostelListContainer.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
