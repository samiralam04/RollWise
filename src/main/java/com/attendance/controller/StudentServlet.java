package com.attendance.controller;

import com.attendance.model.Student;
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


public class StudentServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer studentId = (Integer) session.getAttribute("studentId");

        if (studentId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Fetch student details
            String studentQuery = "SELECT id, name, email, phone, parent_phone FROM users WHERE id = ? AND role = 'student'";
            PreparedStatement studentStmt = conn.prepareStatement(studentQuery);
            studentStmt.setInt(1, studentId);
            ResultSet studentRs = studentStmt.executeQuery();

            if (studentRs.next()) {
                Student student = new Student(
                        studentRs.getInt("id"),
                        studentRs.getString("name"),
                        studentRs.getString("email"),
                        studentRs.getString("phone"),
                        studentRs.getString("parent_phone")
                );

                request.setAttribute("student", student);
            }

            // Fetch student attendance report
            String attendanceQuery = "SELECT subject, total_classes, attended_classes, (attended_classes * 100.0 / total_classes) AS percentage " +
                    "FROM attendance WHERE student_id = ?";
            PreparedStatement attendanceStmt = conn.prepareStatement(attendanceQuery);
            attendanceStmt.setInt(1, studentId);
            ResultSet attendanceRs = attendanceStmt.executeQuery();

            request.setAttribute("attendanceReport", attendanceRs);
            request.getRequestDispatcher("studentDashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("Error retrieving student data.");
        }
    }
}
