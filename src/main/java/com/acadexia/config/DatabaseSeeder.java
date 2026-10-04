package com.acadexia.config;

import com.acadexia.dao.AuditLogDAO;
import com.acadexia.dao.DepartmentDAO;
import com.acadexia.dao.FacultyDAO;
import com.acadexia.dao.UserDAO;
import com.acadexia.model.AuditLog;
import com.acadexia.model.Department;
import com.acadexia.model.Faculty;
import com.acadexia.model.Role;
import com.acadexia.model.User;

import java.sql.*;

/**
 * Automatically seeds the database with initial institutional demo data
 * (Principal, Admin, HODs, CFA, Subject Faculty, Students, Classes, Subjects, and Cross-Department assignments)
 * if the users table is currently empty.
 */
public final class DatabaseSeeder {

    private DatabaseSeeder() {}

    public static void seedIfEmpty() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int userCount = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) userCount = rs.getInt(1);
            }

            if (userCount > 0) {
                return; // Database already seeded
            }

            System.out.println("🌱 Initializing Acadexia starter database records...");

            UserDAO userDAO = new UserDAO();
            DepartmentDAO deptDAO = new DepartmentDAO();
            FacultyDAO facDAO = new FacultyDAO();

            // 1. Create Departments
            deptDAO.createDepartment("CSE", "Computer Science & Engineering");
            deptDAO.createDepartment("MATH", "Mathematics & Basic Sciences");
            deptDAO.createDepartment("ECE", "Electronics & Communication Engineering");

            Department cseDept = deptDAO.getAllDepartments().stream().filter(d -> d.getCode().equals("CSE")).findFirst().orElse(null);
            Department mathDept = deptDAO.getAllDepartments().stream().filter(d -> d.getCode().equals("MATH")).findFirst().orElse(null);

            int cseDeptId = cseDept != null ? cseDept.getId() : 1;
            int mathDeptId = mathDept != null ? mathDept.getId() : 2;

            // 2. Create Principal
            User principalUser = new User();
            principalUser.setUsername("principal");
            principalUser.setFullName("Dr. K. S. Menon");
            principalUser.setEmail("principal@acadexia.edu");
            principalUser.setPhone("9876543210");
            principalUser.setRole(Role.PRINCIPAL);
            principalUser.setActive(true);
            userDAO.createUser(principalUser, "principal123");

            // 3. Create System Admin
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setFullName("IT Systems Administrator");
            adminUser.setEmail("admin@acadexia.edu");
            adminUser.setPhone("9876543211");
            adminUser.setRole(Role.ADMIN);
            adminUser.setActive(true);
            userDAO.createUser(adminUser, "admin123");

            // 4. Create Faculty 1: CSE HOD (Dr. Rajesh Sharma)
            User fac1User = new User();
            fac1User.setUsername("rajesh.cse");
            fac1User.setFullName("Dr. Rajesh Sharma");
            fac1User.setEmail("rajesh.cse@acadexia.edu");
            fac1User.setPhone("9876543212");
            fac1User.setDepartmentId(cseDeptId);
            fac1User.setRole(Role.FACULTY);
            fac1User.setActive(true);
            int fac1UserId = userDAO.createUser(fac1User, "faculty123");

            Faculty fac1 = new Faculty();
            fac1.setUserId(fac1UserId);
            fac1.setFacultyId("FAC-CS-001");
            fac1.setDesignation("Professor & HOD");
            fac1.setQualification("Ph.D. in Computer Science");
            fac1.setDepartmentId(cseDeptId);
            fac1.setJoiningDate(Date.valueOf("2015-06-15"));
            int fac1Id = facDAO.createFaculty(fac1);
            deptDAO.updateHod(cseDeptId, fac1Id);

            // 5. Create Faculty 2: Math HOD (Dr. Sarah Varghese) - Teaches cross-departmentally!
            User fac2User = new User();
            fac2User.setUsername("sarah.math");
            fac2User.setFullName("Dr. Sarah Varghese");
            fac2User.setEmail("sarah.math@acadexia.edu");
            fac2User.setPhone("9876543213");
            fac2User.setDepartmentId(mathDeptId);
            fac2User.setRole(Role.FACULTY);
            fac2User.setActive(true);
            int fac2UserId = userDAO.createUser(fac2User, "faculty123");

            Faculty fac2 = new Faculty();
            fac2.setUserId(fac2UserId);
            fac2.setFacultyId("FAC-MA-001");
            fac2.setDesignation("Professor & HOD (Math)");
            fac2.setQualification("Ph.D. in Applied Mathematics");
            fac2.setDepartmentId(mathDeptId);
            fac2.setJoiningDate(Date.valueOf("2016-08-01"));
            int fac2Id = facDAO.createFaculty(fac2);
            deptDAO.updateHod(mathDeptId, fac2Id);

            // 6. Create Faculty 3: Class Advisor / CFA for S5 CSE A (Prof. Arun Nair)
            User fac3User = new User();
            fac3User.setUsername("arun.cse");
            fac3User.setFullName("Prof. Arun Nair");
            fac3User.setEmail("arun.cse@acadexia.edu");
            fac3User.setPhone("9876543214");
            fac3User.setDepartmentId(cseDeptId);
            fac3User.setRole(Role.FACULTY);
            fac3User.setActive(true);
            int fac3UserId = userDAO.createUser(fac3User, "faculty123");

            Faculty fac3 = new Faculty();
            fac3.setUserId(fac3UserId);
            fac3.setFacultyId("FAC-CS-002");
            fac3.setDesignation("Assistant Professor & CFA");
            fac3.setQualification("M.Tech in Software Engineering");
            fac3.setDepartmentId(cseDeptId);
            fac3.setJoiningDate(Date.valueOf("2019-07-10"));
            int fac3Id = facDAO.createFaculty(fac3);

            // 7. Create Faculty 4: Subject Faculty (Prof. Priya Menon)
            User fac4User = new User();
            fac4User.setUsername("priya.cse");
            fac4User.setFullName("Prof. Priya Menon");
            fac4User.setEmail("priya.cse@acadexia.edu");
            fac4User.setPhone("9876543215");
            fac4User.setDepartmentId(cseDeptId);
            fac4User.setRole(Role.FACULTY);
            fac4User.setActive(true);
            int fac4UserId = userDAO.createUser(fac4User, "faculty123");

            Faculty fac4 = new Faculty();
            fac4.setUserId(fac4UserId);
            fac4.setFacultyId("FAC-CS-003");
            fac4.setDesignation("Assistant Professor");
            fac4.setQualification("M.Tech in Cybersecurity");
            fac4.setDepartmentId(cseDeptId);
            fac4.setJoiningDate(Date.valueOf("2021-01-18"));
            int fac4Id = facDAO.createFaculty(fac4);

            // 8. Create Class Batch: S5 CSE A
            int classId = 1;
            String insertClassSql = """
                INSERT INTO classes (name, department_id, semester, academic_year, section, advisor_faculty_id)
                VALUES ('S5 CSE', ?, 5, '2024-2025', 'A', ?)
                """;
            try (PreparedStatement ps = conn.prepareStatement(insertClassSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, cseDeptId);
                ps.setInt(2, fac3Id); // Advised by Prof. Arun Nair
                ps.executeUpdate();
                try (ResultSet crs = ps.getGeneratedKeys()) {
                    if (crs.next()) classId = crs.getInt(1);
                }
            }

            // 9. Create Students
            createStudentHelper(conn, userDAO, "rohan.das", "Rohan Das", "REG2024CS001", "01", classId, 2022, "Mr. S. Das", "9811122233");
            createStudentHelper(conn, userDAO, "ananya.sen", "Ananya Sen", "REG2024CS002", "02", classId, 2022, "Mrs. R. Sen", "9811122234");
            createStudentHelper(conn, userDAO, "kevin.paul", "Kevin Paul", "REG2024CS003", "03", classId, 2022, "Mr. J. Paul", "9811122235");

            // 10. Create Subjects
            int sub1Id = createSubjectHelper(conn, "CS501", "Design & Analysis of Algorithms", cseDeptId, 5, 4);
            int sub2Id = createSubjectHelper(conn, "CS502", "Database Management Systems", cseDeptId, 5, 4);
            int sub3Id = createSubjectHelper(conn, "MA301", "Discrete Mathematical Structures", mathDeptId, 5, 4); // Math Dept!
            int sub4Id = createSubjectHelper(conn, "CS503", "Operating System Concepts", cseDeptId, 5, 3);

            // 11. Assign Teachers to Subjects for S5 CSE A (demonstrating cross-department assignment)
            assignTeacherHelper(conn, sub1Id, fac1Id, classId, "2024-2025"); // Algorithms taught by CSE HOD
            assignTeacherHelper(conn, sub2Id, fac3Id, classId, "2024-2025"); // DBMS taught by CFA
            assignTeacherHelper(conn, sub3Id, fac2Id, classId, "2024-2025"); // Discrete Math taught by Math HOD!
            assignTeacherHelper(conn, sub4Id, fac4Id, classId, "2024-2025"); // OS taught by Subject Faculty

            // 12. Seed Timetable for S5 CSE A
            seedTimetable(conn, classId, fac1Id, sub1Id, "MONDAY", 1, "LH-301");
            seedTimetable(conn, classId, fac2Id, sub3Id, "MONDAY", 2, "LH-301");
            seedTimetable(conn, classId, fac3Id, sub2Id, "MONDAY", 3, "LH-301");
            seedTimetable(conn, classId, fac4Id, sub4Id, "MONDAY", 4, "LH-301");

            seedTimetable(conn, classId, fac3Id, sub2Id, "TUESDAY", 1, "LH-301");
            seedTimetable(conn, classId, fac1Id, sub1Id, "TUESDAY", 2, "LH-301");
            seedTimetable(conn, classId, fac4Id, sub4Id, "TUESDAY", 3, "LH-301");
            seedTimetable(conn, classId, fac2Id, sub3Id, "TUESDAY", 4, "LH-301");

            // 13. Seed Starter Marks & Attendance
            seedInitialMarksAndAttendance(conn, classId, fac1Id, sub1Id, sub2Id, sub3Id, sub4Id);

            // 14. Seed Sample Duty Leave Application (at Tier 1 awaiting CFA recommendation)
            seedSampleDutyLeave(conn, 1);

            AuditLogDAO.log(adminUser.getId(), "SYSTEM", "ADMIN", "DATABASE_SEEDED", "SYSTEM", null,
                    "Default college hierarchy, departments, faculty, and sample batch seeded successfully.", AuditLog.Severity.INFO);

            System.out.println("✅ Acadexia starter database successfully seeded!");

        } catch (Exception e) {
            System.err.println("Database seeding error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private static void createStudentHelper(Connection conn, UserDAO userDAO, String username, String fullName,
                                            String registerNo, String rollNo, int classId, int admYear,
                                            String gName, String gPhone) throws SQLException {
        User u = new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setEmail(username + "@acadexia.edu");
        u.setPhone("98000" + rollNo + "123");
        u.setRole(Role.STUDENT);
        u.setActive(true);
        int uid = userDAO.createUser(u, "student123");

        String sql = """
            INSERT INTO students (user_id, register_number, roll_number, class_id, admission_year, guardian_name, guardian_phone)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uid);
            ps.setString(2, registerNo);
            ps.setString(3, rollNo);
            ps.setInt(4, classId);
            ps.setInt(5, admYear);
            ps.setString(6, gName);
            ps.setString(7, gPhone);
            ps.executeUpdate();
        }
    }

    private static int createSubjectHelper(Connection conn, String code, String name, int deptId, int sem, int credits) throws SQLException {
        String sql = "INSERT INTO subjects (code, name, department_id, semester, credits) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setInt(3, deptId);
            ps.setInt(4, sem);
            ps.setInt(5, credits);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private static void assignTeacherHelper(Connection conn, int subjectId, int facultyId, int classId, String yr) throws SQLException {
        String sql = "INSERT INTO subject_assignments (subject_id, faculty_id, class_id, academic_year) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, facultyId);
            ps.setInt(3, classId);
            ps.setString(4, yr);
            ps.executeUpdate();
        }
    }

    private static void seedTimetable(Connection conn, int classId, int facultyId, int subjectId, String day, int period, String room) throws SQLException {
        String sql = "INSERT INTO timetable (class_id, faculty_id, subject_id, day_of_week, period_slot, room_number) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, facultyId);
            ps.setInt(3, subjectId);
            ps.setString(4, day);
            ps.setInt(5, period);
            ps.setString(6, room);
            ps.executeUpdate();
        }
    }

    private static void seedInitialMarksAndAttendance(Connection conn, int classId, int fac1Id, int s1, int s2, int s3, int s4) {
        try {
            // Attendance for past 5 days
            String[] dates = {"2026-09-28", "2026-09-29", "2026-09-30", "2026-10-01", "2026-10-02"};
            String attSql = "INSERT IGNORE INTO attendance (student_id, subject_id, class_id, date, hour, status, marked_by_faculty_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(attSql)) {
                for (int stuId = 1; stuId <= 3; stuId++) {
                    for (String d : dates) {
                        for (int h = 1; h <= 3; h++) {
                            ps.setInt(1, stuId);
                            ps.setInt(2, s1);
                            ps.setInt(3, classId);
                            ps.setDate(4, Date.valueOf(d));
                            ps.setInt(5, h);
                            ps.setString(6, (stuId == 3 && h == 2) ? "ABSENT" : "PRESENT");
                            ps.setInt(7, fac1Id);
                            ps.addBatch();
                        }
                    }
                }
                ps.executeBatch();
            }

            // Series 1 Marks
            String markSql = "INSERT IGNORE INTO marks (student_id, subject_id, exam_type, marks_obtained, max_marks, exam_date, recorded_by_faculty_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(markSql)) {
                // Rohan
                ps.setInt(1, 1); ps.setInt(2, s1); ps.setString(3, "SERIES_1"); ps.setDouble(4, 46.5); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-20")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 1); ps.setInt(2, s2); ps.setString(3, "SERIES_1"); ps.setDouble(4, 44.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-21")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 1); ps.setInt(2, s3); ps.setString(3, "SERIES_1"); ps.setDouble(4, 48.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-22")); ps.setInt(7, fac1Id); ps.addBatch();
                // Consolidated Internals
                ps.setInt(1, 1); ps.setInt(2, s1); ps.setString(3, "INTERNAL"); ps.setDouble(4, 47.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 1); ps.setInt(2, s2); ps.setString(3, "INTERNAL"); ps.setDouble(4, 45.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 1); ps.setInt(2, s3); ps.setString(3, "INTERNAL"); ps.setDouble(4, 49.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();

                // Ananya
                ps.setInt(1, 2); ps.setInt(2, s1); ps.setString(3, "INTERNAL"); ps.setDouble(4, 42.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 2); ps.setInt(2, s2); ps.setString(3, "INTERNAL"); ps.setDouble(4, 43.5); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();
                ps.setInt(1, 2); ps.setInt(2, s3); ps.setString(3, "INTERNAL"); ps.setDouble(4, 46.0); ps.setDouble(5, 50.0); ps.setDate(6, Date.valueOf("2026-09-25")); ps.setInt(7, fac1Id); ps.addBatch();

                ps.executeBatch();
            }

            // Seed an assignment
            String assignSql = """
                INSERT IGNORE INTO assignments (id, title, description, subject_id, class_id, faculty_id, due_date, max_marks)
                VALUES (1, 'Dynamic Programming Problem Set', 'Implement Knapsack, Matrix Chain Multiplication, and LCS in Java', ?, ?, ?, '2026-10-15', 10.0)
                """;
            try (PreparedStatement ps = conn.prepareStatement(assignSql)) {
                ps.setInt(1, s1);
                ps.setInt(2, classId);
                ps.setInt(3, fac1Id);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            System.err.println("Seed marks error: " + e.getMessage());
        }
    }

    private static void seedSampleDutyLeave(Connection conn, int studentId) {
        String sql = """
            INSERT IGNORE INTO duty_leaves (id, student_id, reason, start_date, end_date, total_days, proof_details, status)
            VALUES (1, ?, 'National Level Hackathon Participation at IIT Madras', '2026-10-10', '2026-10-12', 3, 'Official Invitation Letter Ref: IITM/HACK/2026/410', 'PENDING_ADVISOR')
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }
}
