package models;

public class Room {
    private int id;
    private int hostelId;
    private String roomNumber;
    private String roomType;
    private double pricePerMonth;
    private boolean isAvailable;
    private int capacity;
    private String description;
    private String hostelName; // joined from hostel table

    public Room() {}

    public Room(int id, int hostelId, String roomNumber, String roomType,
                double pricePerMonth, boolean isAvailable, int capacity, String description) {
        this.id = id;
        this.hostelId = hostelId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerMonth = pricePerMonth;
        this.isAvailable = isAvailable;
        this.capacity = capacity;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHostelId() { return hostelId; }
    public void setHostelId(int hostelId) { this.hostelId = hostelId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public double getPricePerMonth() { return pricePerMonth; }
    public void setPricePerMonth(double pricePerMonth) { this.pricePerMonth = pricePerMonth; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHostelName() { return hostelName; }
    public void setHostelName(String hostelName) { this.hostelName = hostelName; }

    public String getFormattedPrice() {
        return String.format("GH₵ %.2f/month", pricePerMonth);
    }
}
