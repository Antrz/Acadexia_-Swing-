package com.acadexia.ui.student;

import com.acadexia.dao.TimetableDAO;
import com.acadexia.model.TimetableSlot;
import com.acadexia.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modern 7-Period Matrix Timetable UI matching institutional portal standards.
 */
public class StudentTimetablePanel extends JPanel {

    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final int classId;
    private final JPanel gridPanel;

    private static final String[] DAYS = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
    private static final String[] PERIOD_TIMES = {
        "(9:00 - 10:00)",
        "(10:15 - 11:15)",
        "(11:30 - 12:30)",
        "(1:30 - 2:30)",
        "(2:30 - 3:30)",
        "(3:30 - 4:30)",
        "(4:30 - 5:15)"
    };

    public StudentTimetablePanel(int classId) {
        this.classId = classId;
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 18, 16, 18));

        // Top Header section
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Timetable Grid container wrapped in JScrollPane
        gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        loadTimetable();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 4, 12, 4));

        JLabel lblIcon = new JLabel("📅");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel lblTitle = new JLabel("Class Weekly Timetable");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        header.add(lblIcon);
        header.add(lblTitle);
        return header;
    }

    public void loadTimetable() {
        gridPanel.removeAll();

        List<TimetableSlot> slots = timetableDAO.getTimetableForClass(classId);
        // Map slot key "DAY_PERIOD" -> TimetableSlot
        Map<String, TimetableSlot> slotMap = new HashMap<>();
        for (TimetableSlot s : slots) {
            slotMap.put(s.getDayOfWeek().name() + "_" + s.getPeriodSlot(), s);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // 1. Row 0: Column Headers
        // Column 0: "Day"
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gridPanel.add(createHeaderCell("Day", ""), gbc);

        // Columns 1 to 7: Periods 1 to 7
        for (int p = 1; p <= 7; p++) {
            gbc.gridx = p;
            gbc.weightx = 1.0;
            gridPanel.add(createHeaderCell("Period " + p, PERIOD_TIMES[p - 1]), gbc);
        }

        // 2. Rows 1 to 5: Day Rows
        for (int r = 0; r < DAYS.length; r++) {
            String day = DAYS[r];
            gbc.gridy = r + 1;

            // Day header badge in Column 0
            gbc.gridx = 0;
            gbc.weightx = 0.8;
            gridPanel.add(createDayBadge(day), gbc);

            // Periods 1 to 7 in Columns 1 to 7
            for (int p = 1; p <= 7; p++) {
                gbc.gridx = p;
                gbc.weightx = 1.0;
                TimetableSlot slot = slotMap.get(day + "_" + p);
                gridPanel.add(createPeriodCard(slot), gbc);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createHeaderCell(String title, String time) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 30, 48));
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(50, 50, 75), 1, true),
            new EmptyBorder(8, 6, 8, 6)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_BOLD);
        lblTitle.setForeground(new Color(226, 232, 240));
        panel.add(lblTitle, gbc);

        if (!time.isEmpty()) {
            gbc.gridy = 1;
            JLabel lblTime = new JLabel(time, SwingConstants.CENTER);
            lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblTime.setForeground(new Color(148, 163, 184));
            panel.add(lblTime, gbc);
        }

        return panel;
    }

    private JPanel createDayBadge(String dayName) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setMinimumSize(new Dimension(110, 75));
        panel.setPreferredSize(new Dimension(110, 75));

        Color bg, fg, border;
        String labelText;

        switch (dayName.toUpperCase()) {
            case "MONDAY":
                bg = new Color(30, 48, 80);
                fg = new Color(147, 197, 253);
                border = new Color(59, 130, 246);
                labelText = "Monday";
                break;
            case "TUESDAY":
                bg = new Color(20, 55, 40);
                fg = new Color(134, 239, 172);
                border = new Color(34, 197, 94);
                labelText = "Tuesday";
                break;
            case "WEDNESDAY":
                bg = new Color(45, 25, 65);
                fg = new Color(216, 180, 254);
                border = new Color(168, 85, 247);
                labelText = "Wednesday";
                break;
            case "THURSDAY":
                bg = new Color(60, 40, 20);
                fg = new Color(253, 224, 71);
                border = new Color(234, 179, 8);
                labelText = "Thursday";
                break;
            case "FRIDAY":
                bg = new Color(60, 25, 30);
                fg = new Color(252, 165, 165);
                border = new Color(239, 68, 68);
                labelText = "Friday";
                break;
            default:
                bg = new Color(30, 45, 55);
                fg = new Color(125, 211, 252);
                border = new Color(14, 165, 233);
                labelText = dayName;
                break;
        }

        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(border, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(fg);
        panel.add(lbl);

        return panel;
    }

    private JPanel createPeriodCard(TimetableSlot slot) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMinimumSize(new Dimension(135, 75));
        card.setPreferredSize(new Dimension(135, 75));

        if (slot == null || slot.getSubjectCode() == null) {
            card.setBackground(new Color(25, 25, 38));
            card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(40, 40, 60), 1, true),
                new EmptyBorder(10, 8, 10, 8)
            ));
            JLabel lblFree = new JLabel("Free Slot");
            lblFree.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblFree.setForeground(new Color(100, 116, 139));
            lblFree.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(Box.createVerticalGlue());
            card.add(lblFree);
            card.add(Box.createVerticalGlue());
            return card;
        }

        String subCode = slot.getSubjectCode();
        Color bg, border, fgCode, fgName;

        if (subCode.contains("CS501")) {
            bg = new Color(26, 38, 57);
            border = new Color(59, 130, 246, 160);
            fgCode = new Color(147, 197, 253);
            fgName = new Color(226, 232, 240);
        } else if (subCode.contains("MA301")) {
            bg = new Color(40, 26, 54);
            border = new Color(168, 85, 247, 160);
            fgCode = new Color(216, 180, 254);
            fgName = new Color(243, 232, 255);
        } else if (subCode.contains("CS502")) {
            bg = new Color(20, 43, 35);
            border = new Color(34, 197, 94, 160);
            fgCode = new Color(134, 239, 172);
            fgName = new Color(220, 252, 231);
        } else if (subCode.contains("CS503")) {
            bg = new Color(20, 45, 52);
            border = new Color(6, 182, 212, 160);
            fgCode = new Color(103, 232, 249);
            fgName = new Color(207, 250, 254);
        } else {
            bg = new Color(34, 34, 52);
            border = new Color(99, 102, 241, 160);
            fgCode = new Color(199, 210, 254);
            fgName = new Color(241, 245, 249);
        }

        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(border, 1, true),
            new EmptyBorder(8, 8, 8, 8)
        ));

        // Subject Code (Bold Header)
        JLabel lblCode = new JLabel(subCode);
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCode.setForeground(fgCode);
        lblCode.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subject Name
        JLabel lblName = new JLabel(slot.getSubjectName());
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblName.setForeground(fgName);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Room / Instructor (Muted subtext)
        String subtext = slot.getRoomNumber() != null ? slot.getRoomNumber() : "";
        if (slot.getFacultyName() != null && !slot.getFacultyName().isEmpty()) {
            subtext = (subtext.isEmpty() ? "" : subtext + " • ") + slot.getFacultyName();
        }
        JLabel lblSub = new JLabel(subtext);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(148, 163, 184));
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblCode);
        card.add(Box.createVerticalStrut(2));
        card.add(lblName);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);

        return card;
    }
}
