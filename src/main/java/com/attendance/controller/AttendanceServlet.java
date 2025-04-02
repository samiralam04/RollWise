package com.attendance.controller;

import com.attendance.service.AttendanceNotifier;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/attendance_system";
    private static final String DB_USER = "attendance_system";
    private static final String DB_PASSWORD = "mark47";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        String studentIdStr = request.getParameter("student_id");
        String date = request.getParameter("date");
        String status = request.getParameter("status");
        String teacherIdStr = request.getParameter("teacher_id");

        if (studentIdStr == null || date == null || status == null || teacherIdStr == null ||
                studentIdStr.trim().isEmpty() || date.trim().isEmpty() || status.trim().isEmpty() || teacherIdStr.trim().isEmpty()) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Invalid input. Please provide student_id, date, status, and teacher_id.");
            out.print(jsonResponse.toString());
            return;
        }

        int studentId, teacherId;
        try {
            studentId = Integer.parseInt(studentIdStr);
            teacherId = Integer.parseInt(teacherIdStr);
        } catch (NumberFormatException e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Invalid student_id or teacher_id format. Must be a number.");
            out.print(jsonResponse.toString());
            return;
        }

        // Get the logged-in teacher ID from the session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInTeacherId") == null) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Teacher not logged in. Please log in first.");
            out.print(jsonResponse.toString());
            return;
        }

        int loggedInTeacherId = (int) session.getAttribute("loggedInTeacherId");
        if (loggedInTeacherId != teacherId) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Unauthorized action. Teacher ID mismatch.");
            out.print(jsonResponse.toString());
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String checkQuery = "SELECT status FROM attendance WHERE student_id = ? AND date = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, studentId);
                checkStmt.setDate(2, java.sql.Date.valueOf(date));
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String currentStatus = rs.getString("status");

                    if (currentStatus.equalsIgnoreCase(status)) {
                        jsonResponse.put("success", false);
                        jsonResponse.put("message", "Attendance is already marked as " + status + " for this date.");
                        out.print(jsonResponse.toString());
                        return;
                    }

                    // Update the status
                    String updateQuery = "UPDATE attendance SET status = ?, recorded_at = NOW() WHERE student_id = ? AND date = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, status);
                        updateStmt.setInt(2, studentId);
                        updateStmt.setDate(3, java.sql.Date.valueOf(date));

                        int rows = updateStmt.executeUpdate();
                        if (rows > 0) {
                            if (isAttendanceBelowThreshold(conn, studentId)) {
                                AttendanceNotifier.sendAttendanceEmails();
                            }
                            jsonResponse.put("success", true);
                            jsonResponse.put("message", "Attendance updated successfully: " + status);
                            out.print(jsonResponse.toString());
                        } else {
                            jsonResponse.put("success", false);
                            jsonResponse.put("message", "Failed to update attendance.");
                            out.print(jsonResponse.toString());
                        }
                    }
                } else {
                    // Insert new attendance record
                    String insertQuery = "INSERT INTO attendance (student_id, date, status, recorded_at, teacher_id) VALUES (?, ?, ?, NOW(), ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, studentId);
                        insertStmt.setDate(2, java.sql.Date.valueOf(date));
                        insertStmt.setString(3, status);
                        insertStmt.setInt(4, teacherId);

                        int rows = insertStmt.executeUpdate();
                        if (rows > 0) {
                            if (isAttendanceBelowThreshold(conn, studentId)) {
                                AttendanceNotifier.sendAttendanceEmails();
                            }
                            jsonResponse.put("success", true);
                            jsonResponse.put("message", "Attendance marked successfully: " + status);
                            out.print(jsonResponse.toString());
                        } else {
                            jsonResponse.put("success", false);
                            jsonResponse.put("message", "Failed to mark attendance.");
                            out.print(jsonResponse.toString());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error saving attendance: " + e.getMessage());
            out.print(jsonResponse.toString());
        }
    }

    // Check if student's attendance falls below 75%
    private boolean isAttendanceBelowThreshold(Connection conn, int studentId) {
        String query = "SELECT (COUNT(CASE WHEN status = 'Present' THEN 1 END) * 100.0 / COUNT(*)) AS percentage " +
                "FROM attendance WHERE student_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double percentage = rs.getDouble("percentage");
                return percentage < 75;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
