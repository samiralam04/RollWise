package com.attendance.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Invalidate the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Redirect to login page
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}
