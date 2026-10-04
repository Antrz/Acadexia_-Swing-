package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        String sql = """
            SELECT s.*, d.name AS department_name
            FROM subjects s
            JOIN departments d ON s.department_id = d.id
            ORDER BY d.name, s.semester, s.code
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all subjects: " + e.getMessage());
        }
        return list;
    }

    public List<Subject> getSubjectsByClass(int classId) {
        List<Subject> list = new ArrayList<>();
        String sql = """
            SELECT s.*, d.name AS department_name
            FROM subjects s
            JOIN departments d ON s.department_id = d.id
            JOIN classes c ON s.department_id = c.department_id AND s.semester = c.semester
            WHERE c.id = ?
            ORDER BY s.code
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subjects for class: " + e.getMessage());
        }
        return list;
    }

    /**
     * Cross-Departmental Subject Retrieval:
     * Returns all courses assigned to this faculty member across ANY department and class.
     */
    public List<AssignedCourse> getAssignedCoursesForFaculty(int facultyId) {
        List<AssignedCourse> list = new ArrayList<>();
        String sql = """
            SELECT sa.id AS assignment_id, 
                   s.id AS subject_id, s.code AS subject_code, s.name AS subject_name, s.credits,
                   c.id AS class_id, c.name AS class_name, c.semester, c.section, c.academic_year,
                   d.name AS department_name
            FROM subject_assignments sa
            JOIN subjects s ON sa.subject_id = s.id
            JOIN classes c ON sa.class_id = c.id
            JOIN departments d ON c.department_id = d.id
            WHERE sa.faculty_id = ?
            ORDER BY d.name, c.semester, s.code
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AssignedCourse ac = new AssignedCourse(
                            rs.getInt("assignment_id"),
                            rs.getInt("subject_id"),
                            rs.getString("subject_code"),
                            rs.getString("subject_name"),
                            rs.getInt("credits"),
                            rs.getInt("class_id"),
                            rs.getString("class_name"),
                            rs.getInt("semester"),
                            rs.getString("section"),
                            rs.getString("academic_year"),
                            rs.getString("department_name")
                    );
                    list.add(ac);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching assigned courses for faculty: " + e.getMessage());
        }
        return list;
    }

    /**
     * For CFA / Class Advisor:
     * Returns all faculty members assigned to teach subjects in this class.
     */
    public List<ClassTeacherMapping> getFacultyAssignedToClass(int classId) {
        List<ClassTeacherMapping> list = new ArrayList<>();
        String sql = """
            SELECT sa.id AS assignment_id,
                   s.code AS subject_code, s.name AS subject_name,
                   f.id AS faculty_id, f.faculty_id AS emp_code, f.designation,
                   u.full_name AS faculty_name, u.email, u.phone,
                   dept.name AS faculty_department
            FROM subject_assignments sa
            JOIN subjects s ON sa.subject_id = s.id
            JOIN faculty f ON sa.faculty_id = f.id
            JOIN users u ON f.user_id = u.id
            JOIN departments dept ON f.department_id = dept.id
            WHERE sa.class_id = ?
            ORDER BY s.code
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ClassTeacherMapping m = new ClassTeacherMapping(
                            rs.getInt("assignment_id"),
                            rs.getString("subject_code"),
                            rs.getString("subject_name"),
                            rs.getInt("faculty_id"),
                            rs.getString("emp_code"),
                            rs.getString("designation"),
                            rs.getString("faculty_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("faculty_department")
                    );
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching class teacher mapping: " + e.getMessage());
        }
        return list;
    }

    public boolean assignTeacherToSubject(int subjectId, int facultyId, int classId, String academicYear) {
        String sql = """
            INSERT INTO subject_assignments (subject_id, faculty_id, class_id, academic_year)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE faculty_id = VALUES(faculty_id)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, facultyId);
            ps.setInt(3, classId);
            ps.setString(4, academicYear);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error assigning teacher to subject: " + e.getMessage());
            return false;
        }
    }

    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getInt("id"));
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));
        s.setDepartmentId(rs.getInt("department_id"));
        s.setDepartmentName(rs.getString("department_name"));
        s.setSemester(rs.getInt("semester"));
        s.setCredits(rs.getInt("credits"));
        s.setMaxInternalMarks(rs.getInt("max_internal_marks"));
        s.setMaxExternalMarks(rs.getInt("max_external_marks"));
        return s;
    }

    // Helper Record DTOs
    public record AssignedCourse(int assignmentId, int subjectId, String subjectCode, String subjectName,
                                 int credits, int classId, String className, int semester, String section,
                                 String academicYear, String departmentName) {
        @Override
        public String toString() {
            return subjectCode + " - " + subjectName + " (" + className + " S" + semester + " " + section + " - " + departmentName + ")";
        }
    }

    public record ClassTeacherMapping(int assignmentId, String subjectCode, String subjectName,
                                      int facultyId, String employeeCode, String designation,
                                      String facultyName, String email, String phone, String facultyDepartment) {}
}
