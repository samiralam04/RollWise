package com.attendance.controller;

import com.attendance.util.DBConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

        // Insert data into database
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO emergency (title, description, date) VALUES (?, ?, ?)")) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setDate(3, sqlDate);
            stmt.executeUpdate();

            // Redirect with success message
            response.sendRedirect(request.getContextPath() + "/pages/emergency.jsp?success=Emergency%20holiday%20added");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error: {0}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }
}
