package com.attendance.dao;

import com.attendance.model.Teacher;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO {

    // Add a new teacher
    public boolean addTeacher(Teacher teacher) {
        String query = "INSERT INTO teacher (name, email, phone, subject, username, password) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, teacher.getName());
            stmt.setString(2, teacher.getEmail());
            stmt.setString(3, teacher.getPhone());  // Instead of teacher.getPhone().orElse(null)
            stmt.setString(4, teacher.getSubject()); // Instead of teacher.getSubject().orElse(null)
            stmt.setString(5, teacher.getUsername());
            stmt.setString(6, teacher.getPassword());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Get all teachers
    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        String query = "SELECT * FROM teacher ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                teachers.add(new Teacher(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("subject"),
                        rs.getString("username"),
                        rs.getString("password")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teachers;
    }

    // Get a teacher by username
    public Teacher getTeacherByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ? AND role = 'teacher'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Teacher(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("subject"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get a teacher by ID
    public Teacher getTeacherById(int id) {
        String query = "SELECT * FROM users WHERE id = ? AND role = 'teacher'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Teacher(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("subject"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Validate teacher credentials
    public boolean validateTeacher(String email, String password) {
        String query = "SELECT EXISTS (SELECT 1 FROM users WHERE email = ? AND password = ? AND role = 'teacher')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update teacher details
    public boolean updateTeacher(Teacher teacher) {
        String query = "UPDATE users SET name=?, email=?, phone=?, subject=?, password=? WHERE username=? AND role = 'teacher'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, teacher.getName());
            stmt.setString(2, teacher.getEmail());
            stmt.setString(3, teacher.getPhone());  // Instead of teacher.getPhone().orElse(null)
            stmt.setString(4, teacher.getSubject()); // Instead of teacher.getSubject().orElse(null)
            stmt.setString(5, teacher.getPassword());
            stmt.setString(6, teacher.getUsername());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete a teacher by ID
    public boolean deleteTeacher(int teacherId) {
        String query = "DELETE FROM users WHERE id = ? AND role = 'teacher'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, teacherId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
