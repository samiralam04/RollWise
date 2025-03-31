<%@ page import="com.attendance.model.Teacher" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>

<%
    Teacher teacher = (Teacher) request.getAttribute("teacher");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Teacher</title>

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/edit-teacher.css">
</head>
<body class="edit-teacher-body">
    <div class="container edit-teacher-container">
        <div class="edit-teacher-card">
            <h2 class="edit-teacher-title">
                <i class="fas fa-user-edit me-2"></i>Edit Teacher
            </h2>
            <form action="<%= request.getContextPath() %>/teacher" method="post" class="edit-teacher-form">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="<%= teacher.getId() %>">

                <div class="mb-4 form-field">
                    <label class="form-label">Name</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="fas fa-user"></i></span>
                        <input type="text" name="name" class="form-control" value="<%= teacher.getName() %>" required>
                    </div>
                </div>

                <div class="mb-4 form-field">
                    <label class="form-label">Email</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="fas fa-envelope"></i></span>
                        <input type="email" name="email" class="form-control" value="<%= teacher.getEmail() %>" required>
                    </div>
                </div>

                <div class="mb-4 form-field">
                    <label class="form-label">Phone</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="fas fa-phone"></i></span>
                        <input type="text" name="phone" class="form-control" value="<%= teacher.getPhone() %>" required>
                    </div>
                </div>

                <div class="mb-4 form-field">
                    <label class="form-label">Subject</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="fas fa-book"></i></span>
                        <input type="text" name="subject" class="form-control" value="<%= teacher.getSubject() %>" required>
                    </div>
                </div>

                <div class="mb-4 form-field">
                    <label class="form-label">Username</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="fas fa-user-tag"></i></span>
                        <input type="text" name="username" class="form-control" value="<%= teacher.getUsername() %>" required>
                    </div>
                </div>

                <button type="submit" class="btn btn-update-teacher w-100">
                    <i class="fas fa-save me-2"></i>Update Teacher
                </button>
            </form>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>