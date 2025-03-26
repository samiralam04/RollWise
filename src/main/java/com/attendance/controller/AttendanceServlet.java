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
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String studentIdStr = request.getParameter("student_id");
        String date = request.getParameter("date");
        String status = request.getParameter("status");
        String teacherIdStr = request.getParameter("teacher_id");

        if (studentIdStr == null || date == null || status == null || teacherIdStr == null ||
                studentIdStr.trim().isEmpty() || date.trim().isEmpty() || status.trim().isEmpty() || teacherIdStr.trim().isEmpty()) {
            out.println("<script>alert('Invalid input. Please provide student_id, date, status, and teacher_id.'); window.history.back();</script>");
            return;
        }

        int studentId, teacherId;
        try {
            studentId = Integer.parseInt(studentIdStr);
            teacherId = Integer.parseInt(teacherIdStr);
        } catch (NumberFormatException e) {
            out.println("<script>alert('Invalid student_id or teacher_id format. Must be a number.'); window.history.back();</script>");
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
                        out.println("<script>alert('Attendance is already marked as " + status + " for this date.'); window.history.back();</script>");
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
                            // 🔹 Trigger email after updating attendance
                            sendAttendanceEmails(studentId, status);
                            out.println("<script>alert('Attendance updated successfully: " + status + "'); window.history.back();</script>");
                        } else {
                            out.println("<script>alert('Failed to update attendance.'); window.history.back();</script>");
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
                            // 🔹 Trigger email after inserting new attendance
                            sendAttendanceEmails(studentId, status);
                            out.println("<script>alert('Attendance marked successfully: " + status + "'); window.history.back();</script>");
                        } else {
                            out.println("<script>alert('Failed to mark attendance.'); window.history.back();</script>");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<script>alert('Error saving attendance: " + e.getMessage() + "'); window.history.back();</script>");
        }
    }

    // 🔹 Email Notification Method
    private void sendAttendanceEmails(int studentId, String status) {
        // Implement your email sending logic here
        System.out.println("📧 Sending email for student ID: " + studentId + " | Status: " + status);

        // Sample Logic (Replace with actual email sending code)
        if (status.equalsIgnoreCase("absent")) {
            System.out.println("⚠ Alert: Student ID " + studentId + " was marked Absent. Sending email...");
        } else {
            System.out.println("✅ Student ID " + studentId + " is present.");
        }
    }
}
