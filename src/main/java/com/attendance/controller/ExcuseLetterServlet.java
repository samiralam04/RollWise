package com.attendance.controller;

import com.attendance.util.DBConnection;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/ExcuseLetterServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50) // 50MB
public class ExcuseLetterServlet extends HttpServlet {

    private static final String PYTHON_SERVICE_URL = "http://localhost:5000/process-excuse";
    private static final String UPLOAD_DIR = "uploads/excuses";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("upload".equals(action)) {
            handleUpload(request, response);
        } else if ("review".equals(action)) {
            handleReview(request, response);
        } else if ("list".equals(action)) {
            handleList(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int studentId = (int) session.getAttribute("userId");

        org.json.JSONArray excuses = getStudentExcuses(studentId);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\": true, \"excuses\": " + excuses.toString() + "}");
    }

    private org.json.JSONArray getStudentExcuses(int studentId) {
        org.json.JSONArray list = new org.json.JSONArray();
        String sql = "SELECT id, category, status, created_at, recommendation, absence_date, reason FROM excuse_requests WHERE student_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("id", rs.getInt("id"));
                obj.put("category", rs.getString("category") != null ? rs.getString("category") : "Unknown");
                obj.put("status", rs.getString("status") != null ? rs.getString("status") : "PENDING");

                java.sql.Timestamp ts = rs.getTimestamp("created_at");
                obj.put("created_at", ts != null ? ts.toString() : "");

                obj.put("recommendation", rs.getString("recommendation") != null ? rs.getString("recommendation") : "");

                java.sql.Date absDate = rs.getDate("absence_date");
                obj.put("absence_date", absDate != null ? absDate.toString() : "");

                obj.put("reason", rs.getString("reason") != null ? rs.getString("reason") : "");

                list.put(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void handleUpload(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\": false, \"message\": \"User not logged in\"}");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (role == null || !"Student".equalsIgnoreCase(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\": false, \"message\": \"Only students can upload excuses\"}");
            return;
        }

        int studentId = (int) session.getAttribute("userId");
        String absenceDate = request.getParameter("absence_date");
        String reason = request.getParameter("reason");

        Part filePart = request.getPart("document");
        if (filePart == null || filePart.getSize() == 0) {
            response.getWriter().write("{\"success\": false, \"message\": \"No file uploaded\"}");
            return;
        }

        // Save file locally (optional, but good for record)
        String applicationPath = request.getServletContext().getRealPath("");
        String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
        File uploadDir = new File(uploadFilePath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + "_" + getFileName(filePart);
        String filePath = uploadFilePath + File.separator + fileName;
        filePart.write(filePath);

        // Call Python Service
        JSONObject pythonResponse = sendToPythonService(filePath, filePart);

        if (pythonResponse != null && "success".equals(pythonResponse.optString("status"))) {
            // Save to DB
            boolean saved = saveExcuseRequest(studentId, fileName, pythonResponse, absenceDate, reason);
            if (saved) {
                response.setContentType("application/json");
                response.getWriter()
                        .write("{\"success\": true, \"message\": \"Excuse uploaded and processed successfully\"}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"Database error\"}");
            }
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"AI Processing failed: " +
                    (pythonResponse != null ? pythonResponse.optString("message") : "Unknown error") + "\"}");
        }
    }

    private JSONObject sendToPythonService(String filePath, Part filePart) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile = new HttpPost(PYTHON_SERVICE_URL);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            // We can send the file bytes directly
            builder.addBinaryBody("document", new File(filePath), ContentType.DEFAULT_BINARY, getFileName(filePart));
            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);

            try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                String jsonString = EntityUtils.toString(response.getEntity());
                return new JSONObject(jsonString);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean saveExcuseRequest(int studentId, String fileName, JSONObject data, String absenceDate,
            String reason) {
        String sql = "INSERT INTO excuse_requests (student_id, document_path, extracted_text, category, confidence, recommendation, status, absence_date, reason) VALUES (?, ?, ?, ?, ?, ?, 'PENDING', CAST(? AS DATE), ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, fileName);
            stmt.setString(3, data.optString("extracted_text"));
            stmt.setString(4, data.optString("category"));
            stmt.setString(5, data.optString("confidence"));
            stmt.setString(6, data.optString("recommendation"));
            stmt.setString(7, absenceDate);
            stmt.setString(8, reason);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleReview(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestId = request.getParameter("request_id");
        String status = request.getParameter("status"); // APPROVED, REJECTED, HOLD
        String teacherIdStr = request.getParameter("teacher_id");

        if (requestId == null || status == null) {
            response.getWriter().write("{\"success\": false, \"message\": \"Missing parameters\"}");
            return;
        }

        int teacherId = Integer.parseInt(teacherIdStr); // Get from session in real impl

        String sql = "UPDATE excuse_requests SET status = ?, teacher_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, teacherId);
            stmt.setInt(3, Integer.parseInt(requestId));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                response.getWriter().write("{\"success\": true, \"message\": \"Status updated to " + status + "\"}");
                // TODO: If APPROVED, potentially update attendance table
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"Failed to update status\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"Database error\"}");
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
}
