package utils;

import models.Student;
import java.time.LocalDateTime;

public class SessionManager {
    private static Student currentStudent = null;
    private static String currentRole = null; // "STUDENT", "OWNER", "ADMIN"
    private static LocalDateTime lastActivity = null;
    private static final int TIMEOUT_MINUTES = 30;

    public static void setCurrentStudent(Student student, String role) {
        currentStudent = student;
        currentRole = role;
        lastActivity = LocalDateTime.now();
    }

    public static void setCurrentStudent(Student student) {
        setCurrentStudent(student, "STUDENT");
    }

    public static Student getCurrentStudent() {
        return currentStudent;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static boolean isLoggedIn() {
        if (currentStudent == null) return false;
        if (isSessionExpired()) {
            logout();
            return false;
        }
        refreshSession();
        return true;
    }

    public static boolean isSessionExpired() {
        if (lastActivity == null) return true;
        return LocalDateTime.now().isAfter(lastActivity.plusMinutes(TIMEOUT_MINUTES));
    }

    public static void refreshSession() {
        lastActivity = LocalDateTime.now();
    }

    public static void logout() {
        currentStudent = null;
        currentRole = null;
        lastActivity = null;
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(currentRole);
    }

    public static boolean isOwner() {
        return "OWNER".equals(currentRole);
    }

    public static boolean isStudent() {
        return "STUDENT".equals(currentRole);
    }
}
