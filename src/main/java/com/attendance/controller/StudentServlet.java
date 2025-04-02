package com.attendance.controller;

import com.attendance.model.Attendance;
import com.attendance.model.Student;
import com.attendance.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

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

    // Handles GET requests for student data and attendance records
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            handleDelete(request, response);
            return;
        }

        HttpSession session = request.getSession();
        Integer studentId = (Integer) session.getAttribute("studentId");

        System.out.println("[DEBUG] Session Student ID: " + studentId);

        if (studentId == null) {
            System.out.println("[ERROR] Student ID is null. Redirecting to login.jsp");
            response.sendRedirect("login.jsp");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("[DEBUG] Database connection established.");

            // Fetch student details from the database
            String studentQuery = "SELECT id, username, email, phone, parent_phone FROM users WHERE id = ? AND role = 'student'";
            PreparedStatement studentStmt = conn.prepareStatement(studentQuery);
            studentStmt.setInt(1, studentId);
            ResultSet studentRs = studentStmt.executeQuery();

            if (studentRs.next()) {
                Student student = new Student(
                        studentRs.getInt("id"),
                        studentRs.getString("username"),
                        studentRs.getString("email"),
                        studentRs.getString("phone"),
                        studentRs.getString("parent_phone")
                );
                request.setAttribute("student", student);
                System.out.println("[DEBUG] Student Found: " + student.getName());
            } else {
                System.out.println("[ERROR] No student found with ID: " + studentId);
            }

            // Fetch student attendance report
            String attendanceQuery = "SELECT id, student_id, date, status, recorded_at, teacher_id FROM attendance WHERE student_id = ?";
            PreparedStatement attendanceStmt = conn.prepareStatement(attendanceQuery);
            attendanceStmt.setInt(1, studentId);
            ResultSet attendanceRs = attendanceStmt.executeQuery();

            List<Attendance> attendanceList = new ArrayList<>();
            System.out.println("[DEBUG] Fetching attendance records for Student ID: " + studentId);

            while (attendanceRs.next()) {
                int id = attendanceRs.getInt("id");
                Date sqlDate = attendanceRs.getDate("date");
                LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                String status = attendanceRs.getString("status");
                Timestamp sqlTimestamp = attendanceRs.getTimestamp("recorded_at");
                LocalDateTime recordedAt = (sqlTimestamp != null) ? sqlTimestamp.toLocalDateTime() : null;
                int teacherId = attendanceRs.getInt("teacher_id");

                Attendance attendance = new Attendance(id, studentId, localDate, status, recordedAt, teacherId);
                attendanceList.add(attendance);

                System.out.println("[DEBUG] Attendance Record: ID=" + id + ", Date=" + localDate + ", Status=" + status);
            }
            System.out.println("[DEBUG] Total Attendance Records Found: " + attendanceList.size());

            // Store attendance list in request scope
            request.setAttribute("attendanceReport", attendanceList);
            request.getRequestDispatcher("studentDashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ERROR] SQL Exception: " + e.getMessage());
            response.getWriter().write("Error retrieving student data.");
        }
    }

    // Handles POST requests to add new students or delete students
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            handleDelete(request, response);
            return;
        }

        // Existing add student functionality
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        System.out.println("[DEBUG] Adding new student: " + name + " (" + email + ")");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                // Hash the password using BCrypt
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                // SQL query to insert a new student with the "student" role and hashed password
                String sql = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, 'student')";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, hashedPassword);

                int result = ps.executeUpdate();

                if (result > 0) {
                    System.out.println("[DEBUG] Student added successfully.");
                    response.sendRedirect(request.getContextPath() + "/pages/manage-student.jsp");
                } else {
                    System.out.println("[ERROR] Failed to add student.");
                    response.getWriter().println("Failed to add student.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ERROR] SQL Exception: " + e.getMessage());
            response.getWriter().write("Error adding student.");
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Student ID is required");
            return;
        }

        try {
            int studentId = Integer.parseInt(idParam);
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM users WHERE id = ? AND role = 'student'";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, studentId);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    if ("POST".equalsIgnoreCase(request.getMethod())) {
                        response.sendRedirect(request.getContextPath() + "/pages/manage-student.jsp");
                    } else {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write("Student deleted successfully");
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
                }
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid student ID format");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}