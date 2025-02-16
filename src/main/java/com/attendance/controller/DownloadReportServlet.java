package com.attendance.controller;

import com.attendance.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DownloadReportServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String studentId = request.getParameter("studentId");

        if (studentId == null || studentId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Student ID is required");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT date, status FROM attendance WHERE student_id = ? ORDER BY date ASC")) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder csvData = new StringBuilder("Date,Status\n");

            while (rs.next()) {
                csvData.append(rs.getString("date")).append(",").append(rs.getString("status")).append("\n");
            }

            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=attendance_report.csv");

            try (PrintWriter out = response.getWriter()) {
                out.write(csvData.toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating report");
        }
    }
}
