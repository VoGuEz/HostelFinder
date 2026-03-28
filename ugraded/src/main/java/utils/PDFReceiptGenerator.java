package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import models.Booking;
import models.Student;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class PDFReceiptGenerator {

    public static String generateReceipt(Booking booking, Student student) {
        String fileName = "BookingReceipt_" + booking.getId() + ".pdf";
        String filePath = System.getProperty("user.home") + "/Downloads/" + fileName;

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Colors
            BaseColor primaryColor = new BaseColor(44, 82, 130);
            BaseColor lightGray = new BaseColor(247, 250, 252);
            BaseColor greenColor = new BaseColor(47, 133, 90);

            // Fonts
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.WHITE);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, primaryColor);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.DARK_GRAY);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.DARK_GRAY);
            Font greenFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, greenColor);

            // Header background
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(primaryColor);
            headerCell.setPadding(20);
            headerCell.setBorder(Rectangle.NO_BORDER);

            Paragraph headerTitle = new Paragraph("🏠  HostelFinder", titleFont);
            headerTitle.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(headerTitle);

            Paragraph headerSub = new Paragraph("BOOKING RECEIPT", new Font(Font.FontFamily.HELVETICA, 13, Font.NORMAL, new BaseColor(190, 227, 248)));
            headerSub.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(headerSub);
            headerTable.addCell(headerCell);
            document.add(headerTable);

            document.add(Chunk.NEWLINE);

            // Booking ID and status
            Paragraph bookingId = new Paragraph("Booking #" + booking.getId(), headerFont);
            bookingId.setAlignment(Element.ALIGN_CENTER);
            document.add(bookingId);

            Paragraph status = new Paragraph("✅  " + booking.getStatus(), greenFont);
            status.setAlignment(Element.ALIGN_CENTER);
            document.add(status);

            document.add(Chunk.NEWLINE);

            // Details table
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(90);
            detailsTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            detailsTable.setWidths(new float[]{1f, 2f});

            addTableRow(detailsTable, "Student Name", student.getFullName(), boldFont, normalFont, lightGray);
            addTableRow(detailsTable, "Email", student.getEmail(), boldFont, normalFont, BaseColor.WHITE);
            addTableRow(detailsTable, "University", student.getUniversity() != null ? student.getUniversity() : "N/A", boldFont, normalFont, lightGray);
            addTableRow(detailsTable, "Hostel", booking.getHostelName(), boldFont, normalFont, BaseColor.WHITE);
            addTableRow(detailsTable, "Room", "Room " + booking.getRoomNumber() + " (" + booking.getRoomType() + ")", boldFont, normalFont, lightGray);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            addTableRow(detailsTable, "Check-in", booking.getCheckInDate().format(fmt), boldFont, normalFont, BaseColor.WHITE);
            addTableRow(detailsTable, "Check-out", booking.getCheckOutDate().format(fmt), boldFont, normalFont, lightGray);
            addTableRow(detailsTable, "Total Amount", "GH₵ " + String.format("%.2f", booking.getTotalPrice()), boldFont,
                new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, greenColor), BaseColor.WHITE);

            document.add(detailsTable);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Footer
            Paragraph footer = new Paragraph("Thank you for using HostelFinder! 🏠\nFor support, contact us at support@hostelfinder.com",
                new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return filePath;

        } catch (Exception e) {
            System.err.println("PDF generation failed: " + e.getMessage());
            return null;
        }
    }

    private static void addTableRow(PdfPTable table, String label, String value,
            Font labelFont, Font valueFont, BaseColor bgColor) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(bgColor);
        labelCell.setPadding(10);
        labelCell.setBorderColor(new BaseColor(226, 232, 240));

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBackgroundColor(bgColor);
        valueCell.setPadding(10);
        valueCell.setBorderColor(new BaseColor(226, 232, 240));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
