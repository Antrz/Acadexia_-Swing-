<p align="center">
  <img src="https://img.shields.io/badge/Java-27-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 27"/>
  <img src="https://img.shields.io/badge/Swing-FlatLaf%20Dark-6C63FF?style=for-the-badge&logo=java&logoColor=white" alt="Swing FlatLaf"/>
  <img src="https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License"/>
</p>

# 🎓 Acadexia — College Management System

> A comprehensive, role-based academic governance desktop application built with **Java Swing** and a modern **FlatLaf Dark** UI. Manages the complete lifecycle of students, faculty, departments, and institutional administration — from attendance and marks entry to a 3-tier duty leave approval pipeline.

---

## ✨ Key Features

### 🔐 Role-Based Access Control (4 Roles)

| Role | Capabilities |
|------|-------------|
| **🎓 Student** | Marksheets, internal marks, attendance tracker, timetable, submit anonymous faculty feedback, grievance redressal, 3-tier duty leave applications |
| **👨‍🏫 Faculty** | Composite role — a single faculty member can act as **Subject Teacher**, **Class Faculty Advisor (CFA)**, and **HOD** simultaneously. Dashboard tabs unlock dynamically based on assignments |
| **👑 Principal** | Executive authority — final-tier duty leave sanctions, HOD appointments, institution-wide academic analytics, grievance oversight |
| **🛡️ System Admin** | User provisioning, password resets, database health monitor, **real-time security & audit logs viewer** |

### 🧩 Composite Faculty Architecture
- Faculty roles are **not mutually exclusive** — one person can teach across departments, advise a class, and head a department at the same time
- **Cross-departmental teaching** supported (e.g., Mathematics HOD teaching Discrete Math to CSE students)
- Dashboard dynamically enables **My Teaching**, **Class Advisor**, and **HOD Portal** tabs based on active assignments

### 📋 3-Tier Duty Leave Pipeline
```
Student applies → CFA recommends (Tier 1) → HOD endorses (Tier 2) → Principal sanctions (Tier 3)
```

### 🛡️ Dual-Layer SQL Injection Prevention
1. **UI Layer**: Custom Swing `DocumentFilter`s block SQL meta-characters (`;`, `--`, `/*`, quotes) at keystroke/paste time with visual red-border feedback
2. **DAO Layer**: 100% parameterized `PreparedStatement` queries — zero string concatenation in SQL. Passwords hashed with **BCrypt**

---

## 🏗️ Architecture

```
com.acadexia/
├── Main.java                          # Application entry point
├── config/
│   ├── DatabaseConnection.java        # Connection factory (fresh per-call)
│   └── DatabaseSeeder.java            # Auto-seeds demo data on first run
├── dao/                               # Data Access Objects (14 DAOs)
│   ├── UserDAO.java                   # Authentication & user CRUD
│   ├── StudentDAO.java, FacultyDAO.java, DepartmentDAO.java
│   ├── SubjectDAO.java, ClassDAO.java, AttendanceDAO.java
│   ├── MarkDAO.java, AssignmentDAO.java, TimetableDAO.java
│   ├── DutyLeaveDAO.java, FeedbackDAO.java, GrievanceDAO.java
│   └── AuditLogDAO.java              # Security event logging
├── model/                             # 16 POJO entity classes
├── security/
│   ├── InputFilterFactory.java        # Swing DocumentFilter suite
│   ├── SqlSanitizerFilter.java        # SQL injection pattern blocker
│   ├── SecurityUtils.java             # BCrypt hashing utilities
│   └── SessionManager.java           # Logged-in user session context
└── ui/
    ├── auth/    → LandingFrame.java   # Login portal with quick-fill demo buttons
    ├── common/  → UITheme.java, UIComponents.java  # Design system
    ├── student/ → StudentDashboardFrame.java
    ├── faculty/ → FacultyDashboardFrame.java
    ├── principal/ → PrincipalDashboardFrame.java
    └── admin/   → AdminDashboardFrame.java
```

**Database**: 15 tables with foreign key relationships, unique constraints, and referential integrity  
**Schema**: Auto-created on first run via `schema.sql`

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| **Java JDK** | 21 or higher (developed with JDK 27) |
| **MySQL Server** | 8.0 or higher |
| **Maven** | 3.9 or higher |

### Setup

**1. Clone the repository**
```bash
git clone https://github.com/Antrz/Acadexia_-Swing-.git
cd Acadexia_-Swing-
```

**2. Configure database credentials**
```bash
# Copy the template and fill in your MySQL password
cp src/main/resources/db.properties.template src/main/resources/db.properties
```

Edit `src/main/resources/db.properties`:
```properties
db.host=localhost
db.port=3306
db.name=acadexia_db
db.user=root
db.password=YOUR_PASSWORD_HERE
```

> ⚠️ `db.properties` is in `.gitignore` — your credentials will never be committed.

**3. Build and run**
```bash
mvn clean compile
mvn exec:java
```

The application will:
- Automatically create the `acadexia_db` database if it doesn't exist
- Execute `schema.sql` to create all 15 tables
- Seed demo data (departments, faculty, students, timetable, sample marks) on first run
- Launch the FlatLaf Dark themed login portal

---

## 🔑 Demo Credentials

The database seeder pre-loads these accounts for testing:

| Role | Login ID | Password | Character |
|---|---|---|---|
| 🎓 Student | `REG2024CS001` | `student123` | Rohan Das |
| 👨‍🏫 CFA (Class Advisor) | `FAC-CS-002` | `faculty123` | Prof. Arun Nair |
| 👨‍🏫 HOD (CSE) | `FAC-CS-001` | `faculty123` | Dr. Rajesh Sharma |
| 👨‍🏫 HOD (Math) — *Cross-dept* | `FAC-MA-001` | `faculty123` | Dr. Sarah Varghese |
| 👑 Principal | `principal` | `principal123` | Dr. K. S. Menon |
| 🛡️ System Admin | `admin` | `admin123` | IT Administrator |

> 💡 The login screen includes **Quick-Fill** buttons to auto-populate any of these credentials.

---

## 📊 Database Schema (ER Overview)

```
departments ─┬─ faculty (hod_faculty_id)
             ├─ classes (advisor_faculty_id)
             ├─ subjects
             └─ users (department_id)

users ──── students (user_id) ──── attendance, marks, duty_leaves,
       │                           feedback, grievances, assignment_submissions
       └── faculty (user_id) ──── subject_assignments (cross-dept capable)
                                   timetable, assignments

audit_logs ← tracks all system events with severity levels
```

---

## 🔧 Tech Stack

| Component | Technology |
|---|---|
| **Language** | Java 27 (JDK 21+ compatible) |
| **UI Framework** | Java Swing (`javax.swing.*`) |
| **Look & Feel** | FlatLaf 3.5.4 Dark Theme |
| **Database** | MySQL 8.0+ |
| **ORM / Data** | Raw JDBC with PreparedStatement |
| **Security** | BCrypt (jBCrypt 0.4), Custom DocumentFilters |
| **Build** | Apache Maven 3.9+ |

---

## 📁 Project Structure

```
Acadexia (Swing)/
├── pom.xml                                 # Maven build config & dependencies
├── PROGRESS.md                             # Detailed implementation checklist & changelog
├── README.md                               # This file
├── .gitignore                              # Excludes target/, db.properties, IDE files
└── src/main/
    ├── java/com/acadexia/                  # 44 Java source files
    │   ├── config/    (2 files)            # DB connection & seeder
    │   ├── dao/       (14 files)           # Data access layer
    │   ├── model/     (16 files)           # Entity POJOs
    │   ├── security/  (4 files)            # Auth, filters, session
    │   └── ui/        (7 files)            # Swing GUI dashboards
    └── resources/
        ├── db.properties.template          # Safe credential template (committed)
        ├── db.properties                   # Your local credentials (gitignored)
        └── schema.sql                      # Auto-executed DDL (15 tables)
```

---

## 👥 Authors

- **Ann Treesa** — [GitHub](https://github.com/Antrz)

---

## 📄 License

This project is developed as an academic mini project.
