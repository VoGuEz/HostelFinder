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
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Booking;
import models.Hostel;
import models.Room;
import utils.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class OwnerDashboardController implements Initializable {

    @FXML private VBox contentArea;
    @FXML private Label userGreeting;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (SessionManager.getCurrentStudent() != null)
            userGreeting.setText("Hello, " + SessionManager.getCurrentStudent().getFirstName() + " 👋");
        showMyHostels();
    }

    @FXML
    public void showMyHostels() {
        contentArea.getChildren().clear();
        VBox header = new VBox(4);
        Label title = new Label("🏨  My Hostels");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");
        Label subtitle = new Label("Manage your hostel listings and rooms");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        header.getChildren().addAll(title, subtitle);
        contentArea.getChildren().add(header);
        loadMyHostels();
    }

    private void loadMyHostels() {
        // Remove all children below the header (index 0 is the header)
        if (contentArea.getChildren().size() > 1)
            contentArea.getChildren().remove(1, contentArea.getChildren().size());

        int ownerId = SessionManager.getCurrentStudent().getId();
        List<Hostel> hostels = DAO.getOwnerHostels(ownerId);

        if (hostels.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            empty.setPadding(new Insets(40));
            Label msg = new Label("No hostels yet. Add your first hostel!");
            msg.setStyle("-fx-font-size: 15px; -fx-text-fill: #718096;");
            empty.getChildren().add(msg);
            contentArea.getChildren().add(empty);
            return;
        }

        for (Hostel hostel : hostels) {
            contentArea.getChildren().add(createOwnerHostelCard(hostel));
        }
    }

    private VBox createOwnerHostelCard(Hostel hostel) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.07),10,0,0,3); -fx-border-color: #E2E8F0; -fx-border-radius: 14; -fx-border-width: 1;");

        HBox header = new HBox(16);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label icon = new Label("🏨");
        icon.setStyle("-fx-font-size: 32px; -fx-background-color: #EBF8FF; -fx-background-radius: 10; -fx-padding: 10;");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(hostel.getName());
        name.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");
        Label loc = new Label("📍 " + hostel.getLocation() + "   " + hostel.getStarRating() + " (" + hostel.getTotalReviews() + " reviews)");
        loc.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        info.getChildren().addAll(name, loc);

        HBox actions = new HBox(10);
        Button editBtn = new Button("✏️ Edit");
        editBtn.setStyle("-fx-background-color: #EBF8FF; -fx-text-fill: #2B6CB0; -fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> showEditHostelDialog(hostel));

        Button addRoomBtn = new Button("➕ Add Room");
        addRoomBtn.setStyle("-fx-background-color: #F0FFF4; -fx-text-fill: #276749; -fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand; -fx-font-weight: bold;");
        addRoomBtn.setOnAction(e -> showAddRoomDialog(hostel));

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setStyle("-fx-background-color: #FFF5F5; -fx-text-fill: #9B2C2C; -fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Hostel");
            confirm.setHeaderText("Are you sure you want to delete " + hostel.getName() + "?");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) { DAO.deleteHostel(hostel.getId()); loadMyHostels(); }
            });
        });

        actions.getChildren().addAll(editBtn, addRoomBtn, deleteBtn);
        header.getChildren().addAll(icon, info, actions);

        // Rooms list
        List<Room> rooms = DAO.getRoomsByHostel(hostel.getId());
        long freeSpaces = rooms.stream().filter(Room::isAvailable).count();
        Label roomsTitle = new Label("Rooms (" + rooms.size() + ")   🟢 Free spaces: " + freeSpaces + "/" + rooms.size());
        roomsTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4A5568;");

        HBox roomsBox = new HBox(10);
        for (Room r : rooms) {
            VBox roomChip = new VBox(4);
            roomChip.setPadding(new Insets(10));
            roomChip.setStyle("-fx-background-color: " + (r.isAvailable() ? "#F0FFF4" : "#FFF5F5") +
                "; -fx-background-radius: 8; -fx-min-width: 120;");
            Label rNum = new Label("Room " + r.getRoomNumber());
            rNum.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
            Label rType = new Label(r.getRoomType() + " • " + r.getFormattedPrice());
            rType.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
            Label rAvail = new Label(r.isAvailable() ? "✅ Available" : "❌ Occupied");
            rAvail.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (r.isAvailable() ? "#38A169" : "#E53E3E") + ";");

            Button delRoom = new Button("Remove");
            delRoom.setStyle("-fx-background-color: transparent; -fx-text-fill: #E53E3E; -fx-font-size: 10px; -fx-padding: 2 6; -fx-cursor: hand; -fx-border-color: transparent;");
            delRoom.setOnAction(e -> { DAO.deleteRoom(r.getId()); loadMyHostels(); });

            roomChip.getChildren().addAll(rNum, rType, rAvail, delRoom);
            roomsBox.getChildren().add(roomChip);
        }

        ScrollPane roomsScroll = new ScrollPane(roomsBox);
        roomsScroll.setFitToHeight(true);
        roomsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        roomsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        roomsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        roomsScroll.setPrefHeight(120);

        card.getChildren().addAll(header, roomsTitle, roomsScroll);
        return card;
    }

    private void showEditHostelDialog(Hostel hostel) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Hostel");
        VBox root = buildHostelForm(hostel, dialog);
        dialog.setScene(new Scene(root));
        dialog.show();
    }

    @FXML
    public void showAddHostelDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Hostel");
        VBox root = buildHostelForm(null, dialog);
        dialog.setScene(new Scene(root));
        dialog.show();
    }

    private VBox buildHostelForm(Hostel hostel, Stage dialog) {
        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #F7FAFC;");
        root.setPrefWidth(460);

        Label title = new Label(hostel == null ? "➕  Add New Hostel" : "✏️  Edit Hostel");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");

        TextField nameF = new TextField(hostel != null ? hostel.getName() : "");
        nameF.setPromptText("Hostel Name"); nameF.setStyle(fieldStyle());

        TextField locationF = new TextField(hostel != null ? hostel.getLocation() : "");
        locationF.setPromptText("Location (e.g. Accra, East Legon)"); locationF.setStyle(fieldStyle());

        TextArea descF = new TextArea(hostel != null ? hostel.getDescription() : "");
        descF.setPromptText("Description"); descF.setPrefRowCount(3);
        descF.setStyle("-fx-background-color: #EDF2F7; -fx-background-radius: 8; -fx-font-size: 13px;");

        TextField amenitiesF = new TextField(hostel != null ? hostel.getAmenities() : "");
        amenitiesF.setPromptText("Amenities (e.g. WiFi, Security, Water)"); amenitiesF.setStyle(fieldStyle());

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #E53E3E; -fx-font-size: 12px;");

        Button saveBtn = new Button(hostel == null ? "Add Hostel" : "Save Changes");
        saveBtn.setStyle("-fx-background-color: linear-gradient(to right, #2C5282, #3182CE); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12 30; -fx-cursor: hand;");
        saveBtn.setPrefWidth(400);
        saveBtn.setOnAction(e -> {
            if (nameF.getText().isEmpty() || locationF.getText().isEmpty()) {
                errorLbl.setText("Name and location are required."); return;
            }
            boolean success;
            if (hostel == null)
                success = DAO.addHostel(SessionManager.getCurrentStudent().getId(),
                    nameF.getText(), locationF.getText(), descF.getText(), amenitiesF.getText());
            else
                success = DAO.updateHostel(hostel.getId(), nameF.getText(), locationF.getText(), descF.getText(), amenitiesF.getText());

            if (success) { dialog.close(); loadMyHostels(); }
            else errorLbl.setText("Failed to save. Please try again.");
        });

        root.getChildren().addAll(title, label("Hostel Name"), nameF, label("Location"), locationF,
            label("Description"), descF, label("Amenities"), amenitiesF, errorLbl, saveBtn);
        return root;
    }

    private void showAddRoomDialog(Hostel hostel) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Room to " + hostel.getName());

        VBox root = new VBox(14);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #F7FAFC;");
        root.setPrefWidth(420);

        Label title = new Label("➕  Add New Room");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");

        TextField roomNumF = new TextField(); roomNumF.setPromptText("Room Number (e.g. 101)"); roomNumF.setStyle(fieldStyle());
        ComboBox<String> roomTypeF = new ComboBox<>();
        roomTypeF.getItems().addAll("Single", "Double", "Shared");
        roomTypeF.setValue("Single");
        roomTypeF.setStyle("-fx-background-color: #EDF2F7; -fx-background-radius: 8; -fx-font-size: 13px;");
        roomTypeF.setPrefWidth(360);
        TextField priceF = new TextField(); priceF.setPromptText("Price per month (GH₵)"); priceF.setStyle(fieldStyle());
        TextField capacityF = new TextField("1"); capacityF.setPromptText("Capacity"); capacityF.setStyle(fieldStyle());
        TextArea descF = new TextArea(); descF.setPromptText("Room description"); descF.setPrefRowCount(2);
        descF.setStyle("-fx-background-color: #EDF2F7; -fx-background-radius: 8; -fx-font-size: 13px;");

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #E53E3E; -fx-font-size: 12px;");

        Button saveBtn = new Button("Add Room");
        saveBtn.setStyle("-fx-background-color: #38A169; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12 30; -fx-cursor: hand;");
        saveBtn.setPrefWidth(360);
        saveBtn.setOnAction(e -> {
            try {
                double price = Double.parseDouble(priceF.getText().trim());
                int capacity = Integer.parseInt(capacityF.getText().trim());
                boolean success = DAO.addRoom(hostel.getId(), roomNumF.getText(), roomTypeF.getValue(),
                    price, capacity, descF.getText());
                if (success) { dialog.close(); loadMyHostels(); }
                else errorLbl.setText("Failed to add room.");
            } catch (NumberFormatException ex) {
                errorLbl.setText("Please enter valid price and capacity.");
            }
        });

        root.getChildren().addAll(title, label("Room Number"), roomNumF, label("Room Type"), roomTypeF,
            label("Price per Month (GH₵)"), priceF, label("Capacity"), capacityF, label("Description"), descF, errorLbl, saveBtn);
        dialog.setScene(new Scene(root));
        dialog.show();
    }

    private String fieldStyle() {
        return "-fx-background-color: #EDF2F7; -fx-background-radius: 8; -fx-border-color: transparent; -fx-padding: 10 14; -fx-font-size: 13px; -fx-pref-width: 360;";
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4A5568;");
        return l;
    }

    @FXML
    public void showMyBookings() {
        contentArea.getChildren().clear();
        Label title = new Label("📋  My Bookings");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");
        Label subtitle = new Label("See who has booked your hostel rooms");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        VBox header = new VBox(4);
        header.getChildren().addAll(title, subtitle);
        contentArea.getChildren().add(header);

        int ownerId = SessionManager.getCurrentStudent().getId();
        List<Booking> bookings = DAO.getOwnerBookings(ownerId);

        if (bookings.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setAlignment(javafx.geometry.Pos.CENTER);
            empty.setPadding(new Insets(40));
            Label msg = new Label("No bookings yet for your hostels.");
            msg.setStyle("-fx-font-size: 15px; -fx-text-fill: #718096;");
            empty.getChildren().add(msg);
            contentArea.getChildren().add(empty);
            return;
        }

        for (Booking b : bookings) {
            HBox row = new HBox(16);
            row.setPadding(new Insets(14));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label avatar = new Label("👤");
            avatar.setStyle("-fx-font-size: 24px; -fx-background-color: #EBF8FF; -fx-background-radius: 50; -fx-padding: 8;");

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label studentName = new Label(b.getStudentName() != null ? b.getStudentName() : "Unknown");
            studentName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
            String phone = b.getStudentPhone() != null && !b.getStudentPhone().isEmpty()
                ? "  |  📞 " + b.getStudentPhone() : "";
            Label contact = new Label((b.getStudentEmail() != null ? b.getStudentEmail() : "") + phone);
            contact.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
            Label hostelRoom = new Label("🏨 " + b.getHostelName() + "  —  Room " + b.getRoomNumber() + " (" + b.getRoomType() + ")");
            hostelRoom.setStyle("-fx-font-size: 12px; -fx-text-fill: #4A5568;");
            Label dates = new Label("📅 " + b.getCheckInDate() + " → " + b.getCheckOutDate());
            dates.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
            info.getChildren().addAll(studentName, contact, hostelRoom, dates);

            Label price = new Label(b.getFormattedPrice());
            price.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2F855A;");

            String statusColor = "CONFIRMED".equals(b.getStatus())
                ? "#C6F6D5; -fx-text-fill: #276749" : "#FED7D7; -fx-text-fill: #9B2C2C";
            Label status = new Label(b.getStatus());
            status.setStyle("-fx-background-color: " + statusColor + "; -fx-background-radius: 20; -fx-padding: 3 12; -fx-font-size: 11px; -fx-font-weight: bold;");

            row.getChildren().addAll(avatar, info, price, status);
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
