package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.AuditLog;
import com.acadexia.model.Grievance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GrievanceDAO {

    public boolean submitGrievance(Grievance g) {
        String sql = """
            INSERT INTO grievances (student_id, title, category, description, status)
            VALUES (?, ?, ?, ?, 'SUBMITTED')
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, g.getStudentId());
            ps.setString(2, g.getTitle());
            ps.setString(3, g.getCategory().name());
            ps.setString(4, g.getDescription());
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(null, g.getRegisterNumber(), "STUDENT",
                        "GRIEVANCE_SUBMITTED", "GRIEVANCE", g.getTitle(),
                        "Category: " + g.getCategory().name(), AuditLog.Severity.INFO);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error submitting grievance: " + e.getMessage());
            return false;
        }
    }

    public List<Grievance> getGrievancesByStudent(int studentId) {
        String sql = """
            SELECT g.*, stu.register_number, u.full_name AS student_name, c.name AS class_name,
                   res_u.full_name AS resolved_by_user_name
            FROM grievances g
            JOIN students stu ON g.student_id = stu.id
            JOIN users u ON stu.user_id = u.id
            JOIN classes c ON stu.class_id = c.id
            LEFT JOIN users res_u ON g.resolved_by_user_id = res_u.id
            WHERE g.student_id = ?
            ORDER BY g.created_at DESC
            """;
        return queryGrievances(sql, studentId);
    }

    public List<Grievance> getAllGrievances() {
        String sql = """
            SELECT g.*, stu.register_number, u.full_name AS student_name, c.name AS class_name,
                   res_u.full_name AS resolved_by_user_name
            FROM grievances g
            JOIN students stu ON g.student_id = stu.id
            JOIN users u ON stu.user_id = u.id
            JOIN classes c ON stu.class_id = c.id
            LEFT JOIN users res_u ON g.resolved_by_user_id = res_u.id
            ORDER BY g.created_at DESC
            """;
        return queryGrievances(sql, null);
    }

    public boolean updateResolution(int grievanceId, Grievance.Status status, String resolution, int resolvedByUserId, String resolverUsername) {
        String sql = """
            UPDATE grievances 
            SET status = ?, resolution = ?, resolved_by_user_id = ?, resolved_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, resolution);
            ps.setInt(3, resolvedByUserId);
            ps.setInt(4, grievanceId);
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(resolvedByUserId, resolverUsername, "AUTHORITY",
                        "GRIEVANCE_STATUS_UPDATED", "GRIEVANCE", String.valueOf(grievanceId),
                        "New Status: " + status.name() + " | Resolution: " + resolution, AuditLog.Severity.INFO);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating grievance resolution: " + e.getMessage());
            return false;
        }
    }

    private List<Grievance> queryGrievances(String sql, Integer param) {
        List<Grievance> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Grievance g = new Grievance();
                    g.setId(rs.getInt("id"));
                    g.setStudentId(rs.getInt("student_id"));
                    g.setStudentName(rs.getString("student_name"));
                    g.setRegisterNumber(rs.getString("register_number"));
                    g.setClassName(rs.getString("class_name"));
                    g.setTitle(rs.getString("title"));
                    g.setCategory(Grievance.Category.valueOf(rs.getString("category")));
                    g.setDescription(rs.getString("description"));
                    g.setStatus(Grievance.Status.valueOf(rs.getString("status")));
                    g.setResolution(rs.getString("resolution"));
                    int resId = rs.getInt("resolved_by_user_id");
                    if (!rs.wasNull()) g.setResolvedByUserId(resId);
                    g.setResolvedByUserName(rs.getString("resolved_by_user_name"));
                    g.setCreatedAt(rs.getTimestamp("created_at"));
                    g.setResolvedAt(rs.getTimestamp("resolved_at"));
                    list.add(g);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error querying grievances: " + e.getMessage());
        }
        return list;
    }
}
