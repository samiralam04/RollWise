package com.attendance.controller;

import com.attendance.model.Teacher;
import com.attendance.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Servlet URL mapping
@WebServlet("/teacher")
public class TeacherServlet extends HttpServlet {

    // Handles teacher details retrieval
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer teacherId = (Integer) session.getAttribute("teacherId");

        if (teacherId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Fetch teacher details
            String teacherQuery = "SELECT id, name, email, phone FROM users WHERE id = ? AND role = 'teacher'";
            PreparedStatement teacherStmt = conn.prepareStatement(teacherQuery);
            teacherStmt.setInt(1, teacherId);
            ResultSet teacherRs = teacherStmt.executeQuery();

            if (teacherRs.next()) {
                Teacher teacher = new Teacher(
                        teacherRs.getInt("id"),
                        teacherRs.getString("name"),
                        teacherRs.getString("email"),
                        teacherRs.getString("phone")
                );
                request.setAttribute("teacher", teacher);
            }

            request.getRequestDispatcher("teacherDashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("Error retrieving teacher data.");
        }
    }

    // Handles marking attendance
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer teacherId = (Integer) session.getAttribute("teacherId");

        if (teacherId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String studentId = request.getParameter("studentId");
        String status = request.getParameter("status"); // "Present" or "Absent"

        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connected to database.");

            // Step 1: Try to update attendance if already recorded for today
            String updateAttendanceQuery = "UPDATE attendance SET status = ? WHERE student_id = ? AND date = CURRENT_DATE";
            PreparedStatement updateStmt = conn.prepareStatement(updateAttendanceQuery);
            updateStmt.setString(1, status);
            updateStmt.setInt(2, Integer.parseInt(studentId));

            int rowsAffected = updateStmt.executeUpdate();
            System.out.println("Rows updated: " + rowsAffected);

            // Step 2: If no record exists for today, insert a new one
            if (rowsAffected == 0) {
                System.out.println("No record found, inserting new attendance.");
                String insertQuery = "INSERT INTO attendance (student_id, date, status, recorded_at) VALUES (?, CURRENT_DATE, ?, NOW())";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, Integer.parseInt(studentId));
                insertStmt.setString(2, status);
                insertStmt.executeUpdate();
            }

            response.sendRedirect("teacher?success=Attendance updated successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("Error updating attendance: " + e.getMessage());
        }
    }
}
