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

@WebServlet("/ReportServlet")
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
                // Student can view only their attendance
                query = "SELECT date, status FROM attendance WHERE student_id = ? ORDER BY date DESC";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
            } else if ("teacher".equals(role)) {
                // Teacher can view all students' attendance under them
                query = "SELECT s.name, a.date, a.status FROM attendance a " +
                        "JOIN users s ON a.student_id = s.id " +
                        "WHERE a.teacher_id = ? " +
                        "ORDER BY a.date DESC";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
            } else {
                // Admin can view all attendance records
                query = "SELECT s.name, s.email, a.date, a.status, t.name AS teacher_name FROM attendance a " +
                        "JOIN users s ON a.student_id = s.id " +
                        "JOIN users t ON a.teacher_id = t.id " +
                        "ORDER BY a.date DESC";
                stmt = conn.prepareStatement(query);
            }

            ResultSet rs = stmt.executeQuery();

            out.println("<html><head><title>Attendance Report</title></head><body>");
            out.println("<h2>Attendance Report</h2>");
            out.println("<table border='1'><tr>");

            if ("student".equals(role)) {
                out.println("<th>Date</th><th>Status</th>");
            } else if ("teacher".equals(role)) {
                out.println("<th>Student Name</th><th>Date</th><th>Status</th>");
            } else {
                out.println("<th>Student Name</th><th>Email</th><th>Date</th><th>Status</th><th>Teacher Name</th>");
            }

            out.println("</tr>");

            while (rs.next()) {
                out.println("<tr>");
                if (!"student".equals(role)) {
                    out.println("<td>" + rs.getString("name") + "</td>");
                    if ("admin".equals(role)) {
                        out.println("<td>" + rs.getString("email") + "</td>");
                    }
                }
                out.println("<td>" + rs.getString("date") + "</td>");
                out.println("<td>" + rs.getString("status") + "</td>");
                if ("admin".equals(role)) {
                    out.println("<td>" + rs.getString("teacher_name") + "</td>");
                }
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
