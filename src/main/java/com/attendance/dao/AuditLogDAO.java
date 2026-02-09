package com.attendance.dao;

import com.attendance.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class AuditLogDAO {
    private static final Logger LOGGER = Logger.getLogger(AuditLogDAO.class.getName());

    public void logAction(Integer studentId, String action, String details, String actorRole, String actionResult) {
        String query = "INSERT INTO attendance_audit_log (student_id, action, details, actor_role, action_result) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            if (studentId != null) {
                stmt.setInt(1, studentId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.setString(4, actorRole);
            stmt.setString(5, actionResult);

            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Error logging action: " + e.getMessage());
        }
    }
}
