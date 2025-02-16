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


public class SMSNotificationServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = DBConnection.getConnection()) {
            // Fetch students with attendance below 75%
            String query = "SELECT s.name, s.phone, s.parent_phone, a.subject, (a.attended_classes * 100.0 / a.total_classes) AS percentage " +
                    "FROM attendance a JOIN users s ON a.student_id = s.id " +
                    "WHERE (a.attended_classes * 100.0 / a.total_classes) < 75";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String studentName = rs.getString("name");
                String subject = rs.getString("subject");
                double percentage = rs.getDouble("percentage");
                String parentPhone = rs.getString("parent_phone");

                // SMS message to parents
                String message = "Dear Parent, your child " + studentName + " has low attendance in " + subject +
                        " (Only " + percentage + "%). Please ensure they attend classes regularly.";

                // Send SMS
                SMSService.sendSMS(parentPhone, message);
            }

            // Fetch students with attendance nearing 75% (between 75% and 77%) for warning SMS
            query = "SELECT s.name, s.phone, a.subject, (a.attended_classes * 100.0 / a.total_classes) AS percentage " +
                    "FROM attendance a JOIN users s ON a.student_id = s.id " +
                    "WHERE (a.attended_classes * 100.0 / a.total_classes) BETWEEN 75 AND 77";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String studentName = rs.getString("name");
                String subject = rs.getString("subject");
                double percentage = rs.getDouble("percentage");
                String studentPhone = rs.getString("phone");

                // SMS message to student
                String warningMessage = "Alert: Your attendance in " + subject + " is " + percentage + "%. " +
                        "Maintain attendance above 75% to avoid penalties.";

                // Send SMS
                SMSService.sendSMS(studentPhone, warningMessage);
            }

            response.getWriter().write("SMS notifications sent successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("Error sending SMS notifications.");
        }
    }
}
