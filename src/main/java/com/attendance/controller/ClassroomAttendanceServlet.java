package com.attendance.controller;

import com.attendance.dao.AuditLogDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.StudentFaceDataDAO;
import com.attendance.dao.AttendanceDAO;
import com.attendance.model.Student;
import com.attendance.service.PythonAIService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/classroom-attendance")
@MultipartConfig
public class ClassroomAttendanceServlet extends HttpServlet {

    private PythonAIService pythonAIService;
    private StudentFaceDataDAO studentFaceDataDAO;
    private StudentDAO studentDAO;
    private AttendanceDAO attendanceDAO;
    private AuditLogDAO auditLogDAO;

    @Override
    public void init() throws ServletException {
        pythonAIService = new PythonAIService();
        studentFaceDataDAO = new StudentFaceDataDAO();
        studentDAO = new StudentDAO();
        attendanceDAO = new AttendanceDAO(); // Assuming this exists or I need to check
        auditLogDAO = new AuditLogDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String className = request.getParameter("className"); // e.g., "CSE-A"
        Part filePart = request.getPart("image");

        if (filePart == null || filePart.getSize() == 0 || className == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing image or class name");
            return;
        }

        try {
            // 1. Fetch all students in the class
            // Note: I need to check StudentDAO if it supports getStudentsByClass.
            // The existing getAllStudents returns all. I might need filter or add method.
            // For now, I'll fetch all and filter or add a method.
            // StudentDAO has getAllStudents(). I'll assume I can filter or I should add
            // getStudentsByClass.
            // Let's use getAllStudents and filter in memory for simplicity if creating new
            // DAO method is blocked.
            // Actually, I should check StudentDAO again. (It had no getByClass).
            // But wait, I can just fetch all encoded faces for the class?
            // Better to fetch all students, check if they have encoding.

            List<Student> allStudents = studentDAO.getAllStudents();
            Map<Integer, String> encodingsMap = new HashMap<>();

            for (Student s : allStudents) {
                if (className.equals(s.getClassName())) { // Simple filter
                    String encoding = studentFaceDataDAO.getFaceEncoding(s.getId());
                    if (encoding != null) {
                        encodingsMap.put(s.getId(), encoding);
                    }
                }
            }

            if (encodingsMap.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No enrolled students found for this class.");
                return;
            }

            // 2. Prepare JSON for Python
            JSONObject knownEncodings = new JSONObject();
            for (Map.Entry<Integer, String> entry : encodingsMap.entrySet()) {
                knownEncodings.put(String.valueOf(entry.getKey()), new JSONArray(entry.getValue()));
            }

            // 3. Call Python Service
            try (InputStream imageStream = filePart.getInputStream()) {
                String contentType = filePart.getContentType();
                JSONObject result = pythonAIService.recognizeClass(imageStream, filePart.getSubmittedFileName(),
                        contentType, knownEncodings.toString());

                if (result != null && "success".equals(result.optString("status"))) {
                    JSONArray presentStudentsJson = result.getJSONArray("present_students");
                    int markedCount = 0;

                    for (int i = 0; i < presentStudentsJson.length(); i++) {
                        int studentId = presentStudentsJson.getInt(i);

                        // Mark attendance
                        int teacherId = 0;
                        if (request.getSession().getAttribute("user") != null) {
                            teacherId = ((com.attendance.model.User) request.getSession().getAttribute("user")).getId();
                        }

                        boolean marked = attendanceDAO.markAttendance(studentId, "Present", teacherId);

                        if (marked) {
                            auditLogDAO.logAction(studentId, "ATTENDANCE_SUCCESS",
                                    "Marked via Classroom AI by Teacher ID: " + teacherId, "TEACHER", "SUCCESS");
                            markedCount++;
                        } else {
                            auditLogDAO.logAction(studentId, "ATTENDANCE_FAIL", "Database error marking attendance",
                                    "TEACHER", "FAILURE");
                        }
                    }

                    int totalDetected = presentStudentsJson.length();
                    // Ideally python returns unmatched faces count, for now we assume all returned
                    // are matched.
                    // To improve, Python could return 'total_faces_detected' and we compare.
                    // For this iteration, we log success count.
                    auditLogDAO.logAction(null, "CLASSROOM_ATTENDANCE_BATCH",
                            "Processed batch. Marked: " + markedCount + ", Total Detected: " + totalDetected,
                            "TEACHER", "SUCCESS");

                    // To make this real, I need to call the actual attendance marking.
                    // I will check AttendanceDAO in the next step and update this servlet if
                    // needed.
                    // For now I will assume a method addAttendance(Attendance attendance) exists or
                    // similar.

                    response.getWriter().write("{\"status\": \"success\", \"present_count\": " + markedCount + "}");
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "AI recognition failed");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
}
