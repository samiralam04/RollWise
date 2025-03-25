package com.attendance.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.attendance.model.User;
import com.attendance.service.UserService;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        if (name == null || email == null || password == null || role == null ||
                name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            response.getWriter().write("All fields are required.");
            return;
        }

        if (!("Student".equalsIgnoreCase(role) || "Teacher".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role))) {
            response.getWriter().write("Invalid role selection.");
            return;
        }

        // Encrypt the password before saving
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User(name, email, hashedPassword, role);
        boolean isRegistered = userService.registerUser(user);

        if (isRegistered) {
            response.getWriter().write("success");
        } else {
            response.getWriter().write("Registration failed. Email may already exist.");
        }
    }
}
