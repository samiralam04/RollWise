<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*, java.util.*" %>

<%
    String userType = (String) session.getAttribute("role");
    String userEmail = (String) session.getAttribute("email");

    if (userEmail == null || !"teacher".equals(userType)) {
        response.sendRedirect("login.jsp");
        return;
    }

    Connection conn = (Connection) application.getAttribute("DBConnection");

    List<Map<String, String>> attendanceRecords = new ArrayList<>();
    if (conn != null) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT student_id, date, status FROM attendance ORDER BY date DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, String> record = new HashMap<>();
                record.put("student_id", rs.getString("student_id"));
                record.put("date", rs.getString("date"));
                record.put("status", rs.getString("status"));
                attendanceRecords.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Teacher Dashboard</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="/StudentAttendanceManagementSystem/assets/css/teacher.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <div class="loading-overlay" id="loadingSpinner">
        <div class="loading-content">
            <div class="loading-spinner"></div>
            <div class="loading-progress"></div>
            <div class="loading-message" id="loadingMessage">Processing</div>
            <div class="loading-submessage" id="loadingSubmessage">Please wait while we process your request</div>
            <div class="loading-dots">
                <div class="loading-dot"></div>
                <div class="loading-dot"></div>
                <div class="loading-dot"></div>
            </div>
        </div>
    </div>

    <div class="container mt-4">
        <h2 class="text-center">Teacher Dashboard</h2>

        <div class="row">
            <!-- Left Column: Upload Attendance -->
            <div class="col-md-6">
                <div class="card p-3 h-100">
                    <h4><i class="fas fa-file-upload"></i> Upload Attendance</h4>
                    <p class="text-muted mb-3">Upload an Excel file with attendance data</p>
                    <form id="uploadForm" action="<%= request.getContextPath() %>/UploadExcelServlet" method="POST" enctype="multipart/form-data">
                        <div class="mb-3">
                            <input type="file" name="file" class="form-control" accept=".xlsx,.xls" required>
                            <small class="form-text text-muted">Supported formats: .xlsx, .xls</small>
                        </div>
                        <button type="submit" class="btn btn-success w-100">
                            <i class="fas fa-upload"></i> Upload & Process
                        </button>
                    </form>
                </div>
            </div>

            <!-- Right Column: AI Classroom Attendance -->
            <div class="col-md-6">
                <div class="card p-3 h-100">
                    <h4><i class="fas fa-robot"></i> AI Classroom Attendance</h4>
                    <p class="text-muted mb-3">Use facial recognition to mark attendance for the whole class instantly.</p>
                    <div class="mt-3">
                        <div class="d-flex align-items-center mb-2 text-muted">
                            <i class="fas fa-camera me-2"></i>
                            <span>Real-time face detection</span>
                        </div>
                        <div class="d-flex align-items-center mb-2 text-muted">
                            <i class="fas fa-bolt me-2"></i>
                            <span>Instant processing</span>
                        </div>
                        <div class="d-flex align-items-center mb-3 text-muted">
                            <i class="fas fa-users me-2"></i>
                            <span>Whole class at once</span>
                        </div>
                    </div>
                    <a href="classroom_attendance.jsp" class="btn w-100 mt-2" style="background-color: #5e72e4; color: white;">
                        <i class="fas fa-play-circle"></i> Launch AI Attendance
                    </a>
                </div>
            </div>
        </div>

        <!-- Second Row: Manual Attendance -->
        <div class="row mt-4">
            <div class="col-md-12">
                <div class="card p-3">
                    <h4><i class="fas fa-edit"></i> Mark Attendance Manually</h4>
                    <form id="manualAttendanceForm">
                        <div class="row">
                            <div class="col-md-3 mb-2">
                                <label class="form-label">Student ID (User ID)</label>
                                <input type="number" name="student_id" class="form-control" required placeholder="Enter Student ID">
                            </div>
                            <div class="col-md-3 mb-2">
                                <label class="form-label">Teacher ID</label>
                                <input type="number" name="teacher_id" class="form-control" value="<%= session.getAttribute("userId") %>" readonly>
                            </div>
                            <div class="col-md-3 mb-2">
                                <label class="form-label">Date</label>
                                <input type="date" name="date" class="form-control" value="<%= new java.sql.Date(System.currentTimeMillis()) %>" required>
                            </div>
                            <div class="col-md-3 mb-2">
                                <label class="form-label">Status</label>
                                <select name="status" class="form-select" required>
                                    <option value="Present">Present</option>
                                    <option value="Absent">Absent</option>
                                </select>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <button type="submit" class="btn btn-primary w-100">
                                    <i class="fas fa-save"></i> Save Attendance Record
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- Pending Excuses Review -->
        <div class="row mt-4">
            <div class="col-md-12">
                <div class="card p-3">
                    <h4><i class="fas fa-envelope-open-text"></i> Pending Excuse Letters</h4>
                    <div class="table-responsive">
                        <table class="table table-bordered table-hover">
                            <thead class="table-dark">
                                <tr>
                                    <th>ID</th>
                                    <th>Student ID</th>
                                    <th>Category</th>
                                    <th>Confidence</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    if (conn != null) {
                                        try (PreparedStatement psExcuse = conn.prepareStatement(
                                                "SELECT id, student_id, category, confidence, recommendation, extracted_text, document_path, absence_date, reason, status FROM excuse_requests WHERE status IN ('PENDING', 'HOLD') ORDER BY created_at ASC")) {
                                            ResultSet rsExcuse = psExcuse.executeQuery();
                                            while (rsExcuse.next()) {
                                                int reqId = rsExcuse.getInt("id");
                                                int studId = rsExcuse.getInt("student_id");
                                                String cat = rsExcuse.getString("category");
                                                String conf = rsExcuse.getString("confidence");
                                                String rec = rsExcuse.getString("recommendation");
                                                String text = rsExcuse.getString("extracted_text");
                                                if (text == null) text = "";
                                                String docPath = rsExcuse.getString("document_path");
                                                java.sql.Date absDate = rsExcuse.getDate("absence_date");
                                                String reason = rsExcuse.getString("reason");
                                %>
                                <tr>
                                    <td><%= reqId %></td>
                                    <td><%= studId %></td>
                                    <td><%= cat %></td>
                                    <td>
                                        <span class="badge <%= "High".equals(conf) ? "bg-success" : "Medium".equals(conf) ? "bg-warning" : "bg-danger" %>">
                                            <%= conf %>
                                        </span>
                                    </td>
                                    <td>
                                        <% String status = rsExcuse.getString("status"); %>
                                        <span class="badge <%= "HOLD".equals(status) ? "bg-warning text-dark" : "bg-secondary" %>">
                                            <%= status %>
                                        </span>
                                    </td>
                                    <td>
                                        <button type="button" class="btn btn-sm btn-primary" onclick="viewExcuse('<%= reqId %>', '<%= cat %>', '<%= rec %>', `<%= text.replace("`", "").replace("'", "\\'").replace("\n", "\\n") %>`, '<%= docPath %>', '<%= absDate != null ? absDate : "" %>', '<%= reason != null ? reason.replace("'", "\\'") : "" %>')">
                                            Review
                                        </button>
                                    </td>
                                </tr>
                                <%
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- Excuse Modal -->
        <div class="modal fade" id="excuseModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Review Excuse Letter</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-md-6">
                                <h6>Student Request</h6>
                                <p><strong>Absence Date:</strong> <span id="excuseDate"></span></p>
                                <p><strong>Reason:</strong> <span id="excuseReason"></span></p>
                                <hr>
                                <h6>Extracted Text</h6>
                                <p id="excuseText" style="white-space: pre-wrap; background: #f8f9fa; padding: 10px; border-radius: 5px; max-height: 200px; overflow-y: auto;"></p>
                            </div>
                            <div class="col-md-6">
                                <h6>AI Analysis</h6>
                                <p><strong>Category:</strong> <span id="excuseCat"></span></p>
                                <p><strong>Recommendation:</strong> <span id="excuseRec" class="badge bg-info"></span></p>
                                <hr>
                                <a id="excuseDocLink" href="#" target="_blank" class="btn btn-outline-secondary btn-sm mb-3">View Original Document</a>
                                <div>
                                    <button class="btn btn-success w-100 mb-2" onclick="processExcuse('APPROVED')">Approve</button>
                                    <button class="btn btn-danger w-100 mb-2" onclick="processExcuse('REJECTED')">Reject</button>
                                    <button class="btn btn-warning w-100" onclick="processExcuse('HOLD')">Hold</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Add Bootstrap Bundle JS (Required for Modal) -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

        <script>
            let currentExcuseId = null;
            let excuseModalVar = null;

            document.addEventListener('DOMContentLoaded', function() {
                 excuseModalVar = new bootstrap.Modal(document.getElementById('excuseModal'));
            });

            function viewExcuse(id, cat, rec, text, docPath, date, reason) {
                currentExcuseId = id;
                document.getElementById('excuseText').textContent = text;
                document.getElementById('excuseCat').textContent = cat;
                document.getElementById('excuseRec').textContent = rec;
                document.getElementById('excuseDate').textContent = date || 'N/A';
                document.getElementById('excuseReason').textContent = reason || 'N/A';
                document.getElementById('excuseDocLink').href = '${pageContext.request.contextPath}/uploads/excuses/' + docPath;
                if(excuseModalVar) {
                    excuseModalVar.show();
                } else {
                    console.error("Modal not initialized");
                }
            }

            function processExcuse(status) {
                if (!currentExcuseId) return;
                
                showLoading("Processing", status + " excuse request...");
                
                // Using URLSearchParams for form-urlencoded body
                const params = new URLSearchParams();
                params.append('action', 'review');
                params.append('request_id', currentExcuseId);
                params.append('status', status);
                params.append('teacher_id', '<%= session.getAttribute("userId") %>');

                fetch('${pageContext.request.contextPath}/ExcuseLetterServlet', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: params
                })
                .then(response => response.json())
                .then(data => {
                    hideLoading();
                    excuseModalVar.hide();
                    if (data.success) {
                        Swal.fire("Success", data.message, "success").then(() => location.reload());
                    } else {
                        Swal.fire("Error", data.message, "error");
                    }
                })
                .catch(err => {
                    hideLoading();
                    console.error(err);
                    Swal.fire("Error", "Request failed", "error");
                });
            }
        </script>

        <!-- Attendance Risk Radar -->
        <div class="card p-3 mt-4">
            <h4><i class="fas fa-satellite-dish"></i> Predictive Attendance Risk Radar</h4>
            <div class="row">
                <div class="col-md-8">
                    <div class="table-responsive">
                        <table class="table table-bordered table-hover">
                            <thead class="table-dark">
                                <tr>
                                    <th>Student</th>
                                    <th>Attendance %</th>
                                    <th>Trend</th>
                                    <th>Risk Level</th>
                                    <th>Projected %</th>
                                    <th>Suggested Action</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    com.attendance.service.AttendanceRiskService riskService = new com.attendance.service.AttendanceRiskService();
                                    java.util.List<com.attendance.model.StudentRiskDTO> riskReport = riskService.analyzeRisk();
                                    
                                    // Sort by risk score (descending)
                                    riskReport.sort((a, b) -> Integer.compare(b.getRiskScore(), a.getRiskScore()));

                                    for (com.attendance.model.StudentRiskDTO dto : riskReport) {
                                        String riskClass = "bg-success text-white";
                                        if ("AT-RISK".equals(dto.getRiskLevel())) riskClass = "bg-danger text-white";
                                        else if ("WATCH".equals(dto.getRiskLevel())) riskClass = "bg-warning text-dark";
                                        
                                        String trendIcon = "fa-arrow-right";
                                        String trendColor = "text-muted";
                                        if ("UP".equals(dto.getTrend())) { trendIcon = "fa-arrow-up"; trendColor = "text-success"; }
                                        else if ("DOWN".equals(dto.getTrend())) { trendIcon = "fa-arrow-down"; trendColor = "text-danger"; }
                                %>
                                <tr>
                                    <td><%= dto.getName() %></td>
                                    <td><%= String.format("%.1f", dto.getCurrentAttendance()) %>%</td>
                                    <td class="text-center"><i class="fas <%= trendIcon %> <%= trendColor %>"></i> <%= dto.getTrend() %></td>
                                    <td><span class="badge <%= riskClass %>"><%= dto.getRiskLevel() %></span></td>
                                    <td><%= String.format("%.1f", dto.getPredictedFinalAttendance()) %>%</td>
                                    <td><small><%= dto.getSuggestedAction() %></small></td>
                                    <td>
                                        <button class="btn btn-sm btn-info" onclick='showRiskChart(<%= new com.google.gson.Gson().toJson(dto.getWeeklyPresence()) %>, "<%= dto.getName() %>")'>
                                            <i class="fas fa-chart-line"></i> View
                                        </button>
                                    </td>
                                </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="col-md-4">
                     <div class="card h-100">
                        <div class="card-header">
                            <h5 class="mb-0">Attendance Trend (Last 4 Weeks)</h5>
                        </div>
                        <div class="card-body">
                            <canvas id="riskChart"></canvas>
                            <p id="chartPlaceholder" class="text-center text-muted mt-5">Select a student to view trend.</p>
                        </div>
                     </div>
                </div>
            </div>
        </div>



        <!-- Attendance Report -->
        <div class="card p-3 mt-4">
            <h4><i class="fas fa-chart-bar"></i> Student Attendance Report</h4>
            <div class="table-responsive">
                <table class="table table-bordered table-striped table-hover">
                    <thead class="table-dark">
                        <tr>
                            <th>Student ID</th>
                            <th>Teacher ID</th>
                            <th>Date</th>
                            <th>Parent Email</th>
                            <th>Total Classes</th>
                            <th>Attended</th>
                            <th>Attendance %</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            if (conn != null) {
                                try (PreparedStatement ps = conn.prepareStatement(
                                        "SELECT a.student_id, u.username AS student_name, a.teacher_id, a.date, p.parent_email, " +
                                        "COUNT(a.student_id) OVER (PARTITION BY a.student_id) AS total_classes, " +
                                        "SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) OVER (PARTITION BY a.student_id) AS attended_classes " +
                                        "FROM attendance a " +
                                        "JOIN users u ON a.student_id = u.id " +
                                        "LEFT JOIN parents p ON a.parent_email_id = p.id " +
                                        "ORDER BY a.date DESC"
                                );
                                     ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        String studentId = rs.getString("student_id");
                                        String teacherId = rs.getString("teacher_id");
                                        String date = rs.getString("date");
                                        String parentEmail = rs.getString("parent_email");
                                        int totalClasses = rs.getInt("total_classes");
                                        int attendedClasses = rs.getInt("attended_classes");
                                        double percentage = (totalClasses > 0) ? (attendedClasses * 100.0 / totalClasses) : 0;
                        %>
                                <tr>
                                    <td><strong><%= studentId %></strong> (<%= rs.getString("student_name") %>)</td>
                                    <td><%= teacherId %></td>
                                    <td><%= date %></td>
                                    <td><%= (parentEmail != null && !parentEmail.isEmpty()) ? parentEmail : "N/A" %></td>
                                    <td><span class="badge bg-info"><%= totalClasses %></span></td>
                                    <td><span class="badge bg-success"><%= attendedClasses %></span></td>
                                    <td>
                                        <div class="progress" style="height: 20px;">
                                            <div class="progress-bar <%= percentage >= 75 ? "bg-success" : percentage >= 50 ? "bg-warning" : "bg-danger" %>" 
                                                 role="progressbar" 
                                                 style="width: <%= percentage %>%"
                                                 aria-valuenow="<%= percentage %>" 
                                                 aria-valuemin="0" 
                                                 aria-valuemax="100">
                                                <%= String.format("%.1f", percentage) %>%
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                        <%
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    
    <!-- Logout Button -->
    <div class="logout-container">
        <a href="logout.jsp" class="btn btn-danger">
            <i class="fas fa-sign-out-alt"></i> Logout
        </a>
    </div>

    <script>
        let riskChartInstance = null;

        function showRiskChart(dataPoints, studentName) {
            const ctx = document.getElementById('riskChart').getContext('2d');
            document.getElementById('chartPlaceholder').style.display = 'none';

            if (riskChartInstance) {
                riskChartInstance.destroy();
            }

            riskChartInstance = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: ['4 Weeks Ago', '3 Weeks Ago', '2 Weeks Ago', 'Last Week'],
                    datasets: [{
                        label: 'Days Present',
                        data: dataPoints,
                        borderColor: '#5e72e4',
                        backgroundColor: 'rgba(94, 114, 228, 0.1)',
                        tension: 0.4,
                        fill: true,
                        pointRadius: 5,
                        pointHoverRadius: 7
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        title: {
                            display: true,
                            text: 'Attendance Trend for ' + studentName
                        },
                        legend: {
                            display: false
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            suggestedMax: 7, // Assuming 5-7 days a week
                            ticks: {
                                stepSize: 1
                            }
                        }
                    }
                }
            });
        }

        function showLoading(message, submessage) {
            const spinner = document.getElementById("loadingSpinner");
            const loadingMessage = document.getElementById("loadingMessage");
            const loadingSubmessage = document.getElementById("loadingSubmessage");

            if (message) loadingMessage.textContent = message;
            if (submessage) loadingSubmessage.textContent = submessage;

            spinner.style.display = "flex";
            void spinner.offsetWidth; // Force reflow to restart animations
        }

        function hideLoading() {
            const spinner = document.getElementById("loadingSpinner");
            spinner.style.opacity = "0";
            setTimeout(() => {
                spinner.style.display = "none";
                spinner.style.opacity = "1";
            }, 400);
        }

        // File upload form handler
        document.getElementById("uploadForm").addEventListener("submit", function(event) {
            event.preventDefault();
            showLoading("Uploading Attendance", "Processing Excel file...");

            const formData = new FormData(this);
            fetch(this.action, {
                method: "POST",
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                hideLoading();
                if (data.success) {
                    Swal.fire({
                        icon: "success",
                        title: "Success",
                        text: data.message,
                        confirmButtonColor: "#3085d6",
                    }).then(() => {
                        location.reload();
                    });
                } else {
                    Swal.fire({
                        icon: "error",
                        title: "Error",
                        text: data.message || "Failed to upload attendance",
                        confirmButtonColor: "#d33",
                    });
                }
            })
            .catch(error => {
                hideLoading();
                Swal.fire({
                    icon: "error",
                    title: "Error",
                    text: "Something went wrong during the upload process",
                    confirmButtonColor: "#d33",
                });
            });
        });

        // Manual attendance form handler
        document.getElementById("manualAttendanceForm").addEventListener("submit", function(event) {
            event.preventDefault();
            showLoading("Recording Attendance", "Saving attendance data...");

            const formData = new URLSearchParams();
            formData.append('student_id', this.elements['student_id'].value);
            formData.append('teacher_id', this.elements['teacher_id'].value);
            formData.append('date', this.elements['date'].value);
            formData.append('status', this.elements['status'].value);

            fetch('<%= request.getContextPath() %>/attendance', {
                method: "POST",
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                hideLoading();
                if (data.success) {
                    Swal.fire({
                        icon: "success",
                        title: "Success",
                        text: data.message,
                        confirmButtonColor: "#3085d6",
                    }).then(() => {
                        location.reload();
                        this.reset();
                    });
                } else {
                    Swal.fire({
                        icon: "error",
                        title: "Error",
                        text: data.message || "Failed to mark attendance",
                        confirmButtonColor: "#d33",
                    });
                }
            })
            .catch(error => {
                hideLoading();
                Swal.fire({
                    icon: "error",
                    title: "Error",
                    text: "Something went wrong: " + error.message,
                    confirmButtonColor: "#d33",
                });
                console.error("Error:", error);
            });
        });

        // Add visual feedback for AI Attendance button
        document.querySelector('a[href="classroom_attendance.jsp"]').addEventListener('mouseover', function() {
            this.style.transform = 'translateY(-2px)';
            this.style.boxShadow = '0 4px 15px rgba(0,0,0,0.2)';
        });

        document.querySelector('a[href="classroom_attendance.jsp"]').addEventListener('mouseout', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = 'none';
        });
    </script>
</body>
</html>