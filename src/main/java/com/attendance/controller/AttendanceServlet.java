package com.attendance.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // PostgreSQL Database connection parameters
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/attendance_system";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "your_password";

    // Load PostgreSQL Driver
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // GET request: Retrieve all attendance records
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM attendance ORDER BY recorded_at DESC")) {

            ResultSet rs = stmt.executeQuery();
            out.println("<h2>Attendance Records</h2>");
            out.println("<table border='1'><tr><th>Student ID</th><th>Date</th><th>Status</th><th>Recorded At</th></tr>");

            while (rs.next()) {
                out.println("<tr><td>" + rs.getInt("student_id") + "</td><td>" + rs.getDate("date") +
                        "</td><td>" + rs.getString("status") + "</td><td>" + rs.getTimestamp("recorded_at") + "</td></tr>");
            }
            out.println("</table>");
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<p>Error fetching attendance data.</p>");
        }
    }

    // POST request: Mark attendance (Present/Absent)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String studentIdStr = request.getParameter("student_id");
        String status = request.getParameter("status");

        // Validate input
        if (studentIdStr == null || status == null || studentIdStr.isEmpty() || status.isEmpty()) {
            out.println("<p>Invalid input. Please provide student_id and status (Present/Absent).</p>");
            return;
        }

        int studentId;
        try {
            studentId = Integer.parseInt(studentIdStr);
        } catch (NumberFormatException e) {
            out.println("<p>Invalid student_id format. Must be a number.</p>");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO attendance (student_id, date, status, recorded_at) VALUES (?, CURRENT_DATE, ?, NOW())")) {

            stmt.setInt(1, studentId);
            stmt.setString(2, status);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                out.println("<p>Attendance marked successfully for Student ID: " + studentId + " as " + status + ".</p>");
            } else {
                out.println("<p>Failed to mark attendance.</p>");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<p>Error saving attendance.</p>");
        }
    }
}
