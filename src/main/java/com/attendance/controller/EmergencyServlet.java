package com.attendance.controller;

import com.attendance.service.EmailNotifier;
import com.attendance.util.DBConnection;
import com.attendance.service.EmailNotifier;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/emergency")
public class EmergencyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EmergencyServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String dateString = request.getParameter("date");

        // Validate input fields
        if (title == null || title.trim().isEmpty() ||
                description == null || description.trim().isEmpty() ||
                dateString == null || dateString.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Title, description, and date are required.");
            return;
        }

        // Convert String to java.sql.Date
        Date sqlDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = sdf.parse(dateString);
            sqlDate = new java.sql.Date(parsedDate.getTime());
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, "Invalid date format: {0}", dateString);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date format.");
            return;
        }

        // Database connection
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO emergency (title, description, date) VALUES (?, ?, ?)")) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setDate(3, sqlDate);
            stmt.executeUpdate();

            // Fetch all parent emails from the parents table
            List<String> parentEmails = new ArrayList<>();
            String fetchParentEmailsQuery = "SELECT parent_email FROM parents";
            try (PreparedStatement emailStmt = conn.prepareStatement(fetchParentEmailsQuery);
                 ResultSet rs = emailStmt.executeQuery()) {
                while (rs.next()) {
                    parentEmails.add(rs.getString("parent_email"));
                }
            }

            // Send emergency alert email to all parents
            if (!parentEmails.isEmpty()) {
                String emailSubject = "🚨 Emergency Alert: " + title + " 🚨";
                String emailBody = "Dear Parent,\n\nAn emergency alert has been issued:\n\n"
                        + title + "\n" + description
                        + "\n\nDate: " + dateString
                        + "\n\nPlease take necessary actions.\n\nBest regards,\nSchool Administration";

                EmailNotifier.sendAlertToParents(parentEmails, emailBody);
            }

            // Redirect with success message
            response.sendRedirect(request.getContextPath() + "/pages/emergency.jsp?success=Emergency%20holiday%20added");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error: {0}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }
}
