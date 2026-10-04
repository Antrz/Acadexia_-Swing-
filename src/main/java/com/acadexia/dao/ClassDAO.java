package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.ClassBatch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassDAO {

    public List<ClassBatch> getAllClasses() {
        List<ClassBatch> list = new ArrayList<>();
        String sql = """
            SELECT c.*, d.name AS department_name, u.full_name AS advisor_name
            FROM classes c
            JOIN departments d ON c.department_id = d.id
            LEFT JOIN faculty f ON c.advisor_faculty_id = f.id
            LEFT JOIN users u ON f.user_id = u.id
            ORDER BY d.name, c.semester, c.section
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToClass(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all classes: " + e.getMessage());
        }
        return list;
    }

    public List<ClassBatch> getClassesByDepartment(int departmentId) {
        List<ClassBatch> list = new ArrayList<>();
        String sql = """
            SELECT c.*, d.name AS department_name, u.full_name AS advisor_name
            FROM classes c
            JOIN departments d ON c.department_id = d.id
            LEFT JOIN faculty f ON c.advisor_faculty_id = f.id
            LEFT JOIN users u ON f.user_id = u.id
            WHERE c.department_id = ?
            ORDER BY c.semester, c.section
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToClass(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching classes by department: " + e.getMessage());
        }
        return list;
    }

    public ClassBatch getById(int classId) {
        String sql = """
            SELECT c.*, d.name AS department_name, u.full_name AS advisor_name
            FROM classes c
            JOIN departments d ON c.department_id = d.id
            LEFT JOIN faculty f ON c.advisor_faculty_id = f.id
            LEFT JOIN users u ON f.user_id = u.id
            WHERE c.id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClass(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching class by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean assignAdvisor(int classId, Integer advisorFacultyId) {
        String sql = "UPDATE classes SET advisor_faculty_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (advisorFacultyId != null) {
                ps.setInt(1, advisorFacultyId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, classId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error assigning advisor: " + e.getMessage());
            return false;
        }
    }

    public int createClass(ClassBatch cb) throws SQLException {
        String sql = """
            INSERT INTO classes (name, department_id, semester, academic_year, section, advisor_faculty_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cb.getName());
            ps.setInt(2, cb.getDepartmentId());
            ps.setInt(3, cb.getSemester());
            ps.setString(4, cb.getAcademicYear());
            ps.setString(5, cb.getSection());
            if (cb.getAdvisorFacultyId() != null) {
                ps.setInt(6, cb.getAdvisorFacultyId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    cb.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    private ClassBatch mapResultSetToClass(ResultSet rs) throws SQLException {
        ClassBatch cb = new ClassBatch();
        cb.setId(rs.getInt("id"));
        cb.setName(rs.getString("name"));
        cb.setDepartmentId(rs.getInt("department_id"));
        cb.setDepartmentName(rs.getString("department_name"));
        cb.setSemester(rs.getInt("semester"));
        cb.setAcademicYear(rs.getString("academic_year"));
        cb.setSection(rs.getString("section"));
        int advId = rs.getInt("advisor_faculty_id");
        if (!rs.wasNull()) {
            cb.setAdvisorFacultyId(advId);
        }
        cb.setAdvisorName(rs.getString("advisor_name"));
        return cb;
    }
}
