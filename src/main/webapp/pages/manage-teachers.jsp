<%@ page import="java.util.List" %>
<%@ page import="com.attendance.model.Teacher" %>
<%@ page import="com.attendance.dao.TeacherDAO" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>

<%
    // Fetch teacher list
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
</head>
<body>
    <div class="container mt-5">
        <h2 class="text-center mb-4">Manage Teachers</h2>

        <!-- Add Teacher Form -->
        <div class="card p-4 shadow-sm">
            <h4 class="mb-3">Add Teacher</h4>
            <form action="<%= request.getContextPath() %>/teacher" method="post">
                <div class="row g-3">
                    <div class="col-md-6">
                        <input type="text" name="name" class="form-control" placeholder="Name" required>
                    </div>
                    <div class="col-md-6">
                        <input type="email" name="email" class="form-control" placeholder="Email" required>
                    </div>
                    <div class="col-md-6">
                        <input type="text" name="phone" class="form-control" placeholder="Phone" required>
                    </div>
                    <div class="col-md-6">
                        <input type="text" name="subject" class="form-control" placeholder="Subject" required>
                    </div>
                    <div class="col-md-6">
                        <input type="text" name="username" class="form-control" placeholder="Username" required>
                    </div>
                    <div class="col-md-6">
                        <input type="password" name="password" class="form-control" placeholder="Password" required>
                    </div>
                    <div class="col-12 text-center">
                        <button type="submit" class="btn btn-primary">Add Teacher</button>
                    </div>
                </div>
            </form>
        </div>

        <hr class="my-5">

        <!-- Teacher List -->
        <h3 class="text-center">Teacher List</h3>
        <div class="table-responsive">
            <table class="table table-bordered table-hover mt-3">
                <thead class="table-dark">
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Subject</th>
                        <th>Username</th>
                        <th>Actions</th>
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
                                <div class="d-flex gap-2">
                                    <!-- Edit Button -->
                                    <form action="<%= request.getContextPath() %>/teacher" method="get">
                                        <input type="hidden" name="action" value="edit">
                                        <input type="hidden" name="id" value="<%= teacher.getId() %>">
                                        <button type="submit" class="btn btn-warning btn-sm">Edit</button>
                                    </form>

                                    <!-- Delete Button (Trigger Modal) -->
                                    <button type="button" class="btn btn-danger btn-sm"
                                        data-bs-toggle="modal"
                                        data-bs-target="#deleteModal"
                                        onclick="setDeleteData('<%= teacher.getId() %>', '<%= teacher.getName() %>')">
                                        Delete
                                    </button>
                                </div>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div class="modal fade" id="deleteModal" tabindex="-1" aria-labelledby="deleteModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header bg-danger text-white">
                    <h5 class="modal-title" id="deleteModalLabel">Confirm Delete</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    Are you sure you want to delete <strong id="teacherName"></strong>?
                </div>
                <div class="modal-footer">
                    <form id="deleteForm" action="<%= request.getContextPath() %>/teacher" method="post">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" id="teacherId">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-danger">Delete</button>
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
