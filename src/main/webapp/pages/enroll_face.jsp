<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.attendance.model.User" %>
<%
    // Prevent caching
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    // Check for individual session attributes set by LoginServlet
    String role = (String) session.getAttribute("role");
    String email = (String) session.getAttribute("email");
    
    // Redirect to login if session is invalid or role is missing
    if (email == null || role == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Optional: Restrict access if needed (currently checking for Admin, Teacher, Student)
    if (!"admin".equalsIgnoreCase(role) && !"teacher".equalsIgnoreCase(role) && !"student".equalsIgnoreCase(role)) {
        response.sendRedirect("login.jsp"); 
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Enroll Student Face</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        .enroll-container { max-width: 600px; margin: 50px auto; padding: 20px; box-shadow: 0 0 10px rgba(0,0,0,0.1); border-radius: 8px; }
        #preview { max-width: 100%; margin-top: 10px; display: none; }
    </style>
</head>
<body>
    <jsp:include page="../navbar.jsp" />

    <div class="container">
        <div class="card enroll-container">
            <h3 class="text-center mb-4">Enroll Student Face</h3>
            <div id="message"></div>
            
            <form id="enrollForm" enctype="multipart/form-data">
                <div class="mb-3">
                    <label for="studentId" class="form-label">Student ID</label>
                    <input type="number" class="form-control" id="studentId" name="studentId" required placeholder="Enter Student ID">
                </div>

                <div class="mb-3">
                    <label for="className" class="form-label">Class Name (Optional Filter)</label>
                    <select class="form-select" id="className" name="className">
                        <option value="" selected>All Classes</option>
                        <option value="CSE-A">CSE-A</option>
                        <option value="CSE-B">CSE-B</option>
                        <option value="ECE-A">ECE-A</option>
                        <option value="ECE-B">ECE-B</option>
                        <option value="MECH-A">MECH-A</option>
                        <option value="MECH-B">MECH-B</option>
                    </select>
                </div>
                
                <div class="mb-3">
                    <label for="image" class="form-label">Upload Face Image</label>
                    <input type="file" class="form-control" id="image" name="image" accept="image/*" required>
                </div>
                
                <div class="text-center mb-3">
                    <img id="preview" src="#" alt="Face Preview" class="img-thumbnail" />
                </div>

                <button type="submit" class="btn btn-primary w-100">Enroll Face</button>
            </form>
        </div>
    </div>

    <script>
        $(document).ready(function() {
            // Image preview
            $("#image").change(function() {
                const file = this.files[0];
                if (file) {
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        $('#preview').attr('src', e.target.result).show();
                    }
                    reader.readAsDataURL(file);
                } else {
                    $('#preview').hide();
                }
            });

            // Form submission
            $("#enrollForm").submit(function(event) {
                event.preventDefault();
                
                var formData = new FormData(this);
                
                $('#message').html('<div class="alert alert-info">Processing...</div>');
                
                $.ajax({
                    url: '<%= request.getContextPath() %>/enroll-face',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function(response) {
                        var res;
                        try {
                            res = typeof response === "string" ? JSON.parse(response) : response;
                        } catch(e) {
                            res = { status: "error", message: "Invalid server response" };
                        }

                        if (res.status === "success") {
                            $('#message').html('<div class="alert alert-success">Enrollment Successful!</div>');
                            $('#enrollForm')[0].reset();
                            $('#preview').hide();
                        } else {
                            $('#message').html('<div class="alert alert-danger">Error: ' + res.message + '</div>');
                        }
                    },
                    error: function(xhr) {
                        var errorMsg = "Enrollment failed.";
                        
                        // Try to parse JSON from responseText if responseJSON is not populated
                        var resp = xhr.responseJSON;
                        if (!resp && xhr.responseText) {
                            try {
                                resp = JSON.parse(xhr.responseText);
                            } catch(e) {
                                // Not JSON
                            }
                        }

                        if (resp && resp.message) {
                            errorMsg = resp.message;
                        } else if (xhr.statusText && xhr.statusText !== "error") {
                            errorMsg += " Status: " + xhr.statusText;
                        } else {
                            errorMsg += " (Server error code: " + xhr.status + ")";
                        }
                        
                        $('#message').html('<div class="alert alert-danger">Error: ' + errorMsg + '</div>');
                    }
                });
            });
        });
    </script>
</body>
</html>
