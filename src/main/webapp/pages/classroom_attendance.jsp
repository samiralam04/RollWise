<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.attendance.model.User" %>
<%
    // Fix: Check for session string attributes
    String role = (String) session.getAttribute("role");
    
    if (role == null || (!"teacher".equalsIgnoreCase(role) && !"admin".equalsIgnoreCase(role))) {
        response.sendRedirect("login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Classroom Attendance AI</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        .container { max-width: 800px; margin-top: 50px; }
        #preview { width: 100%; max-width: 640px; margin-top: 10px; border: 2px solid #ddd; border-radius: 8px; }
        #canvas { display: none; }
        .camera-container { position: relative; display: inline-block; }
        .overlay { position: absolute; top: 10px; left: 10px; color: white; background: rgba(0,0,0,0.5); padding: 5px; border-radius: 5px; }
    </style>
</head>
<body>
    <jsp:include page="../navbar.jsp" />

    <div class="container">
        <h2 class="mb-4 text-center">Classroom AI Attendance</h2>
        
        <div id="message"></div>
        
        <div class="card shadow">
            <div class="card-header bg-primary text-white">
                Take Classroom Attendance (Image Upload)
            </div>
            <div class="card-body text-center">
                
                <div class="mb-3 text-start">
                    <label for="className" class="form-label">Class Name</label>
                    <input type="text" class="form-control" id="className" list="classOptions" placeholder="Enter Class Name (e.g. CSE-A)" required>
                    <datalist id="classOptions">
                        <option value="CSE-A">
                        <option value="CSE-B">
                        <option value="ECE-A">
                        <option value="ECE-B">
                        <option value="MECH-A">
                        <option value="MECH-B">
                    </datalist>
                </div>

                <div class="mb-3 text-start">
                    <label for="classImage" class="form-label">Upload Class Image</label>
                    <input type="file" class="form-control" id="classImage" accept="image/*" required>
                </div>

                <div class="mb-3">
                    <img id="preview" src="#" alt="Class Image Preview" class="img-fluid rounded" style="max-height: 400px; display: none;">
                </div>
                
                <br>
                <button id="uploadBtn" class="btn btn-success btn-lg mt-3">
                    <i class="bi bi-cloud-upload-fill"></i> Upload & Mark Attendance
                </button>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script>
        $(document).ready(function() {
            // Image preview
            $("#classImage").change(function() {
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

            $('#uploadBtn').click(function() {
                var className = $('#className').val();
                var fileInput = document.getElementById('classImage');
                var file = fileInput.files[0];

                if (!className) {
                    Swal.fire('Error', 'Please enter a Class Name.', 'error');
                    return;
                }

                if (!file) {
                    Swal.fire('Error', 'Please select a class image.', 'error');
                    return;
                }

                var formData = new FormData();
                formData.append('image', file);
                formData.append('className', className);

                // Initial UI feedback
                var btn = $(this);
                btn.prop('disabled', true).text('Processing...');
                $('#message').html('<div class="alert alert-info">Processing image... Please wait.</div>');

                $.ajax({
                    url: '<%= request.getContextPath() %>/classroom-attendance',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function(response) {
                        btn.prop('disabled', false).text('Upload & Mark Attendance');
                        $('#message').html('');
                        
                        var res = (typeof response === 'string') ? JSON.parse(response) : response;
                        
                        if (res.status === "success") {
                            let presentStudents = res.present_students_list || [];
                            let count = res.present_count;
                            
                            if (presentStudents.length > 0) {
                                showSequentialPopups(presentStudents, 0);
                            } else {
                                Swal.fire('Attendance Marked', 'No students detected in the image.', 'info');
                            }
                            
                        } else {
                            Swal.fire('Error', res.message || "Unknown error", 'error');
                        }
                    },
                    error: function(xhr) {
                        btn.prop('disabled', false).text('Upload & Mark Attendance');
                        $('#message').html('');
                        Swal.fire('Request Failed', xhr.responseText || 'Server Error', 'error');
                    }
                });
            });
        });

        function showSequentialPopups(students, index) {
            if (index >= students.length) {
                Swal.fire('Completed', 'All specific students marked present. Others marked absent.', 'success');
                return;
            }

            let student = students[index];
            Swal.fire({
                title: 'Marked Present',
                text: 'ID: ' + student.id + ' - Name: ' + student.name,
                icon: 'success',
                confirmButtonText: 'Next',
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    showSequentialPopups(students, index + 1);
                }
            });
        }
    </script>
</body>
</html>
