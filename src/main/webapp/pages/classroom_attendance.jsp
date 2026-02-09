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
                Take Classroom Attendance
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

                <div class="camera-container">
                    <video id="preview" autoplay playsinline></video>
                    <div class="overlay" id="statusOverlay">Camera Active</div>
                </div>
                <canvas id="canvas"></canvas>
                
                <br>
                <button id="captureBtn" class="btn btn-success btn-lg mt-3">
                    <i class="bi bi-camera-fill"></i> Capture & Mark Attendance
                </button>
            </div>
        </div>
    </div>

    <script>
        $(document).ready(function() {
            var video = document.getElementById('preview');
            var canvas = document.getElementById('canvas');
            var context = canvas.getContext('2d');
            var stream = null;

            // Request camera access
            if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
                navigator.mediaDevices.getUserMedia({ video: true }).then(function(s) {
                    stream = s;
                    video.srcObject = stream;
                    video.play();
                }).catch(function(err) {
                    console.log("An error occurred: " + err);
                    $('#message').html('<div class="alert alert-danger">Cannot access camera: ' + err.message + '</div>');
                    $('#statusOverlay').text('Camera Error');
                });
            } else {
                 $('#message').html('<div class="alert alert-danger">Camera API not supported in this browser.</div>');
            }

            $('#captureBtn').click(function() {
                var className = $('#className').val();
                if (!className) {
                    $('#message').html('<div class="alert alert-warning">Please enter a Class Name.</div>');
                    return;
                }

                if (!stream) {
                    $('#message').html('<div class="alert alert-warning">Camera not active.</div>');
                    return;
                }

                // Initial UI feedback
                var btn = $(this);
                btn.prop('disabled', true).text('Processing...');
                $('#message').html('');

                // Set canvas dimensions to match video
                canvas.width = video.videoWidth;
                canvas.height = video.videoHeight;
                
                // Draw video frame to canvas
                context.drawImage(video, 0, 0, canvas.width, canvas.height);
                
                // Convert to blob
                canvas.toBlob(function(blob) {
                    var formData = new FormData();
                    formData.append('image', blob, 'classroom_capture.jpg');
                    formData.append('className', className);

                    $.ajax({
                        url: '<%= request.getContextPath() %>/classroom-attendance',
                        type: 'POST',
                        data: formData,
                        processData: false,
                        contentType: false,
                        success: function(response) {
                            btn.prop('disabled', false).text('Capture & Mark Attendance');
                            
                            // Parse response if string
                            var res = (typeof response === 'string') ? JSON.parse(response) : response;
                            
                            if (res.status === "success") {
                                $('#message').html('<div class="alert alert-success">Successfully marked present: ' + res.present_count + ' students.</div>');
                            } else {
                                $('#message').html('<div class="alert alert-danger">Error: ' + (res.message || "Unknown error") + '</div>');
                            }
                        },
                        error: function(xhr) {
                            btn.prop('disabled', false).text('Capture & Mark Attendance');
                            $('#message').html('<div class="alert alert-danger">Request failed: ' + xhr.responseText + '</div>');
                        }
                    });
                }, 'image/jpeg', 0.9);
            });
        });
    </script>
</body>
</html>
