package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public Student getByUserId(int userId) {
        String sql = """
            SELECT s.*, u.full_name, u.email, u.phone, 
                   c.name AS class_name, c.semester, c.department_id,
                   d.name AS department_name
            FROM students s
            JOIN users u ON s.user_id = u.id
            JOIN classes c ON s.class_id = c.id
            JOIN departments d ON c.department_id = d.id
            WHERE s.user_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = mapResultSetToStudent(rs);
                    enrichStudentMetrics(s, conn);
                    return s;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student by user ID: " + e.getMessage());
        }
        return null;
    }

    public Student getByRegisterNumber(String registerNumber) {
        String sql = """
            SELECT s.*, u.full_name, u.email, u.phone, 
                   c.name AS class_name, c.semester, c.department_id,
                   d.name AS department_name
            FROM students s
            JOIN users u ON s.user_id = u.id
            JOIN classes c ON s.class_id = c.id
            JOIN departments d ON c.department_id = d.id
            WHERE s.register_number = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, registerNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = mapResultSetToStudent(rs);
                    enrichStudentMetrics(s, conn);
                    return s;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student by register number: " + e.getMessage());
        }
        return null;
    }

    public List<Student> getStudentsByClass(int classId) {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.*, u.full_name, u.email, u.phone, 
                   c.name AS class_name, c.semester, c.department_id,
                   d.name AS department_name
            FROM students s
            JOIN users u ON s.user_id = u.id
            JOIN classes c ON s.class_id = c.id
            JOIN departments d ON c.department_id = d.id
            WHERE s.class_id = ?
            ORDER BY CAST(s.roll_number AS UNSIGNED), s.register_number
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = mapResultSetToStudent(rs);
                    enrichStudentMetrics(s, conn);
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching students by class: " + e.getMessage());
        }
        return list;
    }

    public List<Student> getStudentsByDepartment(int departmentId) {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.*, u.full_name, u.email, u.phone, 
                   c.name AS class_name, c.semester, c.department_id,
                   d.name AS department_name
            FROM students s
            JOIN users u ON s.user_id = u.id
            JOIN classes c ON s.class_id = c.id
            JOIN departments d ON c.department_id = d.id
            WHERE d.id = ?
            ORDER BY c.semester, CAST(s.roll_number AS UNSIGNED)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = mapResultSetToStudent(rs);
                    enrichStudentMetrics(s, conn);
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching students by department: " + e.getMessage());
        }
        return list;
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.*, u.full_name, u.email, u.phone, 
                   c.name AS class_name, c.semester, c.department_id,
                   d.name AS department_name
            FROM students s
            JOIN users u ON s.user_id = u.id
            JOIN classes c ON s.class_id = c.id
            JOIN departments d ON c.department_id = d.id
            ORDER BY d.name, c.semester, CAST(s.roll_number AS UNSIGNED)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Student s = mapResultSetToStudent(rs);
                enrichStudentMetrics(s, conn);
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all students: " + e.getMessage());
        }
        return list;
    }

    public int createStudent(Student s) throws SQLException {
        String sql = """
            INSERT INTO students (user_id, register_number, roll_number, class_id, admission_year, guardian_name, guardian_phone)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getUserId());
            ps.setString(2, s.getRegisterNumber().toUpperCase().trim());
            ps.setString(3, s.getRollNumber().trim());
            ps.setInt(4, s.getClassId());
            ps.setInt(5, s.getAdmissionYear());
            ps.setString(6, s.getGuardianName());
            ps.setString(7, s.getGuardianPhone());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    s.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Dynamically calculates the student's attendance percentage and SGPA/CGPA.
     */
    private void enrichStudentMetrics(Student s, Connection conn) {
        // Attendance calculation
        String attSql = """
            SELECT 
                COUNT(*) AS total_hours,
                SUM(CASE WHEN status IN ('PRESENT', 'DUTY_LEAVE') THEN 1 ELSE 0 END) AS attended_hours
            FROM attendance
            WHERE student_id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(attSql)) {
            ps.setInt(1, s.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_hours");
                    int attended = rs.getInt("attended_hours");
                    s.setAttendancePercentage(total > 0 ? (attended * 100.0 / total) : 100.0);
                }
            }
        } catch (SQLException ignored) {}

        // SGPA calculation from marks
        String sgpaSql = """
            SELECT m.marks_obtained, m.max_marks, sub.credits
            FROM marks m
            JOIN subjects sub ON m.subject_id = sub.id
            WHERE m.student_id = ? AND m.exam_type = 'INTERNAL'
            """;
        try (PreparedStatement ps = conn.prepareStatement(sgpaSql)) {
            ps.setInt(1, s.getId());
            try (ResultSet rs = ps.executeQuery()) {
                double totalCredits = 0;
                double weightedPoints = 0;
                while (rs.next()) {
                    double marks = rs.getDouble("marks_obtained");
                    double max = rs.getDouble("max_marks");
                    int credits = rs.getInt("credits");
                    double pct = max > 0 ? (marks / max) * 100.0 : 0.0;
                    double gp = calculateGradePoint(pct);
                    weightedPoints += gp * credits;
                    totalCredits += credits;
                }
                double sgpa = totalCredits > 0 ? (weightedPoints / totalCredits) : 0.0;
                s.setCurrentSgpa(Math.round(sgpa * 100.0) / 100.0);
                s.setCurrentCgpa(s.getCurrentSgpa()); // Baseline CGPA
            }
        } catch (SQLException ignored) {}
    }

    private double calculateGradePoint(double pct) {
        if (pct >= 90) return 10.0;
        if (pct >= 85) return 9.0;
        if (pct >= 80) return 8.5;
        if (pct >= 75) return 8.0;
        if (pct >= 70) return 7.5;
        if (pct >= 65) return 7.0;
        if (pct >= 60) return 6.5;
        if (pct >= 50) return 6.0;
        if (pct >= 40) return 5.5;
        return 0.0;
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setRegisterNumber(rs.getString("register_number"));
        s.setRollNumber(rs.getString("roll_number"));
        s.setClassId(rs.getInt("class_id"));
        s.setAdmissionYear(rs.getInt("admission_year"));
        s.setGuardianName(rs.getString("guardian_name"));
        s.setGuardianPhone(rs.getString("guardian_phone"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setClassName(rs.getString("class_name"));
        s.setSemester(rs.getInt("semester"));
        s.setDepartmentId(rs.getInt("department_id"));
        s.setDepartmentName(rs.getString("department_name"));
        return s;
    }
}
