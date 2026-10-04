package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Mark;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarkDAO {

    public boolean recordMark(int studentId, int subjectId, Mark.ExamType examType, double marksObtained, double maxMarks, Date examDate, int facultyId) {
        String sql = """
            INSERT INTO marks (student_id, subject_id, exam_type, marks_obtained, max_marks, exam_date, recorded_by_faculty_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                marks_obtained = VALUES(marks_obtained),
                max_marks = VALUES(max_marks),
                exam_date = VALUES(exam_date),
                recorded_by_faculty_id = VALUES(recorded_by_faculty_id)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setString(3, examType.name());
            ps.setDouble(4, marksObtained);
            ps.setDouble(5, maxMarks);
            ps.setDate(6, examDate);
            ps.setInt(7, facultyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording marks: " + e.getMessage());
            return false;
        }
    }

    public List<Mark> getMarksForStudent(int studentId) {
        List<Mark> list = new ArrayList<>();
        String sql = """
            SELECT m.*, s.code AS subject_code, s.name AS subject_name,
                   stu.register_number, stu.roll_number, u.full_name AS student_name
            FROM marks m
            JOIN subjects s ON m.subject_id = s.id
            JOIN students stu ON m.student_id = stu.id
            JOIN users u ON stu.user_id = u.id
            WHERE m.student_id = ?
            ORDER BY s.code, m.exam_type
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Mark m = new Mark();
                    m.setId(rs.getInt("id"));
                    m.setStudentId(rs.getInt("student_id"));
                    m.setStudentName(rs.getString("student_name"));
                    m.setRegisterNumber(rs.getString("register_number"));
                    m.setRollNumber(rs.getString("roll_number"));
                    m.setSubjectId(rs.getInt("subject_id"));
                    m.setSubjectCode(rs.getString("subject_code"));
                    m.setSubjectName(rs.getString("subject_name"));
                    m.setExamType(Mark.ExamType.valueOf(rs.getString("exam_type")));
                    m.setMarksObtained(rs.getDouble("marks_obtained"));
                    m.setMaxMarks(rs.getDouble("max_marks"));
                    m.setExamDate(rs.getDate("exam_date"));
                    m.setRecordedByFacultyId(rs.getInt("recorded_by_faculty_id"));
                    m.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching marks for student: " + e.getMessage());
        }
        return list;
    }

    public List<StudentMarksEntryRow> getClassSubjectMarks(int classId, int subjectId, Mark.ExamType examType) {
        List<StudentMarksEntryRow> list = new ArrayList<>();
        String sql = """
            SELECT stu.id AS student_id, stu.register_number, stu.roll_number, u.full_name,
                   m.marks_obtained, m.max_marks
            FROM students stu
            JOIN users u ON stu.user_id = u.id
            LEFT JOIN marks m ON stu.id = m.student_id AND m.subject_id = ? AND m.exam_type = ?
            WHERE stu.class_id = ?
            ORDER BY CAST(stu.roll_number AS UNSIGNED), stu.register_number
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setString(2, examType.name());
            ps.setInt(3, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Double obtained = rs.getObject("marks_obtained") != null ? rs.getDouble("marks_obtained") : null;
                    Double max = rs.getObject("max_marks") != null ? rs.getDouble("max_marks") : 50.0;
                    list.add(new StudentMarksEntryRow(
                            rs.getInt("student_id"),
                            rs.getString("register_number"),
                            rs.getString("roll_number"),
                            rs.getString("full_name"),
                            obtained,
                            max
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching class marks entry: " + e.getMessage());
        }
        return list;
    }

    public record StudentMarksEntryRow(int studentId, String registerNumber, String rollNumber,
                                       String fullName, Double marksObtained, Double maxMarks) {}
}
