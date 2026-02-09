# Student Attendance Management System

## Overview
An AI-powered web-based attendance management system featuring facial recognition for automated attendance tracking. The system supports three user roles: **Admin**, **Teacher**, and **Student**, each with role-specific dashboards and functionalities.

## Key Features

### 🎯 AI-Powered Face Recognition
- **Face Enrollment**: Register student faces through admin panel.
- **Live Self-Attendance**: Students mark attendance using webcam with blink detection for liveness verification.
- **Classroom Attendance**: Teachers upload classroom photos for automatic batch attendance marking with detection for unknown faces.
- **Attendance Protection**: 'Present' status is protected for the calendar day and cannot be accidentally overwritten as 'Absent'.
- **Mock Mode Support**: System can run without AI libraries for testing (returns dummy data).

### 👨‍💼 Admin Section
- User management (add/remove teachers and students).
- Face enrollment system for students.
- Declare holidays and send emergency alerts.
- View comprehensive attendance reports.
- Automated email notifications to parents.
- Audit logging for all system actions.

### 👩‍🏫 Teacher Section
- **Modernized Dashboard**: Clean, card-based UI with quick actions.
- Mark attendance manually or via Excel upload.
- Classroom photo attendance (AI-powered batch recognition) with feedback for unrecognized students.
- Automatic attendance percentage calculation with visual progress bars.
- Warning emails for students below 75% attendance.
- View student attendance analytics.

### 👨🎓 Student Section
- Live self-attendance with webcam (face + blink verification).
- View personal attendance records and statistics.
- Receive email notifications for low attendance.
- Access to holiday and alert announcements.

## Technology Stack

### Backend
- **Java 8** - Core application logic.
- **Servlet/JSP** - Web application framework.
- **PostgreSQL JDBC** - Direct database connectivity for performance.
- **Maven** - Dependency management and build tool.
- **Apache Tomcat 9** - Application server.

### Frontend
- **HTML5/CSS3** - Structure and styling.
- **JavaScript** - Client-side interactivity.
- **Bootstrap 5** - Responsive UI framework.
- **FontAwesome 6** - Vector icons.
- **SweetAlert2** - Modern popup notifications.
- **AJAX** - Asynchronous requests.

### AI/ML Services
- **Python 3.13** - AI service runtime.
- **Flask** - Python web framework.
- **OpenCV** - Image processing.
- **face_recognition** - Face detection and encoding (based on dlib).
- **NumPy** - Numerical computations.

### Database
- **PostgreSQL** - Primary database (`attendance_system`).
- Schema includes: users, students, teachers, attendance records, face data, audit logs.

### Security
- **BCrypt** password hashing.
- Session-based authentication.
- Role-based access control (RBAC).

## Architecture

```
┌─────────────────┐         ┌──────────────────┐
│   Web Browser   │ ◄─────► │  Tomcat Server   │
│   (Frontend)    │         │  (Java Backend)  │
└─────────────────┘         └────────┬─────────┘
                                     │
                                     │ HTTP
                                     ▼
                            ┌─────────────────┐
                            │  Python AI      │
                            │  Service :5000  │
                            └─────────────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │   PostgreSQL    │
                            │    Database     │
                            └─────────────────┘
```

## Installation and Setup

### Prerequisites
- Java 8 or higher
- Maven 3.6+
- PostgreSQL 12+
- Python 3.8+
- (Optional) cmake - Required for full AI functionality

### 1. Clone the Repository
```bash
git clone https://github.com/samiralam04/StudentAttendanceManagementSystem.git
cd StudentAttendanceManagementSystem
```

### 2. Database Setup
```bash
# Create PostgreSQL database
createdb attendance_system

# Configuration:
# Create a .env file in the root directory with:
DB_URL=jdbc:postgresql://localhost:5432/attendance_system
DB_USER=your_username
DB_PASSWORD=your_password
```

### 3. Python AI Service Setup
```bash
cd python_services

# Create virtual environment
python3 -m venv venv

# Activate virtual environment
source venv/bin/activate  # macOS/Linux
# OR
venv\Scripts\activate     # Windows

# Install dependencies
pip install -r requirements.txt

# For full AI functionality (optional):
brew install cmake  # macOS
# OR
sudo apt-get install cmake  # Ubuntu/Debian

# Then install face_recognition
pip install face_recognition
```

### 4. Start Python AI Service
```bash
# From python_services directory
./start_python_service.sh

# Verify with: curl http://localhost:5000/status
```

### 5. Build and Deploy Java Application
```bash
# From project root directory
mvn clean package cargo:run

# Application will start on http://localhost:8080/StudentAttendanceManagementSystem
```

## Running the Application

### Start Both Services
1. **Python AI Service** (Terminal 1):
   ```bash
   cd python_services
   ./start_python_service.sh
   ```

2. **Java Application** (Terminal 2):
   ```bash
   mvn cargo:run
   ```

## API Endpoints (Python Service)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/enroll-face` | POST | Enroll student face, returns 128-d encoding |
| `/verify-live-student` | POST | Verify identity + liveness (blink detection) |
| `/recognize-class` | POST | Batch face recognition from classroom photo |
| `/status` | GET | Check service and AI model status |
| `/health` | GET | Basic health check |

## Troubleshooting

### Python Service Issues
**Problem**: "Python service unreachable"
- Ensure Python service is running: `ps aux | grep app.py`
- Check port 5000 is available: `lsof -i :5000`
- Restart service: `pkill -f app.py && ./start_python_service.sh`

**Problem**: "AI libraries not available"
- This is expected if cmake/face_recognition isn't installed. System runs in mock mode (returns dummy data).

### Java Application Issues
**Problem**: "Port 8080 already in use"
- Kill existing process: `lsof -ti:8080 | xargs kill -9`
- Restart: `mvn cargo:run`

**Problem**: "Database connection failed"
- Verify PostgreSQL is running.
- Check credentials in `.env` file.

## Features in Detail

### Attendance Logic
The system implements strict attendance rules:
- **Daily Lock**: Once a student is marked "Present", they cannot be marked "Absent" for the rest of the day by automatic systems (prevents overwriting by classroom photos where they might be out of frame).
- **Manual Overrides**: Teachers can still upgrade an "Absent" status to "Present" if needed.
- **Reporting**: Automatic calculation of percentage with color-coded status indicators (Green > 75%, Yellow > 50%, Red < 50%).

### Classroom AI Recognition
The classroom attendance feature processes group photos. If any student is detected but not recognized (not matching any enrolled encoding), the system provides a warning message to the teacher: "One or more students were not recognized."

## License
This project is licensed under the MIT License.
© 2026 Samir Alam. All rights reserved.

## Contact
**Developer**: Samir Alam  
**Year**: 2026

Feel free to reach out for suggestions, collaborations, or support.
