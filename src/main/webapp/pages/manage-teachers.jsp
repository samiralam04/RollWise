<%@ page import="java.util.List" %>
<%@ page import="com.attendance.model.Teacher" %>
<%@ page import="com.attendance.dao.TeacherDAO" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>

<%
    TeacherDAO teacherDAO = new TeacherDAO();
    List<Teacher> teacherList = teacherDAO.getAllTeachers();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Teachers</title>

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/teacher-management.css">
</head>
<body class="teacher-management-body">
    <div class="container teacher-management-container py-5">
        <div class="teacher-management-header text-center mb-5">
            <h1 class="display-5 fw-bold">
                <i class="fas fa-chalkboard-teacher me-2"></i>Teacher Management
            </h1>
            <p class="lead text-muted">Manage all teacher records in one place</p>
        </div>

        <!-- Add Teacher Card -->
        <div class="card add-teacher-card mb-5">
            <div class="card-header bg-gradient-primary text-white">
                <h4 class="mb-0">
                    <i class="fas fa-user-plus me-2"></i>Add New Teacher
                </h4>
            </div>
            <div class="card-body">
                <form action="<%= request.getContextPath() %>/teacher" method="post">
                    <div class="row g-4">
                        <div class="col-md-6">
                            <div class="form-floating">
                                <input type="text" name="name" class="form-control" id="nameInput" placeholder="Name" required>
                                <label for="nameInput">Name</label>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-floating">
                                <input type="email" name="email" class="form-control" id="emailInput" placeholder="Email" required>
                                <label for="emailInput">Email</label>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-floating">
                                <input type="text" name="phone" class="form-control" id="phoneInput" placeholder="Phone" required>
                                <label for="phoneInput">Phone</label>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-floating">
                                <input type="text" name="subject" class="form-control" id="subjectInput" placeholder="Subject" required>
                                <label for="subjectInput">Subject</label>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-floating">
                                <input type="text" name="username" class="form-control" id="usernameInput" placeholder="Username" required>
                                <label for="usernameInput">Username</label>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-floating">
                                <input type="password" name="password" class="form-control" id="passwordInput" placeholder="Password" required>
                                <label for="passwordInput">Password</label>
                            </div>
                        </div>
                        <div class="col-12 text-center mt-3">
                            <button type="submit" class="btn btn-add-teacher">
                                <i class="fas fa-plus-circle me-2"></i>Add Teacher
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <!-- Teacher List Section -->
        <div class="teacher-list-section">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h3 class="teacher-list-title">
                    <i class="fas fa-list-ul me-2"></i>Teacher Records
                </h3>
                <div class="total-teachers-badge">
                    <span class="badge rounded-pill bg-primary">
                        <%= teacherList.size() %> Teachers
                    </span>
                </div>
            </div>

            <div class="card teacher-list-card">
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-teachers mb-0">
                            <thead>
                                <tr>
                                    <th><i class="fas fa-id-card me-1"></i>ID</th>
                                    <th><i class="fas fa-user me-1"></i>Name</th>
                                    <th><i class="fas fa-envelope me-1"></i>Email</th>
                                    <th><i class="fas fa-phone me-1"></i>Phone</th>
                                    <th><i class="fas fa-book me-1"></i>Subject</th>
                                    <th><i class="fas fa-user-tag me-1"></i>Username</th>
                                    <th><i class="fas fa-cog me-1"></i>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (Teacher teacher : teacherList) { %>
                                    <tr>
                                        <td><%= teacher.getId() %></td>
                                        <td><%= teacher.getName() %></td>
                                        <td><%= teacher.getEmail() %></td>
                                        <td><%= teacher.getPhone() %></td>
                                        <td><%= teacher.getSubject() %></td>
                                        <td><%= teacher.getUsername() %></td>
                                        <td>
                                            <div class="action-buttons">
                                                <!-- Edit Button -->
                                                <form action="<%= request.getContextPath() %>/teacher" method="get" class="d-inline">
                                                    <input type="hidden" name="action" value="edit">
                                                    <input type="hidden" name="id" value="<%= teacher.getId() %>">
                                                    <button type="submit" class="btn btn-action btn-edit">
                                                        <i class="fas fa-edit"></i>
                                                    </button>
                                                </form>

                                                <!-- Delete Button -->
                                                <button type="button" class="btn btn-action btn-delete"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#deleteModal"
                                                    onclick="setDeleteData('<%= teacher.getId() %>', '<%= teacher.getName() %>')">
                                                    <i class="fas fa-trash-alt"></i>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div class="modal fade" id="deleteModal" tabindex="-1" aria-labelledby="deleteModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header bg-gradient-danger text-white">
                    <h5 class="modal-title" id="deleteModalLabel">
                        <i class="fas fa-exclamation-triangle me-2"></i>Confirm Deletion
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <p>You are about to permanently delete:</p>
                    <h5 class="text-center my-3"><strong id="teacherName" class="text-danger"></strong></h5>
                    <p class="text-muted text-center">This action cannot be undone.</p>
                </div>
                <div class="modal-footer">
                    <form id="deleteForm" action="<%= request.getContextPath() %>/teacher" method="post">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" id="teacherId">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                            <i class="fas fa-times me-1"></i>Cancel
                        </button>
                        <button type="submit" class="btn btn-danger">
                            <i class="fas fa-trash-alt me-1"></i>Delete Permanently
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        function setDeleteData(id, name) {
            document.getElementById("teacherId").value = id;
            document.getElementById("teacherName").textContent = name;
        }
    </script>
</body>
</html>