<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.attendance.model.User" %>
<%
<%
    // Fix: Check for session string attributes
    String role = (String) session.getAttribute("role");
    
    if (role == null || (!"teacher".equalsIgnoreCase(role) && !"admin".equalsIgnoreCase(role))) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Classroom Attendance AI</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .container { max-width: 800px; margin-top: 50px; }
        #preview { max-width: 100%; margin-top: 10px; display: none; }
    </style>
</head>
<body>
    <jsp:include page="../navbar.jsp" />

    <div class="container">
<!-- ... (content omitted) ... -->
            $.ajax({
                url: '<%= request.getContextPath() %>/classroom-attendance',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(response) {
                    var res = JSON.parse(response);
                    if (res.status === "success") {
                        $('#message').html('<div class="alert alert-success">Successfully marked present: ' + res.present_count + ' students.</div>');
                    } else {
                        $('#message').html('<div class="alert alert-danger">Error: ' + (res.message || "Unknown error") + '</div>');
                    }
                },
                error: function(xhr) {
                    $('#message').html('<div class="alert alert-danger">Request failed: ' + xhr.statusText + '</div>');
                }
            });
        }
    </script>
</body>
</html>
