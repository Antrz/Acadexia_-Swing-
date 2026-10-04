package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public static void log(Integer userId, String username, String role, String action, String entityType, String entityId, String details, AuditLog.Severity severity) {
        String sql = "INSERT INTO audit_logs (user_id, username, role, action, entity_type, entity_id, details, severity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, username != null ? username : "ANONYMOUS");
            ps.setString(3, role != null ? role : "SYSTEM");
            ps.setString(4, action);
            ps.setString(5, entityType);
            ps.setString(6, entityId);
            ps.setString(7, details);
            ps.setString(8, severity.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Audit log error: " + e.getMessage());
        }
    }

    public static void logSecurityAlert(String username, String action, String details) {
        log(null, username, "UNKNOWN", action, "SECURITY", null, details, AuditLog.Severity.SECURITY_ALERT);
    }

    public List<AuditLog> getRecentLogs(int limit, String severityFilter) {
        List<AuditLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_logs ");
        if (severityFilter != null && !severityFilter.equalsIgnoreCase("ALL")) {
            sql.append("WHERE severity = ? ");
        }
        sql.append("ORDER BY timestamp DESC LIMIT ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (severityFilter != null && !severityFilter.equalsIgnoreCase("ALL")) {
                ps.setString(idx++, severityFilter.toUpperCase());
            }
            ps.setInt(idx, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(rs.getInt("id"));
                    int uid = rs.getInt("user_id");
                    if (!rs.wasNull()) log.setUserId(uid);
                    log.setUsername(rs.getString("username"));
                    log.setRole(rs.getString("role"));
                    log.setAction(rs.getString("action"));
                    log.setEntityType(rs.getString("entity_type"));
                    log.setEntityId(rs.getString("entity_id"));
                    log.setDetails(rs.getString("details"));
                    log.setIpAddress(rs.getString("ip_address"));
                    log.setSeverity(AuditLog.Severity.valueOf(rs.getString("severity")));
                    log.setTimestamp(rs.getTimestamp("timestamp"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching audit logs: " + e.getMessage());
        }
        return logs;
    }
}
