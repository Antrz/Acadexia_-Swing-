package com.acadexia.dao;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.model.TimetableSlot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {

    public List<TimetableSlot> getTimetableForClass(int classId) {
        String sql = """
            SELECT t.*, c.name AS class_name, u.full_name AS faculty_name,
                   s.code AS subject_code, s.name AS subject_name
            FROM timetable t
            JOIN classes c ON t.class_id = c.id
            JOIN faculty f ON t.faculty_id = f.id
            JOIN users u ON f.user_id = u.id
            JOIN subjects s ON t.subject_id = s.id
            WHERE t.class_id = ?
            ORDER BY FIELD(t.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'), t.period_slot
            """;
        List<TimetableSlot> slots = querySlots(sql, classId);
        if (slots.size() < 35) {
            populateDefault7PeriodTimetable(classId);
            slots = querySlots(sql, classId);
        }
        return slots;
    }

    private void populateDefault7PeriodTimetable(int classId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Retrieve available subjects for this class or default subjects
            int s1 = 1, s2 = 2, s3 = 3, s4 = 4;
            int f1 = 1, f2 = 2, f3 = 3, f4 = 4;

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id FROM subjects ORDER BY id LIMIT 4")) {
                if (rs.next()) s1 = rs.getInt(1);
                if (rs.next()) s2 = rs.getInt(1);
                if (rs.next()) s3 = rs.getInt(1);
                if (rs.next()) s4 = rs.getInt(1);
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id FROM faculty ORDER BY id LIMIT 4")) {
                if (rs.next()) f1 = rs.getInt(1);
                if (rs.next()) f2 = rs.getInt(1);
                if (rs.next()) f3 = rs.getInt(1);
                if (rs.next()) f4 = rs.getInt(1);
            }

            int[][] subMap = {
                {s1, s3, s2, s4, s1, s2, s4}, // Mon
                {s2, s1, s4, s3, s2, s3, s1}, // Tue
                {s3, s4, s1, s2, s4, s1, s3}, // Wed
                {s4, s2, s3, s1, s3, s4, s2}, // Thu
                {s1, s3, s2, s4, s1, s2, s3}  // Fri
            };
            int[][] facMap = {
                {f1, f2, f3, f4, f1, f3, f4},
                {f3, f1, f4, f2, f3, f2, f1},
                {f2, f4, f1, f3, f4, f1, f2},
                {f4, f3, f2, f1, f2, f4, f3},
                {f1, f2, f3, f4, f1, f3, f2}
            };
            String[] rooms = {"LH-301", "LH-301", "LH-301", "LH-301", "LAB-1", "LAB-1", "LH-301"};
            String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};

            for (int d = 0; d < 5; d++) {
                for (int p = 1; p <= 7; p++) {
                    setTimetableSlot(classId, facMap[d][p-1], subMap[d][p-1], TimetableSlot.DayOfWeek.valueOf(days[d]), p, rooms[p-1]);
                }
            }
        } catch (Exception e) {
            System.err.println("Error auto-populating 7-period timetable: " + e.getMessage());
        }
    }

    public List<TimetableSlot> getTimetableForFaculty(int facultyId) {
        String sql = """
            SELECT t.*, c.name AS class_name, u.full_name AS faculty_name,
                   s.code AS subject_code, s.name AS subject_name
            FROM timetable t
            JOIN classes c ON t.class_id = c.id
            JOIN faculty f ON t.faculty_id = f.id
            JOIN users u ON f.user_id = u.id
            JOIN subjects s ON t.subject_id = s.id
            WHERE t.faculty_id = ?
            ORDER BY FIELD(t.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'), t.period_slot
            """;
        return querySlots(sql, facultyId);
    }

    public boolean setTimetableSlot(int classId, int facultyId, int subjectId, TimetableSlot.DayOfWeek day, int period, String room) {
        String sql = """
            INSERT INTO timetable (class_id, faculty_id, subject_id, day_of_week, period_slot, room_number)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                faculty_id = VALUES(faculty_id),
                subject_id = VALUES(subject_id),
                room_number = VALUES(room_number)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, facultyId);
            ps.setInt(3, subjectId);
            ps.setString(4, day.name());
            ps.setInt(5, period);
            ps.setString(6, room);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error setting timetable slot: " + e.getMessage());
            return false;
        }
    }

    private List<TimetableSlot> querySlots(String sql, int param) {
        List<TimetableSlot> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TimetableSlot slot = new TimetableSlot();
                    slot.setId(rs.getInt("id"));
                    slot.setClassId(rs.getInt("class_id"));
                    slot.setClassName(rs.getString("class_name"));
                    slot.setFacultyId(rs.getInt("faculty_id"));
                    slot.setFacultyName(rs.getString("faculty_name"));
                    slot.setSubjectId(rs.getInt("subject_id"));
                    slot.setSubjectCode(rs.getString("subject_code"));
                    slot.setSubjectName(rs.getString("subject_name"));
                    slot.setDayOfWeek(TimetableSlot.DayOfWeek.valueOf(rs.getString("day_of_week")));
                    slot.setPeriodSlot(rs.getInt("period_slot"));
                    slot.setRoomNumber(rs.getString("room_number"));
                    list.add(slot);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error querying timetable slots: " + e.getMessage());
        }
        return list;
    }
}
