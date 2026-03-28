package database;

import models.*;
import security.PasswordUtils;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DAO {

    // ===================== STUDENT =====================

    public static boolean registerStudent(String fullName, String email, String password,
            String phone, String university, String otp) {
        String sql = "INSERT INTO students (full_name, email, password, phone, university, role, is_verified, verification_otp, otp_expiry) VALUES (?, ?, ?, ?, ?, 'STUDENT', FALSE, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, PasswordUtils.hashPassword(password));
            ps.setString(4, phone);
            ps.setString(5, university);
            ps.setString(6, otp);
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Register error: " + e.getMessage());
            return false;
        }
    }

    public static boolean verifyEmail(String email, String otp) {
        String sql = "SELECT verification_otp, otp_expiry FROM students WHERE email = ? AND is_verified = FALSE";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedOtp = rs.getString("verification_otp");
                Timestamp expiry = rs.getTimestamp("otp_expiry");
                if (storedOtp.equals(otp) && expiry.after(new Timestamp(System.currentTimeMillis()))) {
                    String update = "UPDATE students SET is_verified = TRUE, verification_otp = NULL WHERE email = ?";
                    try (PreparedStatement ps2 = DatabaseConnection.getConnection().prepareStatement(update)) {
                        ps2.setString(1, email);
                        ps2.executeUpdate();
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Verify email error: " + e.getMessage());
        }
        return false;
    }

    public static Student loginStudent(String email, String password) {
        String sql = "SELECT * FROM students WHERE email = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (PasswordUtils.checkPassword(password, hashedPassword)) {
                    Student s = new Student();
                    s.setId(rs.getInt("id"));
                    s.setFullName(rs.getString("full_name"));
                    s.setEmail(rs.getString("email"));
                    s.setPhone(rs.getString("phone"));
                    s.setUniversity(rs.getString("university"));
                    s.setRole(rs.getString("role"));
                    s.setVerified(rs.getBoolean("is_verified"));
                    s.setProfileImage(rs.getString("profile_image"));
                    return s;
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }

    public static boolean emailExists(String email) {
        String sql = "SELECT id FROM students WHERE email = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public static boolean saveResetOTP(String email, String otp) {
        String sql = "UPDATE students SET verification_otp = ?, otp_expiry = ? WHERE email = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, otp);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)));
            ps.setString(3, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean resetPassword(String email, String otp, String newPassword) {
        String sql = "SELECT verification_otp, otp_expiry FROM students WHERE email = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedOtp = rs.getString("verification_otp");
                Timestamp expiry = rs.getTimestamp("otp_expiry");
                if (storedOtp != null && storedOtp.equals(otp) &&
                        expiry.after(new Timestamp(System.currentTimeMillis()))) {
                    String update = "UPDATE students SET password = ?, verification_otp = NULL WHERE email = ?";
                    try (PreparedStatement ps2 = DatabaseConnection.getConnection().prepareStatement(update)) {
                        ps2.setString(1, PasswordUtils.hashPassword(newPassword));
                        ps2.setString(2, email);
                        ps2.executeUpdate();
                        return true;
                    }
                }
            }
        } catch (SQLException e) { System.err.println("Reset password error: " + e.getMessage()); }
        return false;
    }

    public static boolean updateStudentProfile(int id, String fullName, String phone, String university, String profileImage) {
        String sql = "UPDATE students SET full_name = ?, phone = ?, university = ?" +
            (profileImage != null ? ", profile_image = ?" : "") + " WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, university);
            if (profileImage != null) {
                ps.setString(4, profileImage);
                ps.setInt(5, id);
            } else {
                ps.setInt(4, id);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ===================== HOSTELS =====================

    public static List<Hostel> searchHostels(String location, String query, String sortBy) {
        List<Hostel> hostels = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM hostels WHERE is_active = TRUE");
        if (location != null && !location.isEmpty() && !location.equals("All Locations"))
            sql.append(" AND location LIKE ?");
        if (query != null && !query.isEmpty())
            sql.append(" AND (name LIKE ? OR description LIKE ? OR amenities LIKE ?)");
        if ("price".equals(sortBy)) sql.append(" ORDER BY (SELECT MIN(price_per_month) FROM rooms WHERE hostel_id = hostels.id) ASC");
        else sql.append(" ORDER BY rating DESC");

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            if (location != null && !location.isEmpty() && !location.equals("All Locations"))
                ps.setString(idx++, "%" + location + "%");
            if (query != null && !query.isEmpty()) {
                ps.setString(idx++, "%" + query + "%");
                ps.setString(idx++, "%" + query + "%");
                ps.setString(idx++, "%" + query + "%");
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Hostel h = new Hostel(rs.getInt("id"), rs.getString("name"),
                    rs.getString("location"), rs.getString("description"),
                    rs.getString("amenities"), rs.getDouble("rating"));
                h.setTotalReviews(rs.getInt("total_reviews"));
                h.setOwnerId(rs.getInt("owner_id"));
                hostels.add(h);
            }
        } catch (SQLException e) { System.err.println("Search hostels error: " + e.getMessage()); }
        return hostels;
    }

    public static List<Hostel> getAllHostels() { return searchHostels("", "", "rating"); }

    public static List<Hostel> getOwnerHostels(int ownerId) {
        List<Hostel> hostels = new ArrayList<>();
        String sql = "SELECT * FROM hostels WHERE owner_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Hostel h = new Hostel(rs.getInt("id"), rs.getString("name"),
                    rs.getString("location"), rs.getString("description"),
                    rs.getString("amenities"), rs.getDouble("rating"));
                h.setTotalReviews(rs.getInt("total_reviews"));
                hostels.add(h);
            }
        } catch (SQLException e) { System.err.println("Get owner hostels error: " + e.getMessage()); }
        return hostels;
    }

    public static boolean addHostel(int ownerId, String name, String location, String description, String amenities) {
        String sql = "INSERT INTO hostels (owner_id, name, location, description, amenities) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ps.setString(2, name);
            ps.setString(3, location);
            ps.setString(4, description);
            ps.setString(5, amenities);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean updateHostel(int id, String name, String location, String description, String amenities) {
        String sql = "UPDATE hostels SET name=?, location=?, description=?, amenities=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, location);
            ps.setString(3, description); ps.setString(4, amenities);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean deleteHostel(int id) {
        String sql = "UPDATE hostels SET is_active = FALSE WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ===================== ROOMS =====================

    public static List<Room> getRoomsByHostel(int hostelId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, h.name as hostel_name FROM rooms r JOIN hostels h ON r.hostel_id = h.id WHERE r.hostel_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, hostelId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Room r = buildRoom(rs);
                rooms.add(r);
            }
        } catch (SQLException e) { System.err.println("Get rooms error: " + e.getMessage()); }
        return rooms;
    }

    public static List<Room> searchAvailableRooms(String location, String roomType, double minPrice, double maxPrice) {
        List<Room> rooms = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT r.*, h.name as hostel_name FROM rooms r JOIN hostels h ON r.hostel_id = h.id WHERE r.is_available = TRUE AND h.is_active = TRUE");
        if (location != null && !location.isEmpty() && !location.equals("All Locations"))
            sql.append(" AND h.location LIKE ?");
        if (roomType != null && !roomType.isEmpty() && !roomType.equals("All Types"))
            sql.append(" AND r.room_type = ?");
        if (minPrice > 0) sql.append(" AND r.price_per_month >= ?");
        if (maxPrice > 0) sql.append(" AND r.price_per_month <= ?");
        sql.append(" ORDER BY r.price_per_month ASC");

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            if (location != null && !location.isEmpty() && !location.equals("All Locations"))
                ps.setString(idx++, "%" + location + "%");
            if (roomType != null && !roomType.isEmpty() && !roomType.equals("All Types"))
                ps.setString(idx++, roomType);
            if (minPrice > 0) ps.setDouble(idx++, minPrice);
            if (maxPrice > 0) ps.setDouble(idx++, maxPrice);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(buildRoom(rs));
        } catch (SQLException e) { System.err.println("Search rooms error: " + e.getMessage()); }
        return rooms;
    }

    public static boolean addRoom(int hostelId, String roomNumber, String roomType, double price, int capacity, String description) {
        String sql = "INSERT INTO rooms (hostel_id, room_number, room_type, price_per_month, capacity, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, hostelId); ps.setString(2, roomNumber); ps.setString(3, roomType);
            ps.setDouble(4, price); ps.setInt(5, capacity); ps.setString(6, description);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    private static Room buildRoom(ResultSet rs) throws SQLException {
        Room r = new Room(rs.getInt("id"), rs.getInt("hostel_id"), rs.getString("room_number"),
            rs.getString("room_type"), rs.getDouble("price_per_month"),
            rs.getBoolean("is_available"), rs.getInt("capacity"), rs.getString("description"));
        try { r.setHostelName(rs.getString("hostel_name")); } catch (SQLException ignored) {}
        return r;
    }

    // ===================== BOOKINGS =====================

    public static boolean bookRoom(int studentId, int roomId, int hostelId,
            LocalDate checkIn, LocalDate checkOut, double totalPrice, String paymentMethod) {
        String sql = "INSERT INTO bookings (student_id, room_id, hostel_id, check_in_date, check_out_date, total_price, status, payment_method) VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED', ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.setInt(2, roomId); ps.setInt(3, hostelId);
            ps.setDate(4, Date.valueOf(checkIn)); ps.setDate(5, Date.valueOf(checkOut));
            ps.setDouble(6, totalPrice); ps.setString(7, paymentMethod);
            ps.executeUpdate();
            String updateRoom = "UPDATE rooms SET is_available = FALSE WHERE id = ?";
            try (PreparedStatement ps2 = DatabaseConnection.getConnection().prepareStatement(updateRoom)) {
                ps2.setInt(1, roomId); ps2.executeUpdate();
            }
            // Add notification
            addNotification(studentId, "Booking Confirmed! 🎉", "Your booking has been confirmed. Check My Bookings for details.");
            return true;
        } catch (SQLException e) { System.err.println("Book room error: " + e.getMessage()); return false; }
    }

    public static List<Booking> getStudentBookings(int studentId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, h.name as hostel_name, r.room_number, r.room_type FROM bookings b " +
                     "JOIN hostels h ON b.hostel_id = h.id JOIN rooms r ON b.room_id = r.id " +
                     "WHERE b.student_id = ? ORDER BY b.booked_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(buildBooking(rs));
        } catch (SQLException e) { System.err.println("Get bookings error: " + e.getMessage()); }
        return bookings;
    }

    public static List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, h.name as hostel_name, r.room_number, r.room_type FROM bookings b " +
                     "JOIN hostels h ON b.hostel_id = h.id JOIN rooms r ON b.room_id = r.id ORDER BY b.booked_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookings.add(buildBooking(rs));
        } catch (SQLException e) { System.err.println("Get all bookings error: " + e.getMessage()); }
        return bookings;
    }

    public static List<Booking> getOwnerBookings(int ownerId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, h.name as hostel_name, r.room_number, r.room_type, " +
                     "s.full_name as student_name, s.email as student_email, s.phone as student_phone " +
                     "FROM bookings b " +
                     "JOIN hostels h ON b.hostel_id = h.id " +
                     "JOIN rooms r ON b.room_id = r.id " +
                     "JOIN students s ON b.student_id = s.id " +
                     "WHERE h.owner_id = ? ORDER BY b.booked_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Booking b = buildBooking(rs);
                b.setStudentName(rs.getString("student_name"));
                b.setStudentEmail(rs.getString("student_email"));
                b.setStudentPhone(rs.getString("student_phone"));
                bookings.add(b);
            }
        } catch (SQLException e) { System.err.println("Get owner bookings error: " + e.getMessage()); }
        return bookings;
    }

    public static boolean cancelBooking(int bookingId, int roomId) {
        String sql = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, bookingId); ps.executeUpdate();
            String updateRoom = "UPDATE rooms SET is_available = TRUE WHERE id = ?";
            try (PreparedStatement ps2 = DatabaseConnection.getConnection().prepareStatement(updateRoom)) {
                ps2.setInt(1, roomId); ps2.executeUpdate();
            }
            return true;
        } catch (SQLException e) { return false; }
    }

    private static Booking buildBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id")); b.setStudentId(rs.getInt("student_id"));
        b.setRoomId(rs.getInt("room_id")); b.setHostelId(rs.getInt("hostel_id"));
        b.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        b.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        b.setTotalPrice(rs.getDouble("total_price")); b.setStatus(rs.getString("status"));
        b.setHostelName(rs.getString("hostel_name")); b.setRoomNumber(rs.getString("room_number"));
        b.setRoomType(rs.getString("room_type"));
        return b;
    }

    // ===================== REVIEWS =====================

    public static boolean addReview(int studentId, int hostelId, int rating, String comment) {
        String sql = "INSERT INTO reviews (student_id, hostel_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.setInt(2, hostelId);
            ps.setInt(3, rating); ps.setString(4, comment);
            ps.executeUpdate();
            // Update hostel average rating
            String updateRating = "UPDATE hostels SET rating = (SELECT AVG(rating) FROM reviews WHERE hostel_id = ?), " +
                "total_reviews = (SELECT COUNT(*) FROM reviews WHERE hostel_id = ?) WHERE id = ?";
            try (PreparedStatement ps2 = DatabaseConnection.getConnection().prepareStatement(updateRating)) {
                ps2.setInt(1, hostelId); ps2.setInt(2, hostelId); ps2.setInt(3, hostelId);
                ps2.executeUpdate();
            }
            return true;
        } catch (SQLException e) { return false; }
    }

    public static List<Review> getHostelReviews(int hostelId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, s.full_name FROM reviews r JOIN students s ON r.student_id = s.id WHERE r.hostel_id = ? ORDER BY r.created_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, hostelId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Review r = new Review();
                r.setId(rs.getInt("id")); r.setStudentId(rs.getInt("student_id"));
                r.setHostelId(rs.getInt("hostel_id")); r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment")); r.setStudentName(rs.getString("full_name"));
                reviews.add(r);
            }
        } catch (SQLException e) { System.err.println("Get reviews error: " + e.getMessage()); }
        return reviews;
    }

    // ===================== SAVED HOSTELS =====================

    public static boolean saveHostel(int studentId, int hostelId) {
        String sql = "INSERT IGNORE INTO saved_hostels (student_id, hostel_id) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.setInt(2, hostelId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean unsaveHostel(int studentId, int hostelId) {
        String sql = "DELETE FROM saved_hostels WHERE student_id = ? AND hostel_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.setInt(2, hostelId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean isHostelSaved(int studentId, int hostelId) {
        String sql = "SELECT id FROM saved_hostels WHERE student_id = ? AND hostel_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.setInt(2, hostelId);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    // ===================== NOTIFICATIONS =====================

    public static boolean addNotification(int studentId, String title, String message) {
        String sql = "INSERT INTO notifications (student_id, title, message) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.setString(2, title); ps.setString(3, message);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static List<Notification> getStudentNotifications(int studentId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE student_id = ? ORDER BY created_at DESC LIMIT 20";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id")); n.setTitle(rs.getString("title"));
                n.setMessage(rs.getString("message")); n.setRead(rs.getBoolean("is_read"));
                notifications.add(n);
            }
        } catch (SQLException e) { System.err.println("Get notifications error: " + e.getMessage()); }
        return notifications;
    }

    public static int getUnreadNotificationCount(int studentId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE student_id = ? AND is_read = FALSE";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    public static void markAllNotificationsRead(int studentId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE student_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.executeUpdate();
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    // ===================== ADMIN =====================

    public static int getTotalStudents() {
        return getCount("SELECT COUNT(*) FROM students WHERE role = 'STUDENT'");
    }

    public static int getTotalHostels() {
        return getCount("SELECT COUNT(*) FROM hostels WHERE is_active = TRUE");
    }

    public static int getTotalBookings() {
        return getCount("SELECT COUNT(*) FROM bookings");
    }

    public static double getTotalRevenue() {
        String sql = "SELECT SUM(total_price) FROM bookings WHERE status = 'CONFIRMED'";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    public static List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE role = 'STUDENT' ORDER BY created_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Student s = new Student();
                s.setId(rs.getInt("id")); s.setFullName(rs.getString("full_name"));
                s.setEmail(rs.getString("email")); s.setPhone(rs.getString("phone"));
                s.setUniversity(rs.getString("university")); s.setRole(rs.getString("role"));
                s.setVerified(rs.getBoolean("is_verified"));
                students.add(s);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return students;
    }

    private static int getCount(String sql) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }
}
