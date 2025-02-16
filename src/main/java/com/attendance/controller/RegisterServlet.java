package com.attendance.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.attendance.model.User;
import com.attendance.service.UserService;

 // Ensuring proper servlet mapping
 @WebServlet("/RegisterServlet")
 public class RegisterServlet extends HttpServlet {

     private static final long serialVersionUID = 1L;
     private UserService userService;

     @Override
     public void init() {
         userService = new UserService();
     }

     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // Retrieving form parameters
         String name = request.getParameter("name");
         String email = request.getParameter("email");
         String password = request.getParameter("password");
         String role = request.getParameter("role");

         // Setting response type
         response.setContentType("text/plain");
         response.setCharacterEncoding("UTF-8");

         // Input validation: Ensure all fields are provided
         if (name == null || email == null || password == null || role == null ||
                 name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
             response.getWriter().write("All fields are required.");
             return;
         }

         // Role validation
         if (!("Student".equalsIgnoreCase(role) || "Teacher".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role))) {
             response.getWriter().write("Invalid role selection.");
             return;
         }

         // Creating a User object
         User user = new User(name, email, password, role);
         boolean isRegistered = userService.registerUser(user);

         // Returning a plain text response
         if (isRegistered) {
             response.getWriter().write("success");  // Send "success" string instead of redirecting
         } else {
             response.getWriter().write("Registration failed. Email may already exist.");
         }
     }
 }
