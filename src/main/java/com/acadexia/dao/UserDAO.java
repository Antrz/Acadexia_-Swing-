package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.AuditLog;
import com.acadexia.model.Role;
import com.acadexia.model.User;
import com.acadexia.security.SecurityUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * Authenticates a user by matching their institutional identifier
     * (Username, Email, Student Register Number, or Faculty ID) with BCrypt password verification.
     */
    public User authenticate(String identifier, String plainPassword) {
        if (identifier == null || plainPassword == null || identifier.trim().isEmpty() || plainPassword.isEmpty()) {
            return null;
        }

        String trimmedId = identifier.trim();

        // 100% Parameterized query joining users, students, and faculty
        String sql = """
            SELECT u.*, 
                   s.register_number, 
                   f.faculty_id AS emp_faculty_id
            FROM users u
            LEFT JOIN students s ON u.id = s.user_id
            LEFT JOIN faculty f ON u.id = f.user_id
            WHERE u.is_active = TRUE 
              AND (u.username = ? OR u.email = ? OR s.register_number = ? OR f.faculty_id = ?)
            LIMIT 1
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trimmedId);
            ps.setString(2, trimmedId);
            ps.setString(3, trimmedId);
            ps.setString(4, trimmedId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (SecurityUtils.checkPassword(plainPassword, storedHash)) {
                        User user = mapResultSetToUser(rs);
                        AuditLogDAO.log(user.getId(), user.getUsername(), user.getRole().name(),
                                "LOGIN_SUCCESS", "AUTH", String.valueOf(user.getId()),
                                "Successful authentication via identifier: " + trimmedId, AuditLog.Severity.INFO);
                        return user;
                    } else {
                        AuditLogDAO.log(null, trimmedId, "UNKNOWN",
                                "LOGIN_FAILED", "AUTH", null,
                                "Invalid password for identifier: " + trimmedId, AuditLog.Severity.WARNING);
                    }
                } else {
                    AuditLogDAO.log(null, trimmedId, "UNKNOWN",
                            "LOGIN_FAILED", "AUTH", null,
                            "User identifier not found: " + trimmedId, AuditLog.Severity.WARNING);
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication query error: " + e.getMessage());
        }
        return null;
    }

    public User getById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
        }
        return null;
    }

    public int createUser(User user, String plainPassword) throws SQLException {
        String sql = """
            INSERT INTO users (username, password_hash, role, full_name, email, phone, department_id, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, SecurityUtils.hashPassword(plainPassword));
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            if (user.getDepartmentId() != null) {
                ps.setInt(7, user.getDepartmentId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setBoolean(8, user.isActive());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    user.setId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    public boolean updatePassword(int userId, String newPlainPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, SecurityUtils.hashPassword(newPlainPassword));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserStatus(int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isActive);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
        }
        return list;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        int deptId = rs.getInt("department_id");
        if (!rs.wasNull()) {
            u.setDepartmentId(deptId);
        }
        u.setActive(rs.getBoolean("is_active"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
