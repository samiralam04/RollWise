package com.attendance.controller;

import com.attendance.model.Teacher;
import com.attendance.util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class AdminServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getPathInfo();

        if (action == null) {
            response.sendRedirect("/dashboard/admin");
            return;
        }

        switch (action) {
            case "/listTeachers":
                listTeachers(request, response);
                break;
            case "/deleteTeacher":
                deleteTeacher(request, response);
                break;
            case "/listHolidays":
                listHolidays(request, response);
                break;
            case "/announceHoliday":
                announceHoliday(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getPathInfo();

        if (action == null) {
            response.sendRedirect("/dashboard/admin");
            return;
        }

        switch (action) {
            case "/addTeacher":
                addTeacher(request, response);
                break;
            case "/announceEmergencyHoliday":
                announceEmergencyHoliday(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void listTeachers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Teacher> teachers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM teachers");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                teachers.add(new Teacher(rs.getInt("id"), rs.getString("name"), rs.getString("email")));
            }
            request.setAttribute("teachers", teachers);
            request.getRequestDispatcher("/pages/admin-teachers.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void addTeacher(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO teachers (name, email) VALUES (?, ?)")) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.executeUpdate();

            response.sendRedirect("/admin/listTeachers");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void deleteTeacher(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM teachers WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            response.sendRedirect("/admin/listTeachers");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void listHolidays(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<String> holidays = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM holidays");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                holidays.add(rs.getString("date") + " - " + rs.getString("reason"));
            }
            request.setAttribute("holidays", holidays);
            request.getRequestDispatcher("/pages/admin-holidays.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void announceHoliday(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String date = request.getParameter("date");
        String reason = request.getParameter("reason");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO holidays (date, reason) VALUES (?, ?)")) {

            stmt.setString(1, date);
            stmt.setString(2, reason);
            stmt.executeUpdate();

            response.sendRedirect("/admin/listHolidays");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void announceEmergencyHoliday(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String reason = request.getParameter("reason");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO holidays (date, reason) VALUES (CURRENT_DATE, ?)")) {

            stmt.setString(1, "Emergency: " + reason);
            stmt.executeUpdate();

            response.sendRedirect("/admin/listHolidays");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
