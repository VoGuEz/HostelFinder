package models;

public class Hostel {
    private int id;
    private int ownerId;
    private String name;
    private String location;
    private String description;
    private String amenities;
    private String imageUrl;
    private double rating;
    private int totalReviews;

    public Hostel() {}

    public Hostel(int id, String name, String location, String description, String amenities, double rating) {
        this.id = id; this.name = name; this.location = location;
        this.description = description; this.amenities = amenities; this.rating = rating;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }

    public String getStarRating() {
        int fullStars = (int) rating;
        String filled = "*".repeat(fullStars);
        String empty = "-".repeat(5 - fullStars);
        return filled + empty;
    }
}
