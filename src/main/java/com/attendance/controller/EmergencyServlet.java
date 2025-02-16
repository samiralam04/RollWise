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


public class EmergencyServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String reason = request.getParameter("reason");
        String date = request.getParameter("date");

        if (reason == null || reason.trim().isEmpty() || date == null || date.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Reason and date are required");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO emergency_holidays (date, reason) VALUES (?, ?)")) {

            stmt.setString(1, date);
            stmt.setString(2, reason);
            stmt.executeUpdate();

            response.sendRedirect("adminDashboard.jsp?success=Emergency holiday added");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error adding emergency holiday");
        }
    }
}
