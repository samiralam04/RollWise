# Student Attendance Management System

## Overview
The Student Attendance Management System is a web-based application designed to streamline attendance tracking and management in educational institutions. It features three user roles: **Admin**, **Teacher**, and **Student/Parent**. Each role has unique access permissions and functionalities to enhance efficiency and transparency.

## Features

### Admin Section
![Admin Dashboard](Admin.png)

- Login and Register as Admin.
- Add or remove teachers.
- Declare holidays and send emergency alerts.
- View student attendance reports.
- Automatically send holiday and alert notifications to parents' emails.

### Teacher Section
![Teacher Dashboard](Teacher.png)

- Login and Register as Teacher.
- Upload attendance via Excel file or mark attendance manually.
- Automatically calculate attendance percentage after uploading Excel file.
- Automatically send warning emails to parents if attendance falls below 75%.

### Student/Parent Section
![Student Dashboard](student.png)

- Login and Register as Student or Parent.
- View attendance records.
- Receive email notifications for holidays, alerts, and low attendance warnings.

## Technologies Used
- **Backend:** Java, Servlet, JSP
- **Server:** Tomcat 9
- **Frontend:** HTML, CSS, Bootstrap, AJAX
- **Database:** PostgreSQL
- **Version Control:** GitHub
- **Security:** Password Hashing, Session Management

## Installation and Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/student-attendance-management.git
   ```
2. Import the project into your IDE (e.g., Eclipse or IntelliJ).
3. Set up PostgreSQL and configure the database connection in `web.xml`.
4. Deploy the project on Tomcat 9 server.
5. Access the application at:
   ```
   http://localhost:8080/StudentAttendanceManagementSystem
   ```

## Usage Instructions
- Admin can add teachers, declare holidays, and view attendance reports.
- Teachers can upload attendance or mark it manually.
- Students and parents can view attendance records and receive alerts.

## Contributing
Contributions are welcome! Feel free to open issues or submit pull requests.

## License
This project is licensed under the MIT License.

## Contact
Developed by Samir Alam, 2024.
Feel free to reach out for suggestions or collaborations.

