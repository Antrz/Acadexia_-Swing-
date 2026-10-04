# Acadexia - College Management System

> **Tech Stack**: Java 27 (compatible with JDK 21+), Strictly Java Swing (`javax.swing.*`), Modern FlatLaf Dark Theme, MySQL (Port 3306), Maven  
> **Status**: Completed - All Phases Built, Compiled & Packaged  
> **Last Updated**: 2026-10-04  
> **UI Mandate**: 100% Native Desktop Java Swing (`javax.swing.*` / `java.awt.*`). No web wrappers or external frontends.

---

## 📌 Project Overview
Acadexia is a comprehensive desktop College Management System built strictly with **Java Swing**. It provides role-based access for:
- **Principal**: Executive head handling high-level institutional approvals (Final Duty Leave sanctions, HOD appointments, Institutional analytics, Grievance oversight).
- **System Admin**: Technical administrator managing user credentials, account provisioning, database settings, and inspecting system activity & security **Audit Logs**.
- **Faculty (Composite Roles)**: Subject Faculty (all classes), Class Advisor/CFA (assigned class), and HOD (department head).
- **Students**: Academic marksheets, internal evaluations, attendance, timetable, feedback, grievances, and duty leave submissions.

---

## 🗺️ System Architecture & Workflow (Based on Flowchart)

```mermaid
flowchart TD
    Start([Start]) --> Landing[Landing / Welcome Page]
    Landing --> Login[Login Portal]
    Login --> RoleCheck{Role Selection}

    %% Student Branch
    RoleCheck -->|Student| StudentPortal[Student Dashboard]
    StudentPortal --> S1[View Results / Marksheet]
    StudentPortal --> S2[Internal Marks]
    StudentPortal --> S3[Attendance Tracker]
    StudentPortal --> S4[SGPA / CGPA Analytics]
    StudentPortal --> S5[Submit Faculty Feedback]
    StudentPortal --> S6[Grievance Redressal]
    StudentPortal --> S7[Class Timetable]
    StudentPortal --> S8[Apply for Duty Leave]

    %% Faculty Branch (Unified Multi-Role Portal)
    RoleCheck -->|Faculty| FacultyPortal[Faculty Dashboard]
    FacultyPortal --> FBase[Subject Faculty Capabilities\n(Available for all assigned classes)]
    FBase --> F1[Subject Materials & Modules]
    FBase --> F2[Assignments: Create & Grade]
    FBase --> F3[Daily Attendance Marking]
    FBase --> F4[View Anonymous Student Feedback]
    FBase --> F5[Academic Reports & Analytics]
    FBase --> F6[Personal Teaching Timetable]

    FacultyPortal -.->|If Assigned as CFA| CFAPortal[Class Advisor CFA Capabilities]
    CFAPortal --> C1[Advised Class Student Directory]
    CFAPortal --> C2[Faculty Assigned to Class]
    CFAPortal --> C3[Assignment Tracker: Submitted/Pending]
    CFAPortal --> C4[All-Subject Marks & Grade Sheet]
    CFAPortal --> C5[Class SGPA & CGPA Computation]
    CFAPortal --> C6[Tier-1 Duty Leave Recommendations]

    FacultyPortal -.->|If Assigned as HOD| HODPortal[HOD Capabilities]
    HODPortal --> H1[Department-wide Student Details]
    HODPortal --> H2[Semester Management]
    HODPortal --> H3[Assign Class Advisor & Subject Teachers]
    HODPortal --> H4[Tier-2 Department Duty Leave Endorsement]
    HODPortal --> H5[HOD Subject Teaching]

    %% Principal Branch (Executive Academic Authority)
    RoleCheck -->|Principal| PrincipalPortal[Principal Dashboard]
    PrincipalPortal --> P1[Department & HOD Governance]
    PrincipalPortal --> P2[Final Tier-3 Duty Leave Approvals]
    PrincipalPortal --> P3[College-wide Academic & Result Analytics]
    PrincipalPortal --> P4[Institutional Grievance Redressal Oversight]

    %% System Admin Branch (Technical & Audit Management)
    RoleCheck -->|System Admin| AdminPortal[System Admin Dashboard]
    AdminPortal --> A1[User Account Management & Role Provisioning]
    AdminPortal --> A2[System & Security Audit Logs Viewer]
    AdminPortal --> A3[Database Connection & System Configuration]
    AdminPortal --> A4[Master Data Export & Backup]

    %% Logout Flow
    S1 & S2 & S3 & S4 & S5 & S6 & S7 & S8 --> Logout[Logout]
    F1 & F2 & F3 & F4 & F5 & F6 --> Logout
    C1 & C2 & C3 & C4 & C5 & C6 --> Logout
    H1 & H2 & H3 & H4 & H5 --> Logout
    P1 & P2 & P3 & P4 --> Logout
    A1 & A2 & A3 & A4 --> Logout
    Logout --> Landing
```

> 💡 **Key Architecture & Security Notes**:  
> 1. **Unique Institutional Identifiers**:
>    - **Students**: Every student has a unique **Register Number** (e.g., `REG2024CS001`) and class **Roll Number**. Students log in directly using their **Register Number** + password.
>    - **Faculty**: Every faculty member has a unique **Faculty ID / Employee Code** (e.g., `FAC-CS-101`). Faculty log in using their **Faculty ID** + password.
>    - **Principal**: Logs in using Principal ID (`PRINCIPAL`) + password.
>    - **System Admin**: Technical administrators log in with their **Admin Username** + password.
> 2. **Principal vs. System Admin Separation of Concerns**:
>    - **Principal**: Holds the ultimate institutional authority. Academic and administrative approvals (such as final Duty Leave sanctions, approving HOD appointments, and grievance escalations) are strictly reserved for the Principal.
>    - **System Admin**: Focuses on IT administration and system governance. System Admins manage credentials, view active user sessions, monitor security alerts, and inspect **System Audit Logs** (tracking logins, SQL injection attempts blocked, record changes). They do not approve academic duty leaves.
> 3. **3-Tier Duty Leave Approval Pipeline**:
>    - **Tier 1 (Class Advisor / CFA)**: Reviews student duty leave application and proof, gives recommendation (Recommended / Not Recommended).
>    - **Tier 2 (HOD)**: Endorses application with department remarks.
>    - **Tier 3 (Principal)**: Grants the final institutional approval or rejection.
> 4. **Composite Faculty Roles**: Faculty roles are **not mutually exclusive**. A single faculty member can teach subjects across multiple classes (as a Subject Faculty), act as the designated **Class Faculty Advisor (CFA)** for a specific class batch, and simultaneously serve as the **Head of Department (HOD)**. The Faculty Dashboard dynamically enables the respective workspace tabs (Subject Teaching, Class Advisor Portal, and HOD Portal) based on the logged-in faculty member's active assignments.  
> 5. **Cross-Departmental Teaching**: HODs and faculty are **not restricted to teaching within their own department**. For example, a Mathematics HOD or Humanities faculty can be assigned to teach subjects (e.g., Discrete Mathematics, Engineering Economics) to students of Computer Science, Mechanical, etc. In their **"My Teaching"** view, faculty see all classes and subjects they teach across any department, while their **"HOD Portal"** strictly scopes administrative governance to their own department.  
> 6. **Dual-Layer SQL Injection & Exploit Prevention**:
>    - **UI Input Layer (Swing `DocumentFilter`)**: All `JTextField`, `JPasswordField`, and `JTextArea` inputs are guarded by custom `DocumentFilter`s (`SqlSanitizerFilter`, `AlphaNumericFilter`, `NumericRangeFilter`). These actively block SQL meta-characters (`;`, `--`, `/*`, `'`, `"`, `\0`), prohibit dangerous keywords, enforce strict length boundaries, and provide immediate visual cues if invalid input is typed or pasted.
>    - **DAO / Data Layer**: 100% strict enforcement of `java.sql.PreparedStatement` with parameterized placeholders (`?`). String concatenation in SQL statements is strictly banned throughout the application. Salting & hashing passwords via BCrypt.

---

## 📋 Implementation Checklist

### Phase 1: Project Setup & Core Infrastructure
- [x] Maven `pom.xml` configuration with dependencies:
  - FlatLaf Dark Theme (Modern Look and Feel & Themes)
  - MySQL Connector/J driver (`com.mysql:mysql-connector-j`)
  - FlatLaf Extras (Icons, font utilities)
  - BCrypt (`org.mindrot:jbcrypt`) for secure password hashing
- [x] Database Connection Manager (`DatabaseConnection.java`) & `db.properties` configuration (host, port, database, username, password, auto-create database & tables)
- [x] Database Schema & Migrations (`schema.sql`):
  - `users` (id, username, password_hash, role ['STUDENT', 'FACULTY', 'PRINCIPAL', 'ADMIN'], full_name, email, department_id, created_at)
  - `departments` (id, code, name, hod_faculty_id)
  - `classes` (id, name, department_id, semester, academic_year, advisor_faculty_id)
  - `students` (id, user_id, register_number UNIQUE, roll_number, class_id, admission_year)
  - `faculty` (id, user_id, faculty_id UNIQUE / employee_code, designation, department_id)
  - `subjects` (id, code, name, department_id, semester, credits)
  - `subject_assignments` (id, subject_id, faculty_id, class_id, academic_year)
  - `attendance` (id, student_id, subject_id, date, status, hour)
  - `marks` (id, student_id, subject_id, exam_type, marks_obtained, max_marks)
  - `assignments` (id, title, description, subject_id, due_date, max_marks)
  - `assignment_submissions` (id, assignment_id, student_id, submission_date, status, marks)
  - `duty_leaves` (id, student_id, reason, start_date, end_date, proof_url, status, advisor_remarks, hod_remarks, principal_remarks, approved_by_principal_id)
  - `feedback` (id, student_id, faculty_id, subject_id, rating, comments, created_at)
  - `grievances` (id, student_id, category, description, status, resolution, created_at)
  - `timetable` (id, class_id, faculty_id, subject_id, day_of_week, time_slot, room_number)
  - `audit_logs` (id, user_id, action, entity_type, entity_id, timestamp, ip_address, details, status)
- [x] Database Connection Manager & Initial Data Seeder (Default Principal, Admin, Sample Departments, HODs, Advisors, Subjects, Students)

### Phase 2: Design System, Shared Components & Input Security
- [x] Modern UI Theme Configuration (`UITheme.java` with FlatLaf Dark dynamic parameters)
- [x] Input Security & SQL Injection Prevention Filters:
  - Custom Swing `DocumentFilter` suite (`SqlSanitizerFilter`, `AlphaNumericFilter`, `NumericRangeFilter`, `IdentifierFilter`, `InputFilterFactory`)
  - Real-time keystroke and paste sanitization blocking SQL injection patterns (`;`, `--`, `/*`, quotes, dangerous escape sequences)
  - Visual border feedback (red border / tooltip hint) when invalid/blocked characters are attempted
- [x] Reusable UI Components (`UIComponents.java`):
  - Custom Cards with shadows and rounded borders
  - Stat Badges & KPI summary tiles
  - Styled Data Tables with search, filter, and sorting
  - Custom Form Controls, Date Pickers, and Status Pills
  - Modern Sidebar Navigation Bar with active tab indicators
  - Toast / Notification popup system

### Phase 3: Authentication & Landing Flow
- [x] Modern Landing Page (`LandingFrame.java`) with College branding, hero cards, and quick demo credentials shortcuts
- [x] Multi-Role Login Screen supporting Institutional Identifiers:
  - Students log in using **Register Number** + Password
  - Faculty log in using **Faculty ID / Employee Code** + Password
  - Principal logs in using **Principal ID / Username** + Password
  - System Admins log in using **Admin Username** + Password
- [x] Session Context manager (`SessionManager.java` stores logged-in user profile, permissions, active role)

### Phase 4A: Principal Portal (Executive Governance & Approvals)
- [x] Executive Institutional Dashboard (`PrincipalDashboardFrame.java`) (Total students, faculty, departments, live attendance summary)
- [x] Department Governance & HOD Appointments (Assign HOD to departments)
- [x] Tier-3 Duty Leave Sanction Dashboard (Final approve / reject with principal remarks)
- [x] Institutional Academic Analytics & Grade Sheets (Class-wise & Department-wise SGPA/CGPA trends)
- [x] Grievance Redressal Oversight (Review escalated student grievances & resolutions)

### Phase 4B: System Admin Portal (Technical & Audit Management)
- [x] User Account Management (`AdminDashboardFrame.java`) (Provisioning, password resets, role management, deactivation)
- [x] **System & Security Audit Logs Viewer**:
  - Live filterable table of all system events (Logins, Logout, Record Modifications, Duty Leave decisions)
  - Security Alert Logs (Flagged SQL injection attempts & blocked keystrokes)
  - Interactive SQL injection simulation tool for testing
- [x] Database Connection Settings & Health Monitor
- [x] Master Data Backup & Maintenance utilities

### Phase 5: Faculty Module (Unified Multi-Role Dashboard)
- [x] Contextual Module Activator (`FacultyDashboardFrame.java`):
  - **My Teaching (Subject Faculty)**: Always available for all courses/classes assigned to this faculty member (Attendance register, Internals & Exam Marks, Assignments, Feedback viewer, Timetable).
  - **Class Advisor (CFA) Tab**: Dynamically unlocked if faculty is assigned as CFA to a class (Class student list, all-subject marksheet overview, assignment completion status, duty leave review & forwarding, SGPA/CGPA computation).
  - **HOD Portal Tab**: Dynamically unlocked if faculty is designated Head of Department (Department-wide students, semester configs, assigning CFAs and subject teachers, department duty leaves).
- [x] **Subject Faculty Features**:
  - Class & Subject selector dropdown (switch easily between assigned courses/sections, including cross-departmental courses)
  - Attendance Entry (Daily register with quick Present/Absent toggles, date & hour selection)
  - Internal Marks Entry (Series tests, internals, assignments, practicals)
  - Assignment Management (Post new assignment, track submissions, score submissions)
  - View Student Feedback (Anonymous ratings and student commentary)
  - Personal Teaching Timetable
- [x] **Class Advisor (CFA) Features**:
  - Advised class master directory with overall attendance and cumulative grades
  - View subject teachers assigned to their class
  - Class-wide assignment submission monitor (filter by submitted / pending)
  - Consolidated marks overview across all subjects for the class
  - SGPA / CGPA computation & academic standing reports
  - First-tier Duty Leave review and recommendation
- [x] **HOD Features**:
  - Department overview & semester configurations
  - Assign Faculty Advisors (CFA) to class batches
  - Assign Subject Teachers to subjects/sections (with universal cross-department faculty picker)
  - Second-tier Duty Leave review & department recommendation
  - Department performance reports

### Phase 6: Student Module
- [x] Student Profile & Overview (`StudentDashboardFrame.java`)
- [x] Marksheet & Result Viewer (Internal marks, Exam grades, SGPA/CGPA trends)
- [x] Attendance Tracker with percentage warning indicator (<75% alerts)
- [x] Timetable Schedule view
- [x] Submit Anonymous Faculty Feedback
- [x] Grievance Redressal submission and tracking
- [x] Apply for Duty Leave with file/reason attachment and live status tracking (3-tier pipeline)

### Phase 7: Polish, Testing & Packaging
- [x] End-to-end compilation & packaging (`mvn clean compile`, `mvn package`)
- [x] UI responsiveness and high-DPI scaling checks
- [x] Executable JAR build setup via Maven (`target/acadexia-swing-1.0.0.jar`)

---

## 📝 Change Log
- **2026-10-04**: 
  - Project initialized based on handwritten flowchart.
  - Created `PROGRESS.md` covering all entities and workflows.
  - Configured tech stack preferences: Java 27, Swing FlatLaf Dark Theme, MySQL 3306, Maven.
  - **Refined Faculty Architecture**: Updated data model and UI architecture so that Faculty roles are composite, not mutually exclusive. A faculty member can simultaneously teach multiple subjects across different classes, advise a specific class (CFA), and serve as an HOD. The Faculty dashboard dynamically activates respective tabs according to active assignments.
  - **Cross-Departmental Teaching Support**: Explicitly architected `subject_assignments` and the faculty dashboard to support inter-departmental teaching (e.g., Mathematics or Basic Science HOD/faculty teaching courses to Computer Science or Mechanical batches). HOD administrative scope remains securely bounded to their home department while their teaching scope is universal.
  - **Unique Institutional Identifiers**: Integrated unique **Register Number** for Students and **Faculty ID / Employee Code** for Faculty across the database schema and authentication login portal.
  - **Strict Java Swing Mandate**: Confirmed pure native desktop implementation using Java Swing (`javax.swing.*`, `java.awt.*`) with FlatLaf Dark L&F for modern desktop styling. No web components or web wrappers.
  - **SQL Injection Prevention & Input Filtering**: Implemented dual-layer protection: (1) Custom Swing `DocumentFilter`s attached to all text inputs to filter and reject SQL injection patterns, characters, and malicious payloads at keystroke/paste time, and (2) 100% parameterized `PreparedStatement` queries at the DAO layer.
  - **Principal Role & System Admin Audit Separation**: Added dedicated **Principal** role for executive sanctions (Tier-3 final Duty Leave approvals, HOD appointments, academic analytics oversight) and separated **System Admin** into a technical management portal with dedicated **System & Security Audit Logs Viewer** (monitoring logins, user modifications, and blocked SQL injection attempts).
  - **Full Implementation Complete**: All 44 source files created across models, DAOs, security filters, database auto-initialization/seeder, and UI dashboards (Landing, Student, Faculty, Principal, and System Admin). Verified compilation with `mvn clean compile` and generated executable JAR with `mvn package`.

