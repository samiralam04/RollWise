package com.attendance.dao;

import com.attendance.model.Student;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class StudentDAO {

    private static final Logger LOGGER = Logger.getLogger(StudentDAO.class.getName());

    // Add a new student
    public boolean addStudent(Student student) {
        String query = "INSERT INTO student (name, email, phone, roll_number, class_name, parent_phone) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, student.getName());
            stmt.setString(2, student.getEmail());
            stmt.setString(3, student.getPhone());
            stmt.setString(4, student.getRollNumber());
            stmt.setString(5, student.getClassName());
            stmt.setString(6, student.getParentPhone());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error adding student: " + e.getMessage());
        }
        return false;
    }

    // Get all students
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String query = "SELECT * FROM student ORDER BY class_name, roll_number";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving students: " + e.getMessage());
        }
        return students;
    }

    // Get a student by roll number
    public Student getStudentByRollNumber(String rollNumber) {
        String query = "SELECT * FROM student WHERE roll_number = ?";
        return getStudentByQuery(query, rollNumber);
    }

    // Get a student by ID
    public Student getStudentById(int id) {
        String query = "SELECT * FROM student WHERE id = ?";
        return getStudentByQuery(query, id);
    }

    private Student getStudentByQuery(String query, Object param) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (param instanceof String) {
                stmt.setString(1, (String) param);
            } else if (param instanceof Integer) {
                stmt.setInt(1, (Integer) param);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching student: " + e.getMessage());
        }
        return null;
    }

    // Update student details
    public boolean updateStudent(Student student) {
        String query = "UPDATE student SET name=?, email=?, phone=?, class_name=?, parent_phone=? WHERE roll_number=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, student.getName());
            stmt.setString(2, student.getEmail());
            stmt.setString(3, student.getPhone());
            stmt.setString(4, student.getClassName());
            stmt.setString(5, student.getParentPhone());
            stmt.setString(6, student.getRollNumber());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error updating student: " + e.getMessage());
        }
        return false;
    }

    // Get attendance percentage
    public double getAttendancePercentage(int studentId) {
        String query = "SELECT (COUNT(CASE WHEN status = 'Present' THEN 1 END) * 100.0 / COUNT(*)) AS attendance_percentage FROM attendance WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("attendance_percentage");
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching attendance percentage: " + e.getMessage());
        }
        return 0.0; // Default if no records found
    }

    // Get students with low attendance
    public List<Student> getStudentsWithLowAttendance(int threshold) {
        return getStudentsByAttendanceQuery("HAVING (COUNT(CASE WHEN a.status = 'Present' THEN 1 END) * 100.0 / COUNT(*)) < ?", threshold);
    }

    // Get students near attendance threshold
    public List<Student> getStudentsWithAttendanceNearThreshold(int threshold) {
        return getStudentsByAttendanceQuery("HAVING (COUNT(CASE WHEN a.status = 'Present' THEN 1 END) * 100.0 / COUNT(*)) BETWEEN ? - 5 AND ? + 5", threshold, threshold);
    }

    private List<Student> getStudentsByAttendanceQuery(String condition, Object... params) {
        List<Student> students = new ArrayList<>();
        String query = "SELECT s.* FROM student s JOIN attendance a ON s.id = a.student_id GROUP BY s.id " + condition;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching students with attendance conditions: " + e.getMessage());
        }
        return students;
    }

    // Delete a student by ID
    public boolean deleteStudentById(int studentId) {
        String query = "DELETE FROM student WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error deleting student: " + e.getMessage());
        }
        return false;
    }

    // Helper method to map ResultSet to Student object
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("roll_number"),
                rs.getString("class_name"),
                rs.getString("parent_phone")
        );
    }
}
