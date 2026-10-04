package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public boolean recordAttendance(int studentId, int subjectId, int classId, Date date, int hour, Attendance.Status status, int facultyId, String remarks) {
        String sql = """
            INSERT INTO attendance (student_id, subject_id, class_id, date, hour, status, marked_by_faculty_id, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE status = VALUES(status), marked_by_faculty_id = VALUES(marked_by_faculty_id), remarks = VALUES(remarks)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, classId);
            ps.setDate(4, date);
            ps.setInt(5, hour);
            ps.setString(6, status.name());
            ps.setInt(7, facultyId);
            ps.setString(8, remarks);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording attendance: " + e.getMessage());
            return false;
        }
    }

    public List<StudentAttendanceEntry> getAttendanceEntrySheet(int classId, int subjectId, Date date, int hour) {
        List<StudentAttendanceEntry> list = new ArrayList<>();
        String sql = """
            SELECT s.id AS student_id, s.register_number, s.roll_number, u.full_name,
                   COALESCE(a.status, 'PRESENT') AS current_status,
                   a.remarks
            FROM students s
            JOIN users u ON s.user_id = u.id
            LEFT JOIN attendance a ON s.id = a.student_id AND a.subject_id = ? AND a.date = ? AND a.hour = ?
            WHERE s.class_id = ?
            ORDER BY CAST(s.roll_number AS UNSIGNED), s.register_number
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setDate(2, date);
            ps.setInt(3, hour);
            ps.setInt(4, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentAttendanceEntry entry = new StudentAttendanceEntry(
                            rs.getInt("student_id"),
                            rs.getString("register_number"),
                            rs.getString("roll_number"),
                            rs.getString("full_name"),
                            Attendance.Status.valueOf(rs.getString("current_status")),
                            rs.getString("remarks")
                    );
                    list.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance entry sheet: " + e.getMessage());
        }
        return list;
    }

    public List<SubjectAttendanceSummary> getStudentSubjectWiseAttendance(int studentId) {
        List<SubjectAttendanceSummary> list = new ArrayList<>();
        String sql = """
            SELECT sub.id AS subject_id, sub.code AS subject_code, sub.name AS subject_name,
                   COUNT(a.id) AS total_hours,
                   SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_hours,
                   SUM(CASE WHEN a.status = 'DUTY_LEAVE' THEN 1 ELSE 0 END) AS duty_leave_hours,
                   SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_hours
            FROM subjects sub
            JOIN students s ON s.id = ?
            JOIN classes c ON s.class_id = c.id AND sub.department_id = c.department_id AND sub.semester = c.semester
            LEFT JOIN attendance a ON a.student_id = s.id AND a.subject_id = sub.id
            GROUP BY sub.id, sub.code, sub.name
            ORDER BY sub.code
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int total = rs.getInt("total_hours");
                    int present = rs.getInt("present_hours");
                    int dl = rs.getInt("duty_leave_hours");
                    int absent = rs.getInt("absent_hours");
                    double pct = total > 0 ? ((present + dl) * 100.0 / total) : 100.0;

                    SubjectAttendanceSummary sum = new SubjectAttendanceSummary(
                            rs.getInt("subject_id"),
                            rs.getString("subject_code"),
                            rs.getString("subject_name"),
                            total, present, dl, absent, Math.round(pct * 10.0) / 10.0
                    );
                    list.add(sum);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student attendance summary: " + e.getMessage());
        }
        return list;
    }

    public record StudentAttendanceEntry(int studentId, String registerNumber, String rollNumber,
                                         String fullName, Attendance.Status status, String remarks) {}

    public record SubjectAttendanceSummary(int subjectId, String subjectCode, String subjectName,
                                           int totalHours, int presentHours, int dutyLeaveHours,
                                           int absentHours, double percentage) {}
}
