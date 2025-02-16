package com.attendance.controller;

import com.attendance.util.DBConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class ReportServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp?error=Please login first.");
            return;
        }

        String role = (String) session.getAttribute("role");
        int userId = (int) session.getAttribute("userId");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try (Connection conn = DBConnection.getConnection()) {
            String query;
            PreparedStatement stmt;

            if ("student".equals(role)) {
                // Student can view only their attendance report
                query = "SELECT subject, total_classes, attended_classes, (attended_classes * 100.0 / total_classes) AS percentage FROM attendance WHERE student_id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
            } else if ("teacher".equals(role)) {
                // Teacher can view all students' attendance
                query = "SELECT s.name, a.subject, a.total_classes, a.attended_classes, (a.attended_classes * 100.0 / a.total_classes) AS percentage FROM attendance a JOIN users s ON a.student_id = s.id ORDER BY s.name";
                stmt = conn.prepareStatement(query);
            } else {
                // Admin can view all attendance records
                query = "SELECT s.name, s.email, a.subject, a.total_classes, a.attended_classes, (a.attended_classes * 100.0 / a.total_classes) AS percentage FROM attendance a JOIN users s ON a.student_id = s.id ORDER BY s.name";
                stmt = conn.prepareStatement(query);
            }

            ResultSet rs = stmt.executeQuery();

            out.println("<html><head><title>Attendance Report</title></head><body>");
            out.println("<h2>Attendance Report</h2>");
            out.println("<table border='1'><tr>");

            if ("student".equals(role)) {
                out.println("<th>Subject</th><th>Total Classes</th><th>Attended Classes</th><th>Percentage</th>");
            } else {
                out.println("<th>Student Name</th><th>Subject</th><th>Total Classes</th><th>Attended Classes</th><th>Percentage</th>");
            }

            out.println("</tr>");

            while (rs.next()) {
                out.println("<tr>");
                if (!"student".equals(role)) {
                    out.println("<td>" + rs.getString("name") + "</td>");
                }
                out.println("<td>" + rs.getString("subject") + "</td>");
                out.println("<td>" + rs.getInt("total_classes") + "</td>");
                out.println("<td>" + rs.getInt("attended_classes") + "</td>");
                out.println("<td>" + rs.getDouble("percentage") + "%</td>");
                out.println("</tr>");
            }

            out.println("</table>");
            out.println("<br><a href='dashboard.jsp'>Back to Dashboard</a>");
            out.println("</body></html>");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("dashboard.jsp?error=Database error occurred.");
        }
    }
}
