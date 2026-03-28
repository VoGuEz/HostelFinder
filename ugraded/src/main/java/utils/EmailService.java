package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    // ⚠️ Replace these with your Gmail address and App Password
    private static final String FROM_EMAIL = "nyourerub@gmail.com";
    private static final String APP_PASSWORD = "kyhntaxrojvaklrh"; // Gmail App Password

    private static Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });
    }

    // Send verification OTP on register
    public static boolean sendVerificationEmail(String toEmail, String name, String otp) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("HostelFinder - Verify Your Email");

            String body = "Dear " + name + ",\n\n" +
                "Welcome to HostelFinder! 🏠\n\n" +
                "Your verification code is:\n\n" +
                "  ➤  " + otp + "\n\n" +
                "This code expires in 10 minutes.\n\n" +
                "If you did not create an account, please ignore this email.\n\n" +
                "Best regards,\nThe HostelFinder Team";

            message.setText(body);
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Email send failed: " + e.getMessage());
            return false;
        }
    }

    // Send password reset OTP
    public static boolean sendPasswordResetEmail(String toEmail, String name, String otp) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("HostelFinder - Password Reset Code");

            String body = "Dear " + name + ",\n\n" +
                "You requested a password reset for your HostelFinder account.\n\n" +
                "Your reset code is:\n\n" +
                "  ➤  " + otp + "\n\n" +
                "This code expires in 10 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best regards,\nThe HostelFinder Team";

            message.setText(body);
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Email send failed: " + e.getMessage());
            return false;
        }
    }

    // Send booking confirmation email
    public static boolean sendBookingConfirmation(String toEmail, String name,
            String hostelName, String roomNumber, String checkIn, String checkOut, String total) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("HostelFinder - Booking Confirmed! 🎉");

            String body = "Dear " + name + ",\n\n" +
                "Your booking has been confirmed! 🎉\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "BOOKING DETAILS\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Hostel:      " + hostelName + "\n" +
                "Room:        " + roomNumber + "\n" +
                "Check-in:    " + checkIn + "\n" +
                "Check-out:   " + checkOut + "\n" +
                "Total:       GH₵ " + total + "\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Thank you for using HostelFinder!\n\n" +
                "Best regards,\nThe HostelFinder Team";

            message.setText(body);
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Email send failed: " + e.getMessage());
            return false;
        }
    }
}
