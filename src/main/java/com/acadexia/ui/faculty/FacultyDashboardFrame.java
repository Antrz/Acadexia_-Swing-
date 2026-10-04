package com.acadexia.ui.faculty;

import com.acadexia.dao.*;
import com.acadexia.model.*;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.auth.LandingFrame;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class FacultyDashboardFrame extends JFrame {

    private final Faculty currentFaculty;
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MarkDAO markDAO = new MarkDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final DutyLeaveDAO dutyLeaveDAO = new DutyLeaveDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();

    private JComboBox<SubjectDAO.AssignedCourse> cmbCourses;
    private DefaultTableModel attendanceTableModel;
    private DefaultTableModel marksTableModel;
    private DefaultTableModel assignTableModel;
    private DefaultTableModel feedbackTableModel;
    private DefaultTableModel cfaStudentTableModel;
    private DefaultTableModel cfaLeaveTableModel;
    private DefaultTableModel hodLeaveTableModel;

    private List<SubjectDAO.AssignedCourse> assignedCourses;
    private List<DutyLeave> cfaPendingLeaves;
    private List<DutyLeave> hodPendingLeaves;

    public FacultyDashboardFrame() {
        this.currentFaculty = SessionManager.getInstance().getCurrentFaculty();
        setTitle("Acadexia - Faculty Academic & Governance Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1160, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Top Navigation Header
        add(createTopBar(), BorderLayout.NORTH);

        // Center Dynamic Multi-Role Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);

        // 1. My Teaching (Universal Subject Faculty Portal)
        tabs.addTab("📚 My Teaching (Course Portal)", createTeachingPanel());

        // 2. Class Advisor (CFA) Tab (Unlocked if CFA)
        if (currentFaculty != null && currentFaculty.isCfa()) {
            tabs.addTab("📋 Class Advisor (CFA)", createCfaPanel());
        }

        // 3. HOD Portal Tab (Unlocked if HOD)
        if (currentFaculty != null && currentFaculty.isHod()) {
            tabs.addTab("🏛️ HOD Portal (" + currentFaculty.getDepartmentName() + ")", createHodPanel());
        }

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(18, 18, 28));
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        String facName = currentFaculty != null ? currentFaculty.getFullName() : "Faculty";
        String facCode = currentFaculty != null ? currentFaculty.getFacultyId() : "FAC";
        JLabel lblTitle = new JLabel(facName.toUpperCase());
        lblTitle.setFont(UITheme.FONT_SECTION);
        lblTitle.setForeground(UITheme.PRIMARY);

        left.add(lblTitle);
        left.add(UIComponents.createBadge(facCode, new Color(49, 46, 129), Color.WHITE));
        left.add(UIComponents.createBadge("Subject Teacher", new Color(30, 41, 59), UITheme.TEXT_MUTED));

        if (currentFaculty != null && currentFaculty.isCfa()) {
            left.add(UIComponents.createBadge("CFA: " + currentFaculty.getAdvisedClassName(), new Color(6, 78, 59), UITheme.SUCCESS));
        }
        if (currentFaculty != null && currentFaculty.isHod()) {
            left.add(UIComponents.createBadge("HOD (" + currentFaculty.getDepartmentName() + ")", new Color(120, 53, 15), UITheme.WARNING));
        }

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnLogout = UIComponents.createDangerButton("Log Out");
        btnLogout.addActionListener(e -> {
            SessionManager.getInstance().logout();
            new LandingFrame().setVisible(true);
            dispose();
        });

        right.add(btnLogout);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    // 1. MY TEACHING TAB (CROSS-DEPARTMENTAL SUBJECT FACULTY)
    // =========================================================================
    private JPanel createTeachingPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Course Selector Header
        JPanel selectorBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        selectorBar.setOpaque(false);

        JLabel lblSelect = new JLabel("Active Assigned Course:");
        lblSelect.setFont(UITheme.FONT_BOLD);
        lblSelect.setForeground(UITheme.TEXT_PRIMARY);

        int facId = currentFaculty != null ? currentFaculty.getId() : 1;
        assignedCourses = subjectDAO.getAssignedCoursesForFaculty(facId);

        cmbCourses = new JComboBox<>();
        for (SubjectDAO.AssignedCourse ac : assignedCourses) {
            cmbCourses.addItem(ac);
        }
        cmbCourses.addActionListener(e -> refreshCourseData());

        selectorBar.add(lblSelect);
        selectorBar.add(cmbCourses);

        // Sub Tabs for Teaching
        JTabbedPane subTabs = new JTabbedPane();
        subTabs.setFont(UITheme.FONT_REGULAR);
        subTabs.addTab("📝 Attendance Register", createAttendanceRegisterSubPanel());
        subTabs.addTab("📊 Internal Marks Entry", createMarksEntrySubPanel());
        subTabs.addTab("📁 Assignments", createAssignmentsSubPanel());
        subTabs.addTab("💬 Anonymous Student Feedback", createFeedbackSubPanel());
        subTabs.addTab("🕒 Teaching Timetable", createFacultyTimetableSubPanel());

        panel.add(selectorBar, BorderLayout.NORTH);
        panel.add(subTabs, BorderLayout.CENTER);

        refreshCourseData();
        return panel;
    }

    private JPanel createAttendanceRegisterSubPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JTextField txtDate = new JTextField(LocalDate.now().toString(), 10);
        JComboBox<Integer> cmbHour = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7});

        JButton btnLoad = UIComponents.createSecondaryButton("Load Register");
        btnLoad.addActionListener(e -> loadAttendanceRegister(txtDate.getText().trim(), (Integer) cmbHour.getSelectedItem()));

        JButton btnMarkAllPresent = UIComponents.createSecondaryButton("Mark All Present");
        btnMarkAllPresent.addActionListener(e -> {
            for (int r = 0; r < attendanceTableModel.getRowCount(); r++) {
                attendanceTableModel.setValueAt("PRESENT", r, 3);
            }
        });

        JButton btnSave = UIComponents.createSuccessButton("💾 Save Attendance");
        btnSave.addActionListener(e -> saveAttendance(txtDate.getText().trim(), (Integer) cmbHour.getSelectedItem()));

        tool.add(new JLabel("Date (YYYY-MM-DD):"));
        tool.add(txtDate);
        tool.add(new JLabel("Hour Slot:"));
        tool.add(cmbHour);
        tool.add(btnLoad);
        tool.add(btnMarkAllPresent);
        tool.add(btnSave);

        String[] cols = {"Student ID", "Roll", "Register No", "Student Name", "Status (PRESENT/ABSENT/DUTY_LEAVE)", "Remarks"};
        attendanceTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 4 || col == 5; // Allow editing status and remarks
            }
        };
        JTable table = UIComponents.createStyledTable(attendanceTableModel);

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadAttendanceRegister(String dateStr, int hour) {
        attendanceTableModel.setRowCount(0);
        SubjectDAO.AssignedCourse sel = (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem();
        if (sel == null) return;

        try {
            Date d = Date.valueOf(dateStr);
            List<AttendanceDAO.StudentAttendanceEntry> list = attendanceDAO.getAttendanceEntrySheet(sel.classId(), sel.subjectId(), d, hour);
            for (AttendanceDAO.StudentAttendanceEntry se : list) {
                attendanceTableModel.addRow(new Object[]{
                        se.studentId(),
                        se.rollNumber(),
                        se.registerNumber(),
                        se.fullName(),
                        se.status().name(),
                        se.remarks() != null ? se.remarks() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date format (use YYYY-MM-DD).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAttendance(String dateStr, int hour) {
        SubjectDAO.AssignedCourse sel = (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem();
        if (sel == null) return;

        try {
            Date d = Date.valueOf(dateStr);
            int facId = currentFaculty != null ? currentFaculty.getId() : 1;
            int count = 0;
            for (int r = 0; r < attendanceTableModel.getRowCount(); r++) {
                int stuId = (int) attendanceTableModel.getValueAt(r, 0);
                String statStr = attendanceTableModel.getValueAt(r, 4).toString().toUpperCase().trim();
                Attendance.Status status = Attendance.Status.valueOf(statStr);
                String rem = attendanceTableModel.getValueAt(r, 5) != null ? attendanceTableModel.getValueAt(r, 5).toString() : "";
                if (attendanceDAO.recordAttendance(stuId, sel.subjectId(), sel.classId(), d, hour, status, facId, rem)) {
                    count++;
                }
            }
            JOptionPane.showMessageDialog(this, "Attendance recorded successfully for " + count + " students.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createMarksEntrySubPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JComboBox<Mark.ExamType> cmbExam = new JComboBox<>(Mark.ExamType.values());

        JButton btnLoad = UIComponents.createSecondaryButton("Load Marksheet");
        btnLoad.addActionListener(e -> loadMarksEntry((Mark.ExamType) cmbExam.getSelectedItem()));

        JButton btnSave = UIComponents.createSuccessButton("💾 Save Marks");
        btnSave.addActionListener(e -> saveMarks((Mark.ExamType) cmbExam.getSelectedItem()));

        tool.add(new JLabel("Evaluation Type:"));
        tool.add(cmbExam);
        tool.add(btnLoad);
        tool.add(btnSave);

        String[] cols = {"Student ID", "Roll", "Register No", "Student Name", "Marks Obtained", "Max Marks"};
        marksTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 4; // Allow entering marks
            }
        };
        JTable table = UIComponents.createStyledTable(marksTableModel);

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadMarksEntry(Mark.ExamType examType) {
        marksTableModel.setRowCount(0);
        SubjectDAO.AssignedCourse sel = (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem();
        if (sel == null) return;

        List<MarkDAO.StudentMarksEntryRow> rows = markDAO.getClassSubjectMarks(sel.classId(), sel.subjectId(), examType);
        for (MarkDAO.StudentMarksEntryRow r : rows) {
            marksTableModel.addRow(new Object[]{
                    r.studentId(),
                    r.rollNumber(),
                    r.registerNumber(),
                    r.fullName(),
                    r.marksObtained() != null ? r.marksObtained() : 0.0,
                    r.maxMarks()
            });
        }
    }

    private void saveMarks(Mark.ExamType examType) {
        SubjectDAO.AssignedCourse sel = (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem();
        if (sel == null) return;

        int facId = currentFaculty != null ? currentFaculty.getId() : 1;
        int count = 0;
        for (int r = 0; r < marksTableModel.getRowCount(); r++) {
            int stuId = (int) marksTableModel.getValueAt(r, 0);
            double marks = Double.parseDouble(marksTableModel.getValueAt(r, 4).toString());
            double max = Double.parseDouble(marksTableModel.getValueAt(r, 5).toString());
            if (markDAO.recordMark(stuId, sel.subjectId(), examType, marks, max, Date.valueOf(LocalDate.now()), facId)) {
                count++;
            }
        }
        JOptionPane.showMessageDialog(this, "Saved marks for " + count + " students.");
    }

    private JPanel createAssignmentsSubPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JButton btnNewAssign = UIComponents.createPrimaryButton("➕ Post New Assignment");
        btnNewAssign.addActionListener(e -> showPostAssignmentDialog());

        JButton btnViewSub = UIComponents.createSecondaryButton("View & Grade Submissions");
        btnViewSub.addActionListener(e -> showGradeSubmissionsDialog());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadAssignments());

        tool.add(btnNewAssign);
        tool.add(btnViewSub);
        tool.add(btnRefresh);

        String[] cols = {"Assignment ID", "Title", "Due Date", "Max Marks", "Submissions", "Class"};
        assignTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(assignTableModel);

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadAssignments() {
        assignTableModel.setRowCount(0);
        int facId = currentFaculty != null ? currentFaculty.getId() : 1;
        List<Assignment> list = assignmentDAO.getAssignmentsByFaculty(facId);
        for (Assignment a : list) {
            assignTableModel.addRow(new Object[]{
                    a.getId(),
                    a.getTitle(),
                    a.getDueDate(),
                    a.getMaxMarks(),
                    a.getTotalSubmissions() + " / " + a.getTotalStudents(),
                    a.getClassName()
            });
        }
    }

    private void showPostAssignmentDialog() {
        SubjectDAO.AssignedCourse sel = (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem();
        if (sel == null) return;

        JTextField txtTitle = new JTextField();
        JTextArea txtDesc = new JTextArea(4, 20);
        JTextField txtDue = new JTextField(LocalDate.now().plusDays(7).toString());
        JTextField txtMax = new JTextField("10.0");

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Title:"));
        p.add(txtTitle);
        p.add(new JLabel("Description:"));
        p.add(new JScrollPane(txtDesc));
        p.add(new JLabel("Due Date (YYYY-MM-DD):"));
        p.add(txtDue);
        p.add(new JLabel("Max Marks:"));
        p.add(txtMax);

        int opt = JOptionPane.showConfirmDialog(this, p, "Post New Assignment", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            Assignment a = new Assignment();
            a.setTitle(txtTitle.getText().trim());
            a.setDescription(txtDesc.getText().trim());
            a.setSubjectId(sel.subjectId());
            a.setClassId(sel.classId());
            a.setFacultyId(currentFaculty != null ? currentFaculty.getId() : 1);
            a.setDueDate(Date.valueOf(txtDue.getText().trim()));
            a.setMaxMarks(Double.parseDouble(txtMax.getText().trim()));

            if (assignmentDAO.createAssignment(a)) {
                loadAssignments();
                JOptionPane.showMessageDialog(this, "Assignment posted successfully.");
            }
        }
    }

    private void showGradeSubmissionsDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter Assignment ID to view submissions:", "Grade Submissions", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int aid = Integer.parseInt(input.trim());
            List<AssignmentSubmission> subs = assignmentDAO.getSubmissionsForAssignment(aid);
            if (subs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No student submissions found for this assignment.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JDialog dlg = new JDialog(this, "Student Submissions - Assignment #" + aid, true);
            dlg.setSize(800, 480);
            dlg.setLocationRelativeTo(this);

            String[] cols = {"Sub ID", "Roll", "Register No", "Student Name", "Status", "Marks", "Text / Notes"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            for (AssignmentSubmission s : subs) {
                model.addRow(new Object[]{
                        s.getId(), s.getRollNumber(), s.getRegisterNumber(), s.getStudentName(),
                        s.getStatus().name(), s.getMarksObtained() != null ? s.getMarksObtained() : "Ungraded",
                        s.getSubmissionText()
                });
            }

            JTable tbl = UIComponents.createStyledTable(model);
            JButton btnGrade = UIComponents.createPrimaryButton("Award Marks to Selected");
            btnGrade.addActionListener(e -> {
                int row = tbl.getSelectedRow();
                if (row >= 0) {
                    int subId = (int) model.getValueAt(row, 0);
                    String mStr = JOptionPane.showInputDialog(dlg, "Enter Marks Awarded:");
                    String feedback = JOptionPane.showInputDialog(dlg, "Enter Faculty Feedback / Remarks:");
                    if (mStr != null) {
                        assignmentDAO.gradeSubmission(subId, Double.parseDouble(mStr.trim()), feedback != null ? feedback : "Good");
                        model.setValueAt(Double.parseDouble(mStr.trim()), row, 5);
                        model.setValueAt("GRADED", row, 4);
                    }
                }
            });

            dlg.setLayout(new BorderLayout());
            dlg.add(new JScrollPane(tbl), BorderLayout.CENTER);
            dlg.add(btnGrade, BorderLayout.SOUTH);
            dlg.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid Assignment ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createFeedbackSubPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Subject", "Teaching Rating (1-5)", "Punctuality (1-5)", "Clarity (1-5)", "Average", "Anonymous Comments"};
        feedbackTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(feedbackTableModel);

        JButton btnRef = UIComponents.createSecondaryButton("🔄 Refresh Feedback");
        btnRef.addActionListener(e -> loadFeedback());

        p.add(btnRef, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadFeedback() {
        feedbackTableModel.setRowCount(0);
        int facId = currentFaculty != null ? currentFaculty.getId() : 1;
        List<Feedback> list = feedbackDAO.getFeedbackForFaculty(facId);
        for (Feedback fb : list) {
            feedbackTableModel.addRow(new Object[]{
                    fb.getSubjectCode() + " - " + fb.getSubjectName(),
                    fb.getRatingTeaching() + " ★",
                    fb.getRatingPunctuality() + " ★",
                    fb.getRatingClarity() + " ★",
                    String.format("%.1f ★", fb.getAverageRating()),
                    fb.getComments()
            });
        }
    }

    private JPanel createFacultyTimetableSubPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Day", "Period", "Subject Code", "Subject Name", "Class / Section", "Room"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        int facId = currentFaculty != null ? currentFaculty.getId() : 1;
        List<TimetableSlot> slots = timetableDAO.getTimetableForFaculty(facId);
        for (TimetableSlot s : slots) {
            model.addRow(new Object[]{
                    s.getDayOfWeek().getLabel(),
                    "Period " + s.getPeriodSlot(),
                    s.getSubjectCode(),
                    s.getSubjectName(),
                    s.getClassName(),
                    s.getRoomNumber()
            });
        }

        JTable table = UIComponents.createStyledTable(model);
        p.add(new JLabel("Personal Teaching Schedule (Across All Departments)"), BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void refreshCourseData() {
        SubjectDAO.AssignedCourse sel = (SubjectDAO.AssignedCourse) cmbCourses.getSelectedItem();
        if (sel != null) {
            loadAttendanceRegister(LocalDate.now().toString(), 1);
            loadMarksEntry(Mark.ExamType.SERIES_1);
            loadAssignments();
            loadFeedback();
        }
    }

    // =========================================================================
    // 2. CLASS ADVISOR (CFA) TAB
    // =========================================================================
    private JPanel createCfaPanel() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Class Faculty Advisor (CFA) - Advised Class: " + currentFaculty.getAdvisedClassName());
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JTabbedPane cfaTabs = new JTabbedPane();
        cfaTabs.addTab("👥 Advised Class Students", createCfaStudentDirectoryPanel());
        cfaTabs.addTab("📝 Tier-1 Duty Leave Recommendations", createCfaDutyLeavePanel());

        p.add(lblTitle, BorderLayout.NORTH);
        p.add(cfaTabs, BorderLayout.CENTER);
        return p;
    }

    private JPanel createCfaStudentDirectoryPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);

        String[] cols = {"Roll", "Register No", "Student Name", "Attendance %", "SGPA", "CGPA", "Phone", "Guardian"};
        cfaStudentTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(cfaStudentTableModel);

        if (currentFaculty.getAdvisedClassId() != null) {
            List<Student> students = studentDAO.getStudentsByClass(currentFaculty.getAdvisedClassId());
            for (Student s : students) {
                cfaStudentTableModel.addRow(new Object[]{
                        s.getRollNumber(),
                        s.getRegisterNumber(),
                        s.getFullName(),
                        s.getAttendancePercentage() + "%",
                        s.getCurrentSgpa(),
                        s.getCurrentCgpa(),
                        s.getPhone(),
                        s.getGuardianName()
                });
            }
        }

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel createCfaDutyLeavePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JButton btnRecommend = UIComponents.createSuccessButton("👍 Recommend to HOD");
        btnRecommend.addActionListener(e -> processCfaDutyLeave(true));

        JButton btnReject = UIComponents.createDangerButton("❌ Reject Application");
        btnReject.addActionListener(e -> processCfaDutyLeave(false));

        JButton btnRef = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRef.addActionListener(e -> loadCfaDutyLeaves());

        tool.add(btnRecommend);
        tool.add(btnReject);
        tool.add(btnRef);

        String[] cols = {"App ID", "Register No", "Student Name", "Reason", "Days", "Dates", "Proof"};
        cfaLeaveTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(cfaLeaveTableModel);

        loadCfaDutyLeaves();

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadCfaDutyLeaves() {
        cfaLeaveTableModel.setRowCount(0);
        if (currentFaculty.getAdvisedClassId() != null) {
            cfaPendingLeaves = dutyLeaveDAO.getPendingLeavesForAdvisor(currentFaculty.getAdvisedClassId());
            for (DutyLeave dl : cfaPendingLeaves) {
                cfaLeaveTableModel.addRow(new Object[]{
                        dl.getId(), dl.getRegisterNumber(), dl.getStudentName(),
                        dl.getReason(), dl.getTotalDays(), dl.getStartDate() + " to " + dl.getEndDate(),
                        dl.getProofDetails()
                });
            }
        }
    }

    private void processCfaDutyLeave(boolean recommend) {
        String input = JOptionPane.showInputDialog(this, "Enter Application ID to " + (recommend ? "RECOMMEND" : "REJECT") + ":");
        if (input == null || input.trim().isEmpty()) return;

        try {
            int appId = Integer.parseInt(input.trim());
            String rem = JOptionPane.showInputDialog(this, "Enter Advisor Remarks:", recommend ? "Verified and Recommended" : "Rejected");
            if (rem == null) rem = recommend ? "Recommended" : "Rejected";

            dutyLeaveDAO.updateAdvisorDecision(appId, recommend, rem, currentFaculty.getFacultyId());
            loadCfaDutyLeaves();
            JOptionPane.showMessageDialog(this, "Application " + (recommend ? "recommended to HOD." : "rejected."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Application ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // 3. HOD PORTAL TAB
    // =========================================================================
    private JPanel createHodPanel() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("HOD Governance Portal - " + currentFaculty.getDepartmentName());
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JTabbedPane hodTabs = new JTabbedPane();
        hodTabs.addTab("📋 Tier-2 Duty Leave Endorsement", createHodDutyLeavePanel());
        hodTabs.addTab("👔 Assign Class Advisor (CFA)", createHodAssignCfaPanel());
        hodTabs.addTab("📖 Assign Subject Teachers (Cross-Dept)", createHodAssignTeacherPanel());

        p.add(lblTitle, BorderLayout.NORTH);
        p.add(hodTabs, BorderLayout.CENTER);
        return p;
    }

    private JPanel createHodDutyLeavePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JButton btnEndorse = UIComponents.createSuccessButton("✍️ Endorse to Principal");
        btnEndorse.addActionListener(e -> processHodDutyLeave(true));

        JButton btnReject = UIComponents.createDangerButton("❌ Reject Application");
        btnReject.addActionListener(e -> processHodDutyLeave(false));

        JButton btnRef = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRef.addActionListener(e -> loadHodDutyLeaves());

        tool.add(btnEndorse);
        tool.add(btnReject);
        tool.add(btnRef);

        String[] cols = {"App ID", "Register No", "Student Name", "Class", "Reason", "Days", "CFA Remarks"};
        hodLeaveTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(hodLeaveTableModel);

        loadHodDutyLeaves();

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadHodDutyLeaves() {
        hodLeaveTableModel.setRowCount(0);
        hodPendingLeaves = dutyLeaveDAO.getPendingLeavesForHod(currentFaculty.getDepartmentId());
        for (DutyLeave dl : hodPendingLeaves) {
            hodLeaveTableModel.addRow(new Object[]{
                    dl.getId(), dl.getRegisterNumber(), dl.getStudentName(),
                    dl.getClassName(), dl.getReason(), dl.getTotalDays(),
                    dl.getAdvisorRemarks()
            });
        }
    }

    private void processHodDutyLeave(boolean endorse) {
        String input = JOptionPane.showInputDialog(this, "Enter Application ID to " + (endorse ? "ENDORSE" : "REJECT") + ":");
        if (input == null || input.trim().isEmpty()) return;

        try {
            int appId = Integer.parseInt(input.trim());
            String rem = JOptionPane.showInputDialog(this, "Enter HOD Remarks:", endorse ? "Department Endorsed" : "Rejected by HOD");
            if (rem == null) rem = endorse ? "Endorsed" : "Rejected";

            dutyLeaveDAO.updateHodDecision(appId, endorse, rem, currentFaculty.getFacultyId());
            loadHodDutyLeaves();
            JOptionPane.showMessageDialog(this, "Application " + (endorse ? "forwarded to Principal." : "rejected."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Application ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createHodAssignCfaPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        List<ClassBatch> classes = classDAO.getClassesByDepartment(currentFaculty.getDepartmentId());
        List<Faculty> facList = facultyDAO.getFacultyByDepartment(currentFaculty.getDepartmentId());

        JComboBox<ClassBatch> cmbCls = new JComboBox<>(classes.toArray(new ClassBatch[0]));
        JComboBox<Faculty> cmbFac = new JComboBox<>(facList.toArray(new Faculty[0]));

        JButton btnAssign = UIComponents.createPrimaryButton("Assign as Class Faculty Advisor (CFA)");
        btnAssign.addActionListener(e -> {
            ClassBatch cb = (ClassBatch) cmbCls.getSelectedItem();
            Faculty f = (Faculty) cmbFac.getSelectedItem();
            if (cb != null && f != null) {
                classDAO.assignAdvisor(cb.getId(), f.getId());
                JOptionPane.showMessageDialog(this, "Assigned " + f.getFullName() + " as CFA for " + cb.getName());
            }
        });

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.add(new JLabel("Select Department Class:"));
        form.add(cmbCls);
        form.add(new JLabel("Select Department Faculty:"));
        form.add(cmbFac);
        form.add(new JLabel(""));
        form.add(btnAssign);

        p.add(form, BorderLayout.NORTH);
        return p;
    }

    private JPanel createHodAssignTeacherPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        List<ClassBatch> classes = classDAO.getClassesByDepartment(currentFaculty.getDepartmentId());
        List<Subject> subjects = subjectDAO.getAllSubjects();
        // Support cross-departmental faculty: lists all faculty across the entire college!
        List<Faculty> allCollegeFaculty = facultyDAO.getAllFaculty();

        JComboBox<ClassBatch> cmbCls = new JComboBox<>(classes.toArray(new ClassBatch[0]));
        JComboBox<Subject> cmbSub = new JComboBox<>(subjects.toArray(new Subject[0]));
        JComboBox<Faculty> cmbFac = new JComboBox<>(allCollegeFaculty.toArray(new Faculty[0]));

        JButton btnAssign = UIComponents.createPrimaryButton("Assign Teacher to Subject");
        btnAssign.addActionListener(e -> {
            ClassBatch cb = (ClassBatch) cmbCls.getSelectedItem();
            Subject s = (Subject) cmbSub.getSelectedItem();
            Faculty f = (Faculty) cmbFac.getSelectedItem();
            if (cb != null && s != null && f != null) {
                subjectDAO.assignTeacherToSubject(s.getId(), f.getId(), cb.getId(), "2024-2025");
                JOptionPane.showMessageDialog(this, "Assigned " + f.getFullName() + " (" + f.getDepartmentName() + ") to teach " + s.getName() + " in " + cb.getName());
            }
        });

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.add(new JLabel("Class Batch:"));
        form.add(cmbCls);
        form.add(new JLabel("Subject:"));
        form.add(cmbSub);
        form.add(new JLabel("Faculty (Any Department - Cross-Dept Supported):"));
        form.add(cmbFac);
        form.add(new JLabel(""));
        form.add(btnAssign);

        p.add(form, BorderLayout.NORTH);
        return p;
    }
}
