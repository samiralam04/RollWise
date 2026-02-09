package com.attendance.dao;

import com.attendance.model.Student;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class StudentDAO {

    private static final Logger LOGGER = Logger.getLogger(StudentDAO.class.getName());

    private static final String BASE_QUERY = "SELECT s.id, u.username as name, u.email, '' as phone, s.roll_number, c.class_name, '' as parent_phone "
            +
            "FROM students s " +
            "JOIN users u ON s.user_id = u.id " +
            "LEFT JOIN classes c ON s.class_id = c.id ";

    // Add a new student
    public boolean addStudent(Student student) {
        int classId = getOrCreateClassId(student.getClassName());

        String userQuery = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, 'student') RETURNING id";
        String studentQuery = "INSERT INTO students (user_id, roll_number, class_id, student_email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId = -1;
                try (PreparedStatement uStmt = conn.prepareStatement(userQuery)) {
                    uStmt.setString(1, student.getName());
                    uStmt.setString(2, student.getEmail());
                    uStmt.setString(3, "password123"); // Default password
                    try (ResultSet rs = uStmt.executeQuery()) {
                        if (rs.next())
                            userId = rs.getInt(1);
                    }
                }

                if (userId != -1) {
                    try (PreparedStatement sStmt = conn.prepareStatement(studentQuery)) {
                        sStmt.setInt(1, userId);
                        sStmt.setString(2, student.getRollNumber());
                        sStmt.setInt(3, classId); // Dynamic class ID
                        sStmt.setString(4, student.getEmail());
                        sStmt.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                LOGGER.severe("Error adding student: " + e.getMessage());
            }
        } catch (SQLException e) {
            LOGGER.severe("DB Error: " + e.getMessage());
        }
        return false;
    }

    // Helper to get Class ID by Name, or create if not exists
    public int getOrCreateClassId(String className) {
        if (className == null || className.trim().isEmpty()) {
            return 1; // Default if no class provided
        }

        String selectQuery = "SELECT id FROM classes WHERE class_name = ?";
        String insertQuery = "INSERT INTO classes (class_name) VALUES (?) RETURNING id";

        try (Connection conn = DBConnection.getConnection()) {
            // Try to find existing
            try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setString(1, className);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }

            // If not found, create new
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, className);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }

        } catch (SQLException e) {
            LOGGER.severe("Error fetching/creating class ID: " + e.getMessage());
        }
        return 1; // Default fallback
    }

    // Get all students
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String query = BASE_QUERY + "ORDER BY c.class_name, s.roll_number";

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
        String query = BASE_QUERY + "WHERE s.roll_number = ?";
        return getStudentByQuery(query, rollNumber);
    }

    // Get a student by ID
    public Student getStudentById(int id) {
        String query = BASE_QUERY + "WHERE s.id = ?";
        return getStudentByQuery(query, id);
    }

    // Get a student by Email
    public Student getStudentByEmail(String email) {
        String query = BASE_QUERY + "WHERE u.email = ?";
        return getStudentByQuery(query, email);
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
        String query = "UPDATE students SET roll_number=? WHERE id=?"; // Simplified for demonstration
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, student.getRollNumber());
            stmt.setInt(2, student.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error updating student: " + e.getMessage());
        }
        return false;
    }

    // Get attendance percentage
    public double getAttendancePercentage(int studentId) {
        String query = "SELECT (COUNT(CASE WHEN status = 'Present' THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0)) AS attendance_percentage FROM attendance WHERE student_id = ?";

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
        return 0.0;
    }

    // Get students with low attendance
    public List<Student> getStudentsWithLowAttendance(int threshold) {
        return getStudentsByAttendanceQuery(
                "HAVING (COUNT(CASE WHEN a.status = 'Present' THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0)) < ?",
                threshold);
    }

    // Get students near attendance threshold
    public List<Student> getStudentsWithAttendanceNearThreshold(int threshold) {
        return getStudentsByAttendanceQuery(
                "HAVING (COUNT(CASE WHEN a.status = 'Present' THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0)) BETWEEN ? - 5 AND ? + 5",
                threshold, threshold);
    }

    private List<Student> getStudentsByAttendanceQuery(String condition, Object... params) {
        List<Student> students = new ArrayList<>();
        String query = "SELECT s.id, u.username as name, u.email, '' as phone, s.roll_number, c.class_name, '' as parent_phone "
                +
                "FROM students s " +
                "JOIN users u ON s.user_id = u.id " +
                "LEFT JOIN classes c ON s.class_id = c.id " +
                "JOIN attendance a ON s.id = a.student_id " +
                "GROUP BY s.id, u.username, u.email, s.roll_number, c.class_name " + condition;

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
        String query = "DELETE FROM students WHERE id = ?";
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
                rs.getString("parent_phone"));
    }
}
