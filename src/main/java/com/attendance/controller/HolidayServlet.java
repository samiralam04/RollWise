package com.attendance.controller;

import com.attendance.util.DBConnection;
import com.attendance.service.EmailNotifier;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/HolidayServlet")
public class HolidayServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(HolidayServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            deleteHoliday(request, response);
        } else {
            addHoliday(request, response);
        }
    }

    private void addHoliday(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String dateStr = request.getParameter("date");
        String reason = request.getParameter("reason");

        if (dateStr == null || dateStr.trim().isEmpty() || reason == null || reason.trim().isEmpty()) {
            request.setAttribute("error", "Date and reason are required.");
            displayHolidays(request, response);
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = sdf.parse(dateStr);
            Date sqlDate = new Date(parsedDate.getTime());

            try (Connection conn = DBConnection.getConnection()) {
                String checkQuery = "SELECT COUNT(*) FROM holiday WHERE \"date\" = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setDate(1, sqlDate);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        request.setAttribute("error", "Holiday already exists.");
                        displayHolidays(request, response);
                        return;
                    }
                }

                String insertQuery = "INSERT INTO holiday (\"date\", reason) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setDate(1, sqlDate);
                    stmt.setString(2, reason);
                    stmt.executeUpdate();
                }

                List<String> parentEmails = new ArrayList<>();
                String fetchParentEmailsQuery = "SELECT parent_email FROM parents";
                try (PreparedStatement emailStmt = conn.prepareStatement(fetchParentEmailsQuery);
                     ResultSet rs = emailStmt.executeQuery()) {
                    while (rs.next()) {
                        parentEmails.add(rs.getString("parent_email"));
                    }
                }

                if (!parentEmails.isEmpty()) {
                    EmailNotifier.sendHolidayNotification(parentEmails, dateStr, reason);
                    LOGGER.log(Level.INFO, "Holiday notification sent to parents.");
                }


                request.setAttribute("success", "Holiday added successfully and notification sent.");
            }
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, "Invalid date format: " + dateStr, e);
            request.setAttribute("error", "Invalid date format. Use YYYY-MM-DD.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error", e);
            request.setAttribute("error", "Error adding holiday.");
        }

        displayHolidays(request, response);
    }

    private void deleteHoliday(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String dateStr = request.getParameter("date");

        if (dateStr == null || dateStr.trim().isEmpty()) {
            request.setAttribute("error", "Invalid holiday date.");
            displayHolidays(request, response);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String deleteQuery = "DELETE FROM holiday WHERE \"date\" = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                stmt.setDate(1, Date.valueOf(dateStr));
                int rowsDeleted = stmt.executeUpdate();

                if (rowsDeleted > 0) {
                    request.setAttribute("success", "Holiday deleted successfully.");
                } else {
                    request.setAttribute("error", "Holiday not found.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting holiday", e);
            request.setAttribute("error", "Error deleting holiday.");
        }

        displayHolidays(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        displayHolidays(request, response);
    }

    private void displayHolidays(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<String[]> holidays = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM holiday ORDER BY \"date\" DESC");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                holidays.add(new String[]{rs.getDate("date").toString(), rs.getString("reason")});
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving holidays", e);
            request.setAttribute("error", "Error retrieving holidays.");
        }

        request.setAttribute("holidays", holidays);
        request.getRequestDispatcher("/pages/holiday.jsp").forward(request, response);
    }
}
