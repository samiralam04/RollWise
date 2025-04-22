package com.attendance.controller;

import com.attendance.util.DBConnection;
import com.attendance.util.SMSService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/sendEmailNotifications")
public class SMSNotificationServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("📢 Servlet called: sendEmailNotifications");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.out.println("❌ Database connection failed.");
                response.getWriter().write("Database connection error.");
                return;
            }

            System.out.println("✅ Database connected successfully.");

            // ✅ FIXED SQL QUERY: Fetch parent email from attendance table
            String query = "SELECT student_id, parent_email, " +
                    "(COUNT(CASE WHEN status = 'Present' THEN 1 END) * 100.0 / COUNT(*)) AS percentage " +
                    "FROM attendance " +
                    "GROUP BY student_id, parent_email " +
                    "HAVING (COUNT(CASE WHEN status = 'Present' THEN 1 END) * 100.0 / COUNT(*)) < 75";

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                boolean messageSent = false;

                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    double percentage = rs.getDouble("percentage");
                    String parentEmail = rs.getString("parent_email");

                    System.out.println("\n📌 Found student with low attendance:");
                    System.out.println("   📌 Student ID: " + studentId);
                    System.out.println("   📌 Parent Email: " + parentEmail);
                    System.out.println("   📌 Attendance Percentage: " + percentage);

                    // Email subject and body
                    String subject = "⚠️ Attendance Alert for Your Child";
                    String message = "Dear Parent,\n\n" +
                            "Your child's attendance is low (" + percentage + "%). " +
                            "Please ensure they attend classes regularly.\n\n" +
                            "Best Regards,\nAttendance System";

                    System.out.println("📩 Sending Email to " + parentEmail);

                    // Send Email
                    SMSService.sendEmail(parentEmail, subject, message);
                    System.out.print("Email sent to parents"+parentEmail); // for testing console message !

                    messageSent = true;
                }

                if (messageSent) {
                    response.getWriter().write("✅ Email notifications sent successfully.");
                } else {
                    response.getWriter().write("⚠️ No students found with attendance below 75%.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write(" Error sending email notifications.");
        }
    }
}
