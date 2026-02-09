package com.attendance.controller;

import com.attendance.dao.AuditLogDAO;
import com.attendance.dao.StudentFaceDataDAO;
import com.attendance.service.PythonAIService;
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

@WebServlet("/enroll-face")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 20 // 20MB
)
public class FaceEnrollmentServlet extends HttpServlet {

    private PythonAIService pythonAIService;
    private StudentFaceDataDAO studentFaceDataDAO;
    private AuditLogDAO auditLogDAO;

    @Override
    public void init() throws ServletException {
        try {
            pythonAIService = new PythonAIService();
            studentFaceDataDAO = new StudentFaceDataDAO();
            auditLogDAO = new AuditLogDAO();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Failed to initialize DAOs or Services", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String studentIdStr = request.getParameter("studentId");
            if (studentIdStr == null || studentIdStr.trim().isEmpty()) {
                response.getWriter().write("{\"status\": \"error\", \"message\": \"Student ID is required\"}");
                return;
            }

            int studentId = Integer.parseInt(studentIdStr);
            Part filePart = request.getPart("image");

            if (filePart == null) {
                response.getWriter().write("{\"status\": \"error\", \"message\": \"No image part found in request\"}");
                return;
            }

            if (filePart.getSize() == 0) {
                response.getWriter().write("{\"status\": \"error\", \"message\": \"Uploaded image is empty\"}");
                return;
            }

            try (InputStream imageStream = filePart.getInputStream()) {
                String contentType = filePart.getContentType();
                JSONObject result = pythonAIService.enrollFace(imageStream, filePart.getSubmittedFileName(),
                        contentType);

                if (result != null && "success".equals(result.optString("status"))) {
                    String faceEncoding = result.getString("face_encoding");
                    String modelVersion = result.optString("model_version", "unknown");

                    boolean saved = studentFaceDataDAO.addFaceData(studentId, faceEncoding, modelVersion);

                    if (saved) {
                        auditLogDAO.logAction(studentId, "ENROLL_FACE",
                                "Face enrolled successfully. Model: " + modelVersion, "ADMIN", "SUCCESS");
                        response.getWriter()
                                .write("{\"status\": \"success\", \"message\": \"Face enrolled successfully\"}");
                    } else {
                        auditLogDAO.logAction(studentId, "ENROLL_FACE", "Database error saving face data", "ADMIN",
                                "FAILURE");
                        response.getWriter()
                                .write("{\"status\": \"error\", \"message\": \"Database error saving face data\"}");
                    }
                } else {
                    String message = result != null ? result.optString("message", "Unknown Python service error")
                            : "Fatal error communicating with Python service";
                    auditLogDAO.logAction(studentId, "ENROLL_FACE", "Python service error: " + message, "ADMIN",
                            "FAILURE");
                    response.getWriter().write(result != null ? result.toString()
                            : "{\"status\": \"error\", \"message\": \"" + message + "\"}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            // Try to get studentId for logging if possible, otherwise null
            Integer sId = null;
            try {
                sId = Integer.parseInt(request.getParameter("studentId"));
            } catch (Exception ignored) {
            }

            try {
                auditLogDAO.logAction(sId, "ENROLL_FACE", "Exception: " + e.getMessage(), "ADMIN", "FAILURE");
            } catch (Exception logEx) {
                logEx.printStackTrace();
            }

            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Server error: "
                    + errorMsg.replace("\"", "\\\"") + "\"}");
        }
    }
}
