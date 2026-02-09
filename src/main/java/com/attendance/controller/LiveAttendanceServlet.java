package com.attendance.controller;

import com.attendance.dao.AuditLogDAO;
import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.model.Student;
import com.attendance.service.PythonAIService;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("/live-attendance")
@MultipartConfig
public class LiveAttendanceServlet extends HttpServlet {

    private PythonAIService pythonAIService;
    private AttendanceDAO attendanceDAO;
    private AuditLogDAO auditLogDAO;

    @Override
    public void init() throws ServletException {
        pythonAIService = new PythonAIService();
        attendanceDAO = new AttendanceDAO();
        auditLogDAO = new AuditLogDAO();
    }

    private void sendJsonResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", status == HttpServletResponse.SC_OK || status == 200 ? "success" : "error");
        jsonResponse.put("message", message);
        response.getWriter().write(jsonResponse.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Please login first");
            return;
        }

        String email = (String) session.getAttribute("email");
        Part filePart = request.getPart("image");
        if (filePart == null || filePart.getSize() == 0) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No image captured");
            return;
        }

        // 1. Resolve Student ID from User Email
        Student student = new StudentDAO().getStudentByEmail(email);
        if (student == null) {
            Object userIdObj = session.getAttribute("userId");
            int userId = (userIdObj instanceof Integer) ? (Integer) userIdObj : -1;
            auditLogDAO.logAction(userId, "SELF_ATTENDANCE_FAIL",
                    "User email not found in student records: " + email, "STUDENT", "FAILURE");
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Student record not found for this user.");
            return;
        }
        int studentId = student.getId();

        // 2. Fetch Reference Face Encoding
        com.attendance.dao.StudentFaceDataDAO faceDAO = new com.attendance.dao.StudentFaceDataDAO();
        String referenceEncoding = faceDAO.getFaceEncoding(studentId);

        if (referenceEncoding == null) {
            auditLogDAO.logAction(studentId, "SELF_ATTENDANCE_FAIL", "No face encoding found", "STUDENT", "FAILURE");
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Face not enrolled. Please ask admin to enroll your face first.");
            return;
        }

        // 3. Perform Liveness & Identity Check
        try (InputStream imageStream = filePart.getInputStream()) {
            String contentType = filePart.getContentType();
            JSONObject result = pythonAIService.verifyLiveness(imageStream, "capture.jpg", contentType,
                    referenceEncoding);

            if (result != null && result.optBoolean("verified")) {
                if (!result.optBoolean("blink_detected")) {
                    auditLogDAO.logAction(studentId, "SELF_ATTENDANCE_FAIL", "Blink not detected", "STUDENT",
                            "FAILURE");
                    sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Face matched, but please blink your eyes clearly to confirm liveness.");
                    return;
                }

                // Check Rate Limit (1 attempt per day - support updating 'Absent' to 'Present')
                java.time.LocalDate today = java.time.LocalDate.now();
                java.util.List<com.attendance.model.Attendance> history = attendanceDAO
                        .getAttendanceByStudent(studentId);

                com.attendance.model.Attendance existingRecord = history.stream()
                        .filter(a -> a.getDate().isEqual(today))
                        .findFirst()
                        .orElse(null);

                if (existingRecord != null) {
                    if ("Present".equals(existingRecord.getStatus())) {
                        auditLogDAO.logAction(studentId, "SELF_ATTENDANCE_FAIL", "Rate Limit: Already Present today",
                                "STUDENT", "FAILURE");
                        sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                "You have already marked attendance (Present) for today.");
                        return;
                    } else {
                        // Update existing entry (e.g., from 'Absent' to 'Present')
                        boolean updated = attendanceDAO.updateAttendance(existingRecord.getId(), "Present");
                        if (updated) {
                            auditLogDAO.logAction(studentId, "SELF_ATTENDANCE_SUCCESS",
                                    "Status updated to Present", "STUDENT", "SUCCESS");
                            sendJsonResponse(response, HttpServletResponse.SC_OK, "Attendance updated to Present!");
                        } else {
                            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                    "Failed to update record.");
                        }
                        return;
                    }
                }

                // Mark attendance
                boolean marked = attendanceDAO.markAttendance(studentId, "Present", 0); // 0 for self/system

                if (marked) {
                    auditLogDAO.logAction(studentId, "SELF_ATTENDANCE_SUCCESS",
                            "Liveness: " + result.optString("liveness"), "STUDENT", "SUCCESS");
                    sendJsonResponse(response, HttpServletResponse.SC_OK, "Attendance marked successful!");
                } else {
                    auditLogDAO.logAction(studentId, "SELF_ATTENDANCE_FAIL", "Database error", "STUDENT", "FAILURE");
                    sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
                }
            } else {
                String failMsg = result != null ? result.optString("message", "Face match failed.")
                        : "Liveness check failed.";
                System.out.println("400 Error: " + failMsg); // Server-side log
                auditLogDAO.logAction(studentId, "SELF_ATTENDANCE_FAIL", failMsg, "STUDENT", "FAILURE");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, failMsg);
            }
        } catch (Exception e) {
            Object userIdObj = session.getAttribute("userId");
            int userId = (userIdObj instanceof Integer) ? (Integer) userIdObj : -1;
            auditLogDAO.logAction(userId, "SELF_ATTENDANCE_ERROR", e.getMessage(), "STUDENT", "FAILURE");
            e.printStackTrace();
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
}
