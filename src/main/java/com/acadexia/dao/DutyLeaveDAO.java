package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.AuditLog;
import com.acadexia.model.DutyLeave;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DutyLeaveDAO {

    public boolean applyDutyLeave(DutyLeave dl) {
        String sql = """
            INSERT INTO duty_leaves (student_id, reason, start_date, end_date, total_days, proof_details, status)
            VALUES (?, ?, ?, ?, ?, ?, 'PENDING_ADVISOR')
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dl.getStudentId());
            ps.setString(2, dl.getReason());
            ps.setDate(3, dl.getStartDate());
            ps.setDate(4, dl.getEndDate());
            ps.setInt(5, dl.getTotalDays());
            ps.setString(6, dl.getProofDetails());
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(null, dl.getRegisterNumber(), "STUDENT",
                        "DUTY_LEAVE_APPLIED", "DUTY_LEAVE", String.valueOf(dl.getStudentId()),
                        "Applied for " + dl.getTotalDays() + " days duty leave: " + dl.getReason(), AuditLog.Severity.INFO);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error applying duty leave: " + e.getMessage());
            return false;
        }
    }

    public List<DutyLeave> getLeavesForStudent(int studentId) {
        String sql = """
            SELECT dl.*, stu.register_number, u.full_name AS student_name, c.name AS class_name, d.name AS department_name
            FROM duty_leaves dl
            JOIN students stu ON dl.student_id = stu.id
            JOIN users u ON stu.user_id = u.id
            JOIN classes c ON stu.class_id = c.id
            JOIN departments d ON c.department_id = d.id
            WHERE dl.student_id = ?
            ORDER BY dl.applied_at DESC
            """;
        return queryDutyLeaves(sql, studentId);
    }

    public List<DutyLeave> getPendingLeavesForAdvisor(int classId) {
        String sql = """
            SELECT dl.*, stu.register_number, u.full_name AS student_name, c.name AS class_name, d.name AS department_name
            FROM duty_leaves dl
            JOIN students stu ON dl.student_id = stu.id
            JOIN users u ON stu.user_id = u.id
            JOIN classes c ON stu.class_id = c.id
            JOIN departments d ON c.department_id = d.id
            WHERE stu.class_id = ? AND dl.status = 'PENDING_ADVISOR'
            ORDER BY dl.applied_at ASC
            """;
        return queryDutyLeaves(sql, classId);
    }

    public List<DutyLeave> getPendingLeavesForHod(int departmentId) {
        String sql = """
            SELECT dl.*, stu.register_number, u.full_name AS student_name, c.name AS class_name, d.name AS department_name
            FROM duty_leaves dl
            JOIN students stu ON dl.student_id = stu.id
            JOIN users u ON stu.user_id = u.id
            JOIN classes c ON stu.class_id = c.id
            JOIN departments d ON c.department_id = d.id
            WHERE d.id = ? AND dl.status = 'RECOMMENDED_BY_ADVISOR'
            ORDER BY dl.applied_at ASC
            """;
        return queryDutyLeaves(sql, departmentId);
    }

    public List<DutyLeave> getPendingLeavesForPrincipal() {
        String sql = """
            SELECT dl.*, stu.register_number, u.full_name AS student_name, c.name AS class_name, d.name AS department_name
            FROM duty_leaves dl
            JOIN students stu ON dl.student_id = stu.id
            JOIN users u ON stu.user_id = u.id
            JOIN classes c ON stu.class_id = c.id
            JOIN departments d ON c.department_id = d.id
            WHERE dl.status = 'ENDORSED_BY_HOD'
            ORDER BY dl.applied_at ASC
            """;
        return queryDutyLeaves(sql, null);
    }

    public List<DutyLeave> getAllLeaves() {
        String sql = """
            SELECT dl.*, stu.register_number, u.full_name AS student_name, c.name AS class_name, d.name AS department_name
            FROM duty_leaves dl
            JOIN students stu ON dl.student_id = stu.id
            JOIN users u ON stu.user_id = u.id
            JOIN classes c ON stu.class_id = c.id
            JOIN departments d ON c.department_id = d.id
            ORDER BY dl.applied_at DESC
            """;
        return queryDutyLeaves(sql, null);
    }

    public boolean updateAdvisorDecision(int leaveId, boolean recommend, String remarks, String advisorFacultyId) {
        String status = recommend ? "RECOMMENDED_BY_ADVISOR" : "REJECTED_BY_ADVISOR";
        String sql = "UPDATE duty_leaves SET status = ?, advisor_remarks = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, remarks);
            ps.setInt(3, leaveId);
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(null, advisorFacultyId, "CFA",
                        "DUTY_LEAVE_TIER1_" + (recommend ? "RECOMMENDED" : "REJECTED"),
                        "DUTY_LEAVE", String.valueOf(leaveId), remarks, AuditLog.Severity.INFO);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating advisor decision: " + e.getMessage());
            return false;
        }
    }

    public boolean updateHodDecision(int leaveId, boolean endorse, String remarks, String hodFacultyId) {
        String status = endorse ? "ENDORSED_BY_HOD" : "REJECTED_BY_HOD";
        String sql = "UPDATE duty_leaves SET status = ?, hod_remarks = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, remarks);
            ps.setInt(3, leaveId);
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(null, hodFacultyId, "HOD",
                        "DUTY_LEAVE_TIER2_" + (endorse ? "ENDORSED" : "REJECTED"),
                        "DUTY_LEAVE", String.valueOf(leaveId), remarks, AuditLog.Severity.INFO);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating HOD decision: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePrincipalDecision(int leaveId, boolean approve, String remarks, String principalUsername) {
        String status = approve ? "APPROVED" : "REJECTED";
        String sql = "UPDATE duty_leaves SET status = ?, principal_remarks = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, remarks);
            ps.setInt(3, leaveId);
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                AuditLogDAO.log(null, principalUsername, "PRINCIPAL",
                        "DUTY_LEAVE_TIER3_" + (approve ? "SANCTIONED" : "DENIED"),
                        "DUTY_LEAVE", String.valueOf(leaveId), remarks, AuditLog.Severity.INFO);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating principal decision: " + e.getMessage());
            return false;
        }
    }

    private List<DutyLeave> queryDutyLeaves(String sql, Integer param) {
        List<DutyLeave> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DutyLeave dl = new DutyLeave();
                    dl.setId(rs.getInt("id"));
                    dl.setStudentId(rs.getInt("student_id"));
                    dl.setStudentName(rs.getString("student_name"));
                    dl.setRegisterNumber(rs.getString("register_number"));
                    dl.setClassName(rs.getString("class_name"));
                    dl.setDepartmentName(rs.getString("department_name"));
                    dl.setReason(rs.getString("reason"));
                    dl.setStartDate(rs.getDate("start_date"));
                    dl.setEndDate(rs.getDate("end_date"));
                    dl.setTotalDays(rs.getInt("total_days"));
                    dl.setProofDetails(rs.getString("proof_details"));
                    dl.setStatus(DutyLeave.Status.valueOf(rs.getString("status")));
                    dl.setAdvisorRemarks(rs.getString("advisor_remarks"));
                    dl.setHodRemarks(rs.getString("hod_remarks"));
                    dl.setPrincipalRemarks(rs.getString("principal_remarks"));
                    dl.setAppliedAt(rs.getTimestamp("applied_at"));
                    dl.setUpdatedAt(rs.getTimestamp("updated_at"));
                    list.add(dl);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error querying duty leaves: " + e.getMessage());
        }
        return list;
    }
}
