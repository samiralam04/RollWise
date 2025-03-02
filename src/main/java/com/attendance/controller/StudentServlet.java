package com.attendance.controller;

import com.attendance.model.Attendance;
import com.attendance.model.Student;
import com.attendance.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/student")
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
            String attendanceQuery = "SELECT id, student_id, date, status, recorded_at, teacher_id FROM attendance WHERE student_id = ?";
            PreparedStatement attendanceStmt = conn.prepareStatement(attendanceQuery);
            attendanceStmt.setInt(1, studentId);
            ResultSet attendanceRs = attendanceStmt.executeQuery();

            List<Attendance> attendanceList = new ArrayList<>();

            while (attendanceRs.next()) {
                int id = attendanceRs.getInt("id");
                int studentIdDb = attendanceRs.getInt("student_id");

                // Convert java.sql.Date to java.time.LocalDate
                Date sqlDate = attendanceRs.getDate("date");
                LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                String status = attendanceRs.getString("status");

                // Convert java.sql.Timestamp to java.time.LocalDateTime
                Timestamp sqlTimestamp = attendanceRs.getTimestamp("recorded_at");
                LocalDateTime recordedAt = (sqlTimestamp != null) ? sqlTimestamp.toLocalDateTime() : null;

                int teacherId = attendanceRs.getInt("teacher_id");

                // Create Attendance object
                Attendance attendance = new Attendance(id, studentIdDb, localDate, status, recordedAt, teacherId);
                attendanceList.add(attendance);
            }

            // Store attendance list in request scope
            request.setAttribute("attendanceReport", attendanceList);
            request.getRequestDispatcher("studentDashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("Error retrieving student data.");
        }
    }
}
