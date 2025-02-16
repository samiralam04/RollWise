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
import java.sql.ResultSet;
import java.sql.SQLException;


public class HolidayServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String date = request.getParameter("date");
        String reason = request.getParameter("reason");

        if (date == null || date.trim().isEmpty() || reason == null || reason.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Date and reason are required.");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO holidays (date, reason) VALUES (?, ?)")) {

            stmt.setString(1, date);
            stmt.setString(2, reason);
            stmt.executeUpdate();

            response.sendRedirect("adminDashboard.jsp?success=Holiday added");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error adding holiday.");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM holidays ORDER BY date DESC");
             ResultSet rs = stmt.executeQuery()) {

            StringBuilder holidays = new StringBuilder();
            while (rs.next()) {
                holidays.append("<tr>")
                        .append("<td>").append(rs.getString("date")).append("</td>")
                        .append("<td>").append(rs.getString("reason")).append("</td>")
                        .append("</tr>");
            }

            request.setAttribute("holidays", holidays.toString());
            request.getRequestDispatcher("holidays.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving holidays.");
        }
    }
}
