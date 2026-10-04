package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.Feedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {

    public boolean submitFeedback(Feedback fb) {
        String sql = """
            INSERT INTO feedback (student_id, faculty_id, subject_id, rating_teaching, rating_punctuality, rating_clarity, comments)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                rating_teaching = VALUES(rating_teaching),
                rating_punctuality = VALUES(rating_punctuality),
                rating_clarity = VALUES(rating_clarity),
                comments = VALUES(comments),
                created_at = CURRENT_TIMESTAMP
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fb.getStudentId());
            ps.setInt(2, fb.getFacultyId());
            ps.setInt(3, fb.getSubjectId());
            ps.setInt(4, fb.getRatingTeaching());
            ps.setInt(5, fb.getRatingPunctuality());
            ps.setInt(6, fb.getRatingClarity());
            ps.setString(7, fb.getComments());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error submitting feedback: " + e.getMessage());
            return false;
        }
    }

    public List<Feedback> getFeedbackForFaculty(int facultyId) {
        List<Feedback> list = new ArrayList<>();
        String sql = """
            SELECT fb.*, s.code AS subject_code, s.name AS subject_name, u.full_name AS faculty_name
            FROM feedback fb
            JOIN subjects s ON fb.subject_id = s.id
            JOIN faculty f ON fb.faculty_id = f.id
            JOIN users u ON f.user_id = u.id
            WHERE fb.faculty_id = ?
            ORDER BY fb.created_at DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Feedback fb = new Feedback();
                    fb.setId(rs.getInt("id"));
                    // Anonymized: do not expose studentId in UI
                    fb.setFacultyId(rs.getInt("faculty_id"));
                    fb.setFacultyName(rs.getString("faculty_name"));
                    fb.setSubjectId(rs.getInt("subject_id"));
                    fb.setSubjectCode(rs.getString("subject_code"));
                    fb.setSubjectName(rs.getString("subject_name"));
                    fb.setRatingTeaching(rs.getInt("rating_teaching"));
                    fb.setRatingPunctuality(rs.getInt("rating_punctuality"));
                    fb.setRatingClarity(rs.getInt("rating_clarity"));
                    fb.setComments(rs.getString("comments"));
                    fb.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(fb);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching faculty feedback: " + e.getMessage());
        }
        return list;
    }
}
