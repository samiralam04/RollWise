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
import java.util.ArrayList;

@WebServlet("/classroom-attendance")
@MultipartConfig
public class ClassroomAttendanceServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String className = request.getParameter("className");
        Part filePart = request.getPart("image");

        if (filePart == null || filePart.getSize() == 0 || className == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing image or class name");
            return;
        }

        try {
            // 1. Fetch ALL students in the selected class
            StudentDAO studentDAO = new StudentDAO();
            List<Student> allStudents = studentDAO.getAllStudents(); // Fetch all first

            // Filter by class name strictly
            List<Student> classStudents = new ArrayList<>();
            for (Student s : allStudents) {
                if (className.equals(s.getClassName())) {
                    classStudents.add(s);
                }
            }

            if (classStudents.isEmpty()) {
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"status\": \"error\", \"message\": \"No students found in class " + className + "\"}");
                return;
            }

            // 2. Prepare encodings for students who have them
            StudentFaceDataDAO faceDAO = new StudentFaceDataDAO();
            JSONObject knownEncodings = new JSONObject();
            boolean hasAnyFaceData = false;

            for (Student s : classStudents) {
                String encoding = faceDAO.getFaceEncoding(s.getId());
                if (encoding != null && !encoding.isEmpty()) {
                    try {
                        knownEncodings.put(String.valueOf(s.getId()), new JSONArray(encoding));
                        hasAnyFaceData = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!hasAnyFaceData) {
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"status\": \"error\", \"message\": \"No students in this class have face data registered.\"}");
                return;
            }

            // 3. Call Python Service
            PythonAIService pythonService = new PythonAIService();
            JSONObject result = null;

            try (InputStream imageStream = filePart.getInputStream()) {
                result = pythonService.recognizeClass(imageStream, filePart.getSubmittedFileName(),
                        filePart.getContentType(), knownEncodings.toString());
            }

            if (result != null && "success".equals(result.optString("status"))) {
                JSONArray presentStudentIds = result.getJSONArray("present_students");
                List<Integer> presentList = new ArrayList<>();
                for (int i = 0; i < presentStudentIds.length(); i++)
                    presentList.add(presentStudentIds.getInt(i));

                AttendanceDAO attendanceDAO = new AttendanceDAO();
                AuditLogDAO auditLogDAO = new AuditLogDAO();
                int markedCount = 0;
                JSONArray presentStudentsResponse = new JSONArray();

                // 4 & 5. Mark Attendance (Present/Absent)
                for (Student s : classStudents) {
                    boolean isPresent = presentList.contains(s.getId());
                    String status = isPresent ? "Present" : "Absent";

                    // Mark in DB
                    int teacherId = 1; // Default teacher ID
                    if (request.getSession().getAttribute("userId") != null) {
                        teacherId = (Integer) request.getSession().getAttribute("userId");
                    }

                    attendanceDAO.markAttendance(s.getUserId(), status, teacherId);

                    if (isPresent) {
                        markedCount++;
                        auditLogDAO.logAction(s.getId(), "ATTENDANCE_SUCCESS", "Marked Present via Classroom AI",
                                "TEACHER", "SUCCESS");

                        JSONObject studentObj = new JSONObject();
                        studentObj.put("id", s.getId());
                        studentObj.put("name", s.getName());
                        presentStudentsResponse.put(studentObj);
                    }
                }

                JSONObject responseJson = new JSONObject();
                responseJson.put("status", "success");
                responseJson.put("present_count", markedCount);
                responseJson.put("present_students_list", presentStudentsResponse);

                response.setContentType("application/json");
                response.getWriter().write(responseJson.toString());

            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"status\": \"error\", \"message\": \"AI Processing Failed: "
                        + (result != null ? result.optString("message") : "Unknown Error") + "\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
}
