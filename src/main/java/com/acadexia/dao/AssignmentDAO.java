package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Assignment;
import com.acadexia.model.AssignmentSubmission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDAO {

    public boolean createAssignment(Assignment a) {
        String sql = """
            INSERT INTO assignments (title, description, subject_id, class_id, faculty_id, due_date, max_marks)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getDescription());
            ps.setInt(3, a.getSubjectId());
            ps.setInt(4, a.getClassId());
            ps.setInt(5, a.getFacultyId());
            ps.setDate(6, a.getDueDate());
            ps.setDouble(7, a.getMaxMarks());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating assignment: " + e.getMessage());
            return false;
        }
    }

    public List<Assignment> getAssignmentsForClass(int classId, Integer currentStudentId) {
        List<Assignment> list = new ArrayList<>();
        String sql = """
            SELECT a.*, s.code AS subject_code, s.name AS subject_name,
                   c.name AS class_name, u.full_name AS faculty_name,
                   (SELECT COUNT(*) FROM assignment_submissions sub WHERE sub.assignment_id = a.id) AS total_submissions,
                   (SELECT COUNT(*) FROM students stu WHERE stu.class_id = a.class_id) AS total_students,
                   sub_cur.status AS student_sub_status,
                   sub_cur.marks_obtained AS student_marks
            FROM assignments a
            JOIN subjects s ON a.subject_id = s.id
            JOIN classes c ON a.class_id = c.id
            JOIN faculty f ON a.faculty_id = f.id
            JOIN users u ON f.user_id = u.id
            LEFT JOIN assignment_submissions sub_cur ON a.id = sub_cur.assignment_id AND sub_cur.student_id = ?
            WHERE a.class_id = ?
            ORDER BY a.due_date DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (currentStudentId != null) {
                ps.setInt(1, currentStudentId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Assignment a = new Assignment();
                    a.setId(rs.getInt("id"));
                    a.setTitle(rs.getString("title"));
                    a.setDescription(rs.getString("description"));
                    a.setSubjectId(rs.getInt("subject_id"));
                    a.setSubjectCode(rs.getString("subject_code"));
                    a.setSubjectName(rs.getString("subject_name"));
                    a.setClassId(rs.getInt("class_id"));
                    a.setClassName(rs.getString("class_name"));
                    a.setFacultyId(rs.getInt("faculty_id"));
                    a.setFacultyName(rs.getString("faculty_name"));
                    a.setDueDate(rs.getDate("due_date"));
                    a.setMaxMarks(rs.getDouble("max_marks"));
                    a.setCreatedAt(rs.getTimestamp("created_at"));
                    a.setTotalSubmissions(rs.getInt("total_submissions"));
                    a.setTotalStudents(rs.getInt("total_students"));
                    String subStatus = rs.getString("student_sub_status");
                    a.setSubmittedByCurrentStudent(subStatus != null && !subStatus.equalsIgnoreCase("PENDING"));
                    if (rs.getObject("student_marks") != null) {
                        a.setMarksAwarded(rs.getDouble("student_marks"));
                    }
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching assignments for class: " + e.getMessage());
        }
        return list;
    }

    public List<Assignment> getAssignmentsByFaculty(int facultyId) {
        List<Assignment> list = new ArrayList<>();
        String sql = """
            SELECT a.*, s.code AS subject_code, s.name AS subject_name,
                   c.name AS class_name, u.full_name AS faculty_name,
                   (SELECT COUNT(*) FROM assignment_submissions sub WHERE sub.assignment_id = a.id) AS total_submissions,
                   (SELECT COUNT(*) FROM students stu WHERE stu.class_id = a.class_id) AS total_students
            FROM assignments a
            JOIN subjects s ON a.subject_id = s.id
            JOIN classes c ON a.class_id = c.id
            JOIN faculty f ON a.faculty_id = f.id
            JOIN users u ON f.user_id = u.id
            WHERE a.faculty_id = ?
            ORDER BY a.due_date DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Assignment a = new Assignment();
                    a.setId(rs.getInt("id"));
                    a.setTitle(rs.getString("title"));
                    a.setDescription(rs.getString("description"));
                    a.setSubjectId(rs.getInt("subject_id"));
                    a.setSubjectCode(rs.getString("subject_code"));
                    a.setSubjectName(rs.getString("subject_name"));
                    a.setClassId(rs.getInt("class_id"));
                    a.setClassName(rs.getString("class_name"));
                    a.setFacultyId(rs.getInt("faculty_id"));
                    a.setFacultyName(rs.getString("faculty_name"));
                    a.setDueDate(rs.getDate("due_date"));
                    a.setMaxMarks(rs.getDouble("max_marks"));
                    a.setCreatedAt(rs.getTimestamp("created_at"));
                    a.setTotalSubmissions(rs.getInt("total_submissions"));
                    a.setTotalStudents(rs.getInt("total_students"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching assignments by faculty: " + e.getMessage());
        }
        return list;
    }

    public boolean submitAssignment(int assignmentId, int studentId, String submissionText, String fileName) {
        String sql = """
            INSERT INTO assignment_submissions (assignment_id, student_id, submission_text, file_name, status)
            VALUES (?, ?, ?, ?, 'SUBMITTED')
            ON DUPLICATE KEY UPDATE submission_text = VALUES(submission_text), file_name = VALUES(file_name), submission_date = CURRENT_TIMESTAMP
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ps.setInt(2, studentId);
            ps.setString(3, submissionText);
            ps.setString(4, fileName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error submitting assignment: " + e.getMessage());
            return false;
        }
    }

    public List<AssignmentSubmission> getSubmissionsForAssignment(int assignmentId) {
        List<AssignmentSubmission> list = new ArrayList<>();
        String sql = """
            SELECT sub.*, stu.register_number, stu.roll_number, u.full_name AS student_name,
                   a.title AS assignment_title, a.max_marks
            FROM assignment_submissions sub
            JOIN students stu ON sub.student_id = stu.id
            JOIN users u ON stu.user_id = u.id
            JOIN assignments a ON sub.assignment_id = a.id
            WHERE sub.assignment_id = ?
            ORDER BY CAST(stu.roll_number AS UNSIGNED)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AssignmentSubmission s = new AssignmentSubmission();
                    s.setId(rs.getInt("id"));
                    s.setAssignmentId(rs.getInt("assignment_id"));
                    s.setAssignmentTitle(rs.getString("assignment_title"));
                    s.setStudentId(rs.getInt("student_id"));
                    s.setStudentName(rs.getString("student_name"));
                    s.setRegisterNumber(rs.getString("register_number"));
                    s.setRollNumber(rs.getString("roll_number"));
                    s.setSubmissionDate(rs.getTimestamp("submission_date"));
                    s.setSubmissionText(rs.getString("submission_text"));
                    s.setFileName(rs.getString("file_name"));
                    s.setStatus(AssignmentSubmission.Status.valueOf(rs.getString("status")));
                    if (rs.getObject("marks_obtained") != null) {
                        s.setMarksObtained(rs.getDouble("marks_obtained"));
                    }
                    s.setMaxMarks(rs.getDouble("max_marks"));
                    s.setFacultyFeedback(rs.getString("faculty_feedback"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching submissions: " + e.getMessage());
        }
        return list;
    }

    public boolean gradeSubmission(int submissionId, double marksObtained, String feedback) {
        String sql = "UPDATE assignment_submissions SET marks_obtained = ?, faculty_feedback = ?, status = 'GRADED' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, marksObtained);
            ps.setString(2, feedback);
            ps.setInt(3, submissionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error grading submission: " + e.getMessage());
            return false;
        }
    }
}
