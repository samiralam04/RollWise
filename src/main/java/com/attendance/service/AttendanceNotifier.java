package com.attendance.service;

import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.*;

public class AttendanceNotifier {

    public static void sendAttendanceEmails() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.out.println("❌ Database connection failed.");
                return;
            }

            System.out.println("✅ Database connected successfully.");

            // Fetch students with low attendance
            String query = "SELECT a.student_id, p.parent_email, " +
                    "(COUNT(CASE WHEN a.status = 'Present' THEN 1 END) * 100.0 / COUNT(*)) AS percentage " +
                    "FROM attendance a " +
                    "JOIN parents p ON a.parent_email_id = p.id " +
                    "GROUP BY a.student_id, p.parent_email " +
                    "HAVING (COUNT(CASE WHEN a.status = 'Present' THEN 1 END) * 100.0 / COUNT(*)) < 75";


            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                boolean messageSent = false;
                List<String> emailList = new ArrayList<>();
                List<String> messages = new ArrayList<>();

                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    double percentage = rs.getDouble("percentage");
                    String parentEmail = rs.getString("parent_email");

                    System.out.println("\n📌 Found student with low attendance:");
                    System.out.println("   📌 Student ID: " + studentId);
                    System.out.println("   📌 Parent Email: " + parentEmail);
                    System.out.println("   📌 Attendance Percentage: " + percentage);

                    emailList.add(parentEmail);
                    messages.add("Dear Parent,\n\n" +
                            "Your child's attendance is low (" + percentage + "%). " +
                            "Please ensure they attend classes regularly.\n\n" +
                            "Best Regards,\nAttendance System");

                    messageSent = true;
                }

                if (messageSent) {
                    sendBulkEmails(emailList, messages);
//                    response.getWriter().write("✅ Email notifications sent successfully.");
                }
//                else {
//                    response.getWriter().write("⚠️ No students found with attendance below 75%.");
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendBulkEmails(List<String> recipients, List<String> messages) {
        String subject = "⚠️ Attendance Alert for Your Child";
        for (int i = 0; i < recipients.size(); i++) {
            try {
                String emailBody = messages.get(i);
                EmailNotifier.send(Collections.singletonList(recipients.get(i)), subject, emailBody);
                System.out.println("📩 Email sent to " + recipients.get(i));
            } catch (Exception e) {
                System.err.println("❌ Error sending email to " + recipients.get(i) + ": " + e.getMessage());
            }
        }
    }
}
