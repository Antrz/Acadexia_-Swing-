package com.acadexia.ui.student;

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

public class StudentDashboardFrame extends JFrame {

    private final Student currentStudent;
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MarkDAO markDAO = new MarkDAO();
    private final DutyLeaveDAO dutyLeaveDAO = new DutyLeaveDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final GrievanceDAO grievanceDAO = new GrievanceDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    private DefaultTableModel marksTableModel;
    private DefaultTableModel attTableModel;
    private DefaultTableModel leaveTableModel;
    private DefaultTableModel assignTableModel;
    private DefaultTableModel grievTableModel;

    public StudentDashboardFrame() {
        this.currentStudent = SessionManager.getInstance().getCurrentStudent();
        setTitle("Acadexia - Student Academic Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1140, 740);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Top Navigation Bar
        add(createTopBar(), BorderLayout.NORTH);

        // Center Content: KPI Summary + Tabs
        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(16, 20, 16, 20));

        center.add(createKpiBanner(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);

        tabs.addTab("📊 Results & Marksheet", createMarksheetPanel());
        tabs.addTab("🕒 Attendance Tracker", createAttendancePanel());
        tabs.addTab("✈️ Duty Leave Pipeline", createDutyLeavePanel());
        tabs.addTab("📅 Class Timetable", createTimetablePanel());
        tabs.addTab("📁 Assignments", createAssignmentsPanel());
        tabs.addTab("⚖️ Grievance Redressal", createGrievancePanel());
        tabs.addTab("💬 Faculty Feedback", createFeedbackPanel());

        center.add(tabs, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        loadAllData();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(18, 18, 28));
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        String sName = currentStudent != null ? currentStudent.getFullName() : "Student";
        String sReg = currentStudent != null ? currentStudent.getRegisterNumber() : "REG";
        String sClass = currentStudent != null ? currentStudent.getClassName() : "Class";

        JLabel lblTitle = new JLabel(sName.toUpperCase());
        lblTitle.setFont(UITheme.FONT_SECTION);
        lblTitle.setForeground(UITheme.PRIMARY);

        left.add(lblTitle);
        left.add(UIComponents.createBadge(sReg, new Color(49, 46, 129), Color.WHITE));
        left.add(UIComponents.createBadge(sClass, new Color(30, 41, 59), UITheme.TEXT_MUTED));

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

    private JPanel createKpiBanner() {
        JPanel kpi = new JPanel(new GridLayout(1, 4, 14, 14));
        kpi.setOpaque(false);

        double attPct = currentStudent != null ? currentStudent.getAttendancePercentage() : 100.0;
        double sgpa = currentStudent != null ? currentStudent.getCurrentSgpa() : 0.0;
        double cgpa = currentStudent != null ? currentStudent.getCurrentCgpa() : 0.0;

        Color attColor = attPct >= 75.0 ? UITheme.SUCCESS : UITheme.DANGER;
        String attSub = attPct >= 75.0 ? "Eligible for Examinations" : "⚠️ Shortage Warning (<75%)";

        kpi.add(UIComponents.createStatCard("Overall Attendance", String.format("%.1f%%", attPct), attSub, attColor));
        kpi.add(UIComponents.createStatCard("Current SGPA", String.format("%.2f", sgpa), "Current Semester GPA", UITheme.PRIMARY));
        kpi.add(UIComponents.createStatCard("Cumulative CGPA", String.format("%.2f", cgpa), "Academic Standing", UITheme.INFO));
        kpi.add(UIComponents.createStatCard("Batch & Roll", (currentStudent != null ? "Roll #" + currentStudent.getRollNumber() : "01"), (currentStudent != null ? currentStudent.getDepartmentName() : "CSE"), UITheme.WARNING));

        return kpi;
    }

    private JPanel createMarksheetPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Subject Code", "Subject Name", "Exam Type", "Marks Obtained", "Max Marks", "Percentage", "Grade", "Grade Point"};
        marksTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(marksTableModel);

        p.add(new JLabel("Consolidated Academic Marksheet & Series Evaluations"), BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadMarks() {
        marksTableModel.setRowCount(0);
        if (currentStudent == null) return;
        List<Mark> list = markDAO.getMarksForStudent(currentStudent.getId());
        for (Mark m : list) {
            marksTableModel.addRow(new Object[]{
                    m.getSubjectCode(),
                    m.getSubjectName(),
                    m.getExamType().getLabel(),
                    m.getMarksObtained(),
                    m.getMaxMarks(),
                    String.format("%.1f%%", m.getPercentage()),
                    m.getGrade(),
                    m.getGradePoint()
            });
        }
    }

    private JPanel createAttendancePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Subject Code", "Subject Name", "Total Hours", "Present", "Duty Leave Credit", "Absent", "Attendance %", "Status"};
        attTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(attTableModel);

        p.add(new JLabel("Subject-wise Attendance Distribution & Shortage Check"), BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadAttendance() {
        attTableModel.setRowCount(0);
        if (currentStudent == null) return;
        List<AttendanceDAO.SubjectAttendanceSummary> list = attendanceDAO.getStudentSubjectWiseAttendance(currentStudent.getId());
        for (AttendanceDAO.SubjectAttendanceSummary s : list) {
            String stat = s.percentage() >= 75.0 ? "Satisfactory" : "⚠️ SHORTAGE";
            attTableModel.addRow(new Object[]{
                    s.subjectCode(), s.subjectName(), s.totalHours(), s.presentHours(),
                    s.dutyLeaveHours(), s.absentHours(), s.percentage() + "%", stat
            });
        }
    }

    private JPanel createDutyLeavePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JButton btnApply = UIComponents.createPrimaryButton("➕ Apply for Duty Leave");
        btnApply.addActionListener(e -> showApplyDutyLeaveDialog());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh Status");
        btnRefresh.addActionListener(e -> loadDutyLeaves());

        tool.add(btnApply);
        tool.add(btnRefresh);

        String[] cols = {"App ID", "Reason", "Days", "Dates", "Proof", "Current Pipeline Status", "Advisor Remarks", "HOD Remarks", "Principal Sanction"};
        leaveTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(leaveTableModel);

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadDutyLeaves() {
        leaveTableModel.setRowCount(0);
        if (currentStudent == null) return;
        List<DutyLeave> list = dutyLeaveDAO.getLeavesForStudent(currentStudent.getId());
        for (DutyLeave dl : list) {
            leaveTableModel.addRow(new Object[]{
                    dl.getId(),
                    dl.getReason(),
                    dl.getTotalDays(),
                    dl.getStartDate() + " to " + dl.getEndDate(),
                    dl.getProofDetails(),
                    dl.getStatus().getLabel(),
                    dl.getAdvisorRemarks() != null ? dl.getAdvisorRemarks() : "Pending",
                    dl.getHodRemarks() != null ? dl.getHodRemarks() : "Pending",
                    dl.getPrincipalRemarks() != null ? dl.getPrincipalRemarks() : "Pending"
            });
        }
    }

    private void showApplyDutyLeaveDialog() {
        JTextField txtReason = new JTextField();
        JTextField txtStart = new JTextField(LocalDate.now().toString());
        JTextField txtEnd = new JTextField(LocalDate.now().plusDays(1).toString());
        JTextField txtDays = new JTextField("2");
        JTextField txtProof = new JTextField();

        JPanel p = new JPanel(new GridLayout(5, 2, 8, 8));
        p.add(new JLabel("Reason:"));
        p.add(txtReason);
        p.add(new JLabel("Start Date (YYYY-MM-DD):"));
        p.add(txtStart);
        p.add(new JLabel("End Date (YYYY-MM-DD):"));
        p.add(txtEnd);
        p.add(new JLabel("Total Days:"));
        p.add(txtDays);
        p.add(new JLabel("Proof / Ref Document:"));
        p.add(txtProof);

        int opt = JOptionPane.showConfirmDialog(this, p, "Submit Duty Leave Application (3-Tier Pipeline)", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                DutyLeave dl = new DutyLeave();
                dl.setStudentId(currentStudent.getId());
                dl.setRegisterNumber(currentStudent.getRegisterNumber());
                dl.setReason(txtReason.getText().trim());
                dl.setStartDate(Date.valueOf(txtStart.getText().trim()));
                dl.setEndDate(Date.valueOf(txtEnd.getText().trim()));
                dl.setTotalDays(Integer.parseInt(txtDays.getText().trim()));
                dl.setProofDetails(txtProof.getText().trim());

                if (dutyLeaveDAO.applyDutyLeave(dl)) {
                    loadDutyLeaves();
                    JOptionPane.showMessageDialog(this, "Duty leave submitted! Forwarded to your Class Faculty Advisor (CFA) for Tier-1 verification.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error submitting application: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createTimetablePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Day", "Period Slot", "Subject Code", "Subject Name", "Faculty Instructor", "Room"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        if (currentStudent != null) {
            List<TimetableSlot> slots = timetableDAO.getTimetableForClass(currentStudent.getClassId());
            for (TimetableSlot s : slots) {
                model.addRow(new Object[]{
                        s.getDayOfWeek().getLabel(),
                        "Period " + s.getPeriodSlot(),
                        s.getSubjectCode(),
                        s.getSubjectName(),
                        s.getFacultyName(),
                        s.getRoomNumber()
                });
            }
        }

        JTable table = UIComponents.createStyledTable(model);
        p.add(new JLabel("Class Weekly Timetable Schedule"), BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel createAssignmentsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JButton btnSubmit = UIComponents.createPrimaryButton("📝 Submit Work");
        btnSubmit.addActionListener(e -> showSubmitAssignmentDialog());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadAssignments());

        tool.add(btnSubmit);
        tool.add(btnRefresh);

        String[] cols = {"Assignment ID", "Title", "Subject", "Instructor", "Due Date", "Max Marks", "Submission Status", "Marks Awarded"};
        assignTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(assignTableModel);

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadAssignments() {
        assignTableModel.setRowCount(0);
        if (currentStudent == null) return;
        List<Assignment> list = assignmentDAO.getAssignmentsForClass(currentStudent.getClassId(), currentStudent.getId());
        for (Assignment a : list) {
            assignTableModel.addRow(new Object[]{
                    a.getId(),
                    a.getTitle(),
                    a.getSubjectCode() + " - " + a.getSubjectName(),
                    a.getFacultyName(),
                    a.getDueDate(),
                    a.getMaxMarks(),
                    a.isSubmittedByCurrentStudent() ? "SUBMITTED" : "PENDING",
                    a.getMarksAwarded() != null ? a.getMarksAwarded() : "Pending Evaluation"
            });
        }
    }

    private void showSubmitAssignmentDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter Assignment ID to submit:", "Submit Assignment", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int aid = Integer.parseInt(input.trim());
            JTextArea txtWork = new JTextArea(6, 25);
            txtWork.setLineWrap(true);

            JPanel p = new JPanel(new BorderLayout(8, 8));
            p.add(new JLabel("Paste text solution / code / submission notes:"), BorderLayout.NORTH);
            p.add(new JScrollPane(txtWork), BorderLayout.CENTER);

            int opt = JOptionPane.showConfirmDialog(this, p, "Assignment Submission", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                if (assignmentDAO.submitAssignment(aid, currentStudent.getId(), txtWork.getText().trim(), "solution.txt")) {
                    loadAssignments();
                    JOptionPane.showMessageDialog(this, "Assignment submitted successfully!");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid Assignment ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createGrievancePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tool.setOpaque(false);

        JButton btnNewGriev = UIComponents.createPrimaryButton("➕ File New Grievance");
        btnNewGriev.addActionListener(e -> showFileGrievanceDialog());

        JButton btnRef = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRef.addActionListener(e -> loadGrievances());

        tool.add(btnNewGriev);
        tool.add(btnRef);

        String[] cols = {"Grievance ID", "Category", "Title", "Description", "Status", "Resolution Notes", "Resolved By"};
        grievTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(grievTableModel);

        p.add(tool, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadGrievances() {
        grievTableModel.setRowCount(0);
        if (currentStudent == null) return;
        List<Grievance> list = grievanceDAO.getGrievancesByStudent(currentStudent.getId());
        for (Grievance g : list) {
            grievTableModel.addRow(new Object[]{
                    g.getId(),
                    g.getCategory().getLabel(),
                    g.getTitle(),
                    g.getDescription(),
                    g.getStatus().getLabel(),
                    g.getResolution() != null ? g.getResolution() : "Under Review",
                    g.getResolvedByUserName() != null ? g.getResolvedByUserName() : "-"
            });
        }
    }

    private void showFileGrievanceDialog() {
        JComboBox<Grievance.Category> cmbCat = new JComboBox<>(Grievance.Category.values());
        JTextField txtTitle = new JTextField();
        JTextArea txtDesc = new JTextArea(5, 25);
        txtDesc.setLineWrap(true);

        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
        p.add(new JLabel("Category:"));
        p.add(cmbCat);
        p.add(new JLabel("Title:"));
        p.add(txtTitle);
        p.add(new JLabel("Description:"));
        p.add(new JScrollPane(txtDesc));

        int opt = JOptionPane.showConfirmDialog(this, p, "Submit Grievance to Administration", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            Grievance g = new Grievance();
            g.setStudentId(currentStudent.getId());
            g.setRegisterNumber(currentStudent.getRegisterNumber());
            g.setCategory((Grievance.Category) cmbCat.getSelectedItem());
            g.setTitle(txtTitle.getText().trim());
            g.setDescription(txtDesc.getText().trim());

            if (grievanceDAO.submitGrievance(g)) {
                loadGrievances();
                JOptionPane.showMessageDialog(this, "Grievance lodged successfully. Forwarded to the Office of the Principal.");
            }
        }
    }

    private JPanel createFeedbackPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        if (currentStudent == null) return p;

        List<SubjectDAO.ClassTeacherMapping> teachers = subjectDAO.getFacultyAssignedToClass(currentStudent.getClassId());
        JComboBox<SubjectDAO.ClassTeacherMapping> cmbTeacher = new JComboBox<>();
        for (SubjectDAO.ClassTeacherMapping t : teachers) {
            cmbTeacher.addItem(t);
        }

        JComboBox<Integer> cmbTeach = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        JComboBox<Integer> cmbPunct = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        JComboBox<Integer> cmbClarity = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        JTextArea txtCom = new JTextArea(4, 25);
        txtCom.setLineWrap(true);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Select Course & Faculty Instructor:"));
        form.add(cmbTeacher);
        form.add(new JLabel("Teaching Effectiveness (1-5):"));
        form.add(cmbTeach);
        form.add(new JLabel("Punctuality & Regularity (1-5):"));
        form.add(cmbPunct);
        form.add(new JLabel("Concept Clarity & Doubt Resolution (1-5):"));
        form.add(cmbClarity);
        form.add(new JLabel("Anonymous Feedback Comments:"));
        form.add(new JScrollPane(txtCom));

        JButton btnSubmit = UIComponents.createPrimaryButton("Submit Confidential Feedback");
        btnSubmit.addActionListener(e -> {
            SubjectDAO.ClassTeacherMapping sel = (SubjectDAO.ClassTeacherMapping) cmbTeacher.getSelectedItem();
            if (sel != null) {
                Feedback fb = new Feedback();
                fb.setStudentId(currentStudent.getId());
                fb.setFacultyId(sel.facultyId());
                fb.setSubjectId(1); // Mapped subject
                fb.setRatingTeaching((Integer) cmbTeach.getSelectedItem());
                fb.setRatingPunctuality((Integer) cmbPunct.getSelectedItem());
                fb.setRatingClarity((Integer) cmbClarity.getSelectedItem());
                fb.setComments(txtCom.getText().trim());

                if (feedbackDAO.submitFeedback(fb)) {
                    JOptionPane.showMessageDialog(this, "Feedback submitted anonymously. Thank you for your contribution!");
                    txtCom.setText("");
                }
            }
        });

        p.add(new JLabel("Submit Anonymous Faculty Feedback"), BorderLayout.NORTH);
        p.add(form, BorderLayout.CENTER);
        p.add(btnSubmit, BorderLayout.SOUTH);

        return p;
    }

    private void loadAllData() {
        loadMarks();
        loadAttendance();
        loadDutyLeaves();
        loadAssignments();
        loadGrievances();
    }
}
