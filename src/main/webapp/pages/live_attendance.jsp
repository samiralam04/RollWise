<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Cache control
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    // Session validation
    String email = (String) session.getAttribute("email");
    String role  = (String) session.getAttribute("role");

    if (email == null || role == null || !"student".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Live Self Attendance</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

    <style>
        .camera-container {
            max-width: 600px;
            margin: 40px auto;
            text-align: center;
        }
        .video-wrapper {
            position: relative;
            border-radius: 12px;
            overflow: hidden;
            background: #000;
            box-shadow: 0 0 20px rgba(0,0,0,0.3);
        }
        video {
            width: 100%;
            transform: scaleX(-1);
        }
        .scan-line {
            position: absolute;
            width: 100%;
            height: 2px;
            background: rgba(0,255,0,0.7);
            animation: scan 2s linear infinite;
            display: none;
        }
        @keyframes scan {
            from { top: 0; }
            to { top: 100%; }
        }
    </style>
</head>

<body>
<jsp:include page="../navbar.jsp" />

<div class="container camera-container">
    <h3 class="mb-3">Mark Self Attendance</h3>

    <div id="message" class="mb-3"></div>

    <div class="video-wrapper mb-3">
        <video id="video" autoplay playsinline></video>
        <div id="scanLine" class="scan-line"></div>
    </div>

    <canvas id="canvas" width="640" height="480" style="display:none;"></canvas>

    <button id="startButton" class="btn btn-primary btn-lg">
        Start Attendance
    </button>

    <p class="text-muted mt-2">
        Look at the camera and blink naturally when scanning starts.
    </p>
</div>

<script>
    let stream = null;
    const video = document.getElementById("video");
    const canvas = document.getElementById("canvas");
    const startButton = document.getElementById("startButton");
    const scanLine = document.getElementById("scanLine");

    async function initCamera() {
        try {
            stream = await navigator.mediaDevices.getUserMedia({
                video: { facingMode: "user" }
            });
            video.srcObject = stream;
        } catch (err) {
            console.error(err);
            $('#message').html(
                '<div class="alert alert-danger">❌ Camera access denied</div>'
            );
            startButton.disabled = true;
        }
    }

    initCamera();

    startButton.addEventListener("click", () => {
        startButton.disabled = true;
        startButton.innerText = "Scanning...";
        scanLine.style.display = "block";

        setTimeout(captureAndSend, 2000);
    });

    function captureAndSend() {
        const ctx = canvas.getContext("2d");

        // Mirror fix
        ctx.save();
        ctx.scale(-1, 1);
        ctx.drawImage(video, -canvas.width, 0, canvas.width, canvas.height);
        ctx.restore();

        canvas.toBlob(blob => {
            const formData = new FormData();
            formData.append("image", blob, "live.jpg");

            $('#message').html(
                '<div class="alert alert-info">Verifying your presence...</div>'
            );

            $.ajax({
                url: '<%= request.getContextPath() %>/live-attendance',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,

                success: function (response) {
                    let res = (typeof response === "string")
                        ? JSON.parse(response)
                        : response;

                    if (res.status === "success") {
                        scanLine.style.display = "none";
                        $('#message').html(
                            '<div class="alert alert-success">✅ ' + res.message + '</div>'
                        );

                        if (stream) {
                            stream.getTracks().forEach(t => t.stop());
                        }

                        startButton.innerText = "Attendance Marked";
                    } else {
                        // Special Case: Face matched but no blink
                        if (res.message && res.message.includes("blink")) {
                            $('#message').html(
                                '<div class="alert alert-warning">Face matched! <b>Now BLINK your eyes...</b></div>'
                            );
                            // Auto-retry in 1 second
                            setTimeout(captureAndSend, 1000);
                        } else {
                            scanLine.style.display = "none";
                            $('#message').html(
                                '<div class="alert alert-danger">❌ ' +
                                (res.message || "Verification failed") +
                                '</div>'
                            );
                            startButton.disabled = false;
                            startButton.innerText = "Try Again";
                        }
                    }
                },

                error: function (xhr) {
                    let errorMsg = "Verification error";
                    let res = null;
                    if (xhr.responseText) {
                        try {
                            res = JSON.parse(xhr.responseText);
                            errorMsg = res.message || errorMsg;
                        } catch (e) {}
                    }

                    // Handle 400 error specifically for blink
                    if (res && res.message && res.message.includes("blink")) {
                        $('#message').html(
                            '<div class="alert alert-warning">Face matched! <b>Now BLINK your eyes...</b></div>'
                        );
                        // Auto-retry in 800ms
                        setTimeout(captureAndSend, 800);
                    } else {
                        scanLine.style.display = "none";
                        $('#message').html(
                            '<div class="alert alert-danger">❌ ' + errorMsg + '</div>'
                        );
                        startButton.disabled = false;
                        startButton.innerText = "Retry";
                    }
                }
            });
        }, "image/jpeg");
    }
</script>

</body>
</html>
