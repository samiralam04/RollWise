package com.attendance.controller;

import com.attendance.model.Teacher;
import com.attendance.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/teacher")
public class TeacherServlet extends HttpServlet {

    // Handles listing teachers and fetching details for editing
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username"); // Retrieve logged-in user

        if (username == null || !isAdmin(username)) { // Check if user is admin
            response.sendRedirect("/pages/login.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("edit".equals(action)) {
            // Fetch teacher by ID for editing
            int teacherId = Integer.parseInt(request.getParameter("id"));
            Teacher teacher = getTeacherById(teacherId);
            request.setAttribute("teacher", teacher);
            request.getRequestDispatcher("/pages/editTeacher.jsp").forward(request, response);
        } else {
            // List all teachers
            List<Teacher> teacherList = getAllTeachers();
            request.setAttribute("teachers", teacherList);
            request.getRequestDispatcher("/pages/manage-teachers.jsp").forward(request, response);
        }
    }

    // Handles adding, updating, and deleting teachers
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username"); // Use session username

        if (username == null) {
            response.sendRedirect("/pages/login.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            // Delete teacher
            int teacherId = Integer.parseInt(request.getParameter("id"));
            deleteTeacher(teacherId);
            response.sendRedirect(request.getContextPath() + "/teacher");
        } else if ("update".equals(action)) {
            // Update teacher details
            int teacherId = Integer.parseInt(request.getParameter("id"));
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String subject = request.getParameter("subject");
            String usernameTeacher = request.getParameter("username");

            updateTeacher(teacherId, name, email, phone, subject, usernameTeacher);
            response.sendRedirect(request.getContextPath() + "/teacher");
        } else {
            // Add new teacher
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String subject = request.getParameter("subject");
            String usernameTeacher = request.getParameter("username");
            String password = request.getParameter("password");

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO teacher (name, email, phone, subject, username, password) VALUES (?, ?, ?, ?, ?, ?)")) {

                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, phone);
                stmt.setString(4, subject);
                stmt.setString(5, usernameTeacher);
                stmt.setString(6, hashedPassword);

                stmt.executeUpdate();
                response.sendRedirect(request.getContextPath() + "/teacher");

            } catch (SQLException e) {
                e.printStackTrace();
                request.setAttribute("error", "Database error while adding teacher.");
                request.getRequestDispatcher("/pages/manage-teachers.jsp").forward(request, response);
            }
        }
    }

    private boolean isAdmin(String username) {
        boolean isAdmin = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT role FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    isAdmin = "admin".equalsIgnoreCase(rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isAdmin;
    }

    private Teacher getTeacherById(int id) {
        Teacher teacher = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name, email, phone, subject, username FROM teacher WHERE id = ?")) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    teacher = new Teacher(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("subject"),
                            rs.getString("username")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teacher;
    }

    private List<Teacher> getAllTeachers() {
        List<Teacher> teacherList = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name, email, phone, subject, username FROM teacher");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Teacher teacher = new Teacher(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("subject"),
                        rs.getString("username")
                );
                teacherList.add(teacher);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teacherList;
    }

    private void updateTeacher(int id, String name, String email, String phone, String subject, String username) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE teacher SET name = ?, email = ?, phone = ?, subject = ?, username = ? WHERE id = ?")) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, subject);
            stmt.setString(5, username);
            stmt.setInt(6, id);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteTeacher(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM teacher WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
