package com.acadexia.ui.student;

import com.acadexia.dao.*;
import com.acadexia.model.*;
import com.acadexia.security.SessionManager;
import com.acadexia.security.SecurityUtils;
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
    private final UserDAO userDAO = new UserDAO();

    private JComboBox<String> cmbSemesterFilter;
    private JLabel lblMarksSummary;
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
        tabs.addTab("⚙️ Settings", createSettingsPanel());

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
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Toolbar for Semester selection
        JPanel toolBar = new JPanel(new BorderLayout(10, 0));
        toolBar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel lblSelectSem = new JLabel("Select Academic Semester:");
        lblSelectSem.setFont(UITheme.FONT_BOLD);
        lblSelectSem.setForeground(UITheme.TEXT_PRIMARY);

        String[] sems = {
                "Semester 1", "Semester 2", "Semester 3", "Semester 4",
                "Semester 5", "Semester 6", "Semester 7", "Semester 8",
                "All Semesters"
        };
        cmbSemesterFilter = new JComboBox<>(sems);
        cmbSemesterFilter.setFont(UITheme.FONT_REGULAR);

        int curSem = (currentStudent != null && currentStudent.getSemester() > 0) ? currentStudent.getSemester() : 5;
        if (curSem >= 1 && curSem <= 8) {
            cmbSemesterFilter.setSelectedIndex(curSem - 1);
        }

        cmbSemesterFilter.addActionListener(e -> loadMarks());

        JButton btnLoad = UIComponents.createPrimaryButton("🔍 Load Marksheet");
        btnLoad.addActionListener(e -> loadMarks());

        left.add(lblSelectSem);
        left.add(cmbSemesterFilter);
        left.add(btnLoad);

        lblMarksSummary = new JLabel();
        lblMarksSummary.setFont(UITheme.FONT_BOLD);
        lblMarksSummary.setForeground(UITheme.PRIMARY);

        toolBar.add(left, BorderLayout.WEST);
        toolBar.add(lblMarksSummary, BorderLayout.EAST);

        String[] cols = {"Semester", "Subject Code", "Subject Name", "Exam Type", "Marks Obtained", "Max Marks", "Percentage", "Grade", "Grade Point"};
        marksTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(marksTableModel);

        p.add(toolBar, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadMarks() {
        marksTableModel.setRowCount(0);
        if (currentStudent == null) return;

        int selectedIdx = cmbSemesterFilter != null ? cmbSemesterFilter.getSelectedIndex() : -1;
        List<Mark> list;
        if (selectedIdx >= 0 && selectedIdx <= 7) {
            int targetSemester = selectedIdx + 1;
            list = markDAO.getMarksForStudentAndSemester(currentStudent.getId(), targetSemester);
        } else {
            list = markDAO.getMarksForStudent(currentStudent.getId());
        }

        double totalMarksObtained = 0;
        double totalMaxMarks = 0;

        for (Mark m : list) {
            marksTableModel.addRow(new Object[]{
                    "Sem " + m.getSemester(),
                    m.getSubjectCode(),
                    m.getSubjectName(),
                    m.getExamType().getLabel(),
                    m.getMarksObtained(),
                    m.getMaxMarks(),
                    String.format("%.1f%%", m.getPercentage()),
                    m.getGrade(),
                    m.getGradePoint()
            });
            totalMarksObtained += m.getMarksObtained();
            totalMaxMarks += m.getMaxMarks();
        }

        if (lblMarksSummary != null) {
            String semText = (selectedIdx >= 0 && selectedIdx <= 7) ? "Semester " + (selectedIdx + 1) : "All Semesters";
            double avgPct = totalMaxMarks > 0 ? (totalMarksObtained / totalMaxMarks) * 100.0 : 0.0;
            lblMarksSummary.setText(String.format("%s | Records: %d | Total: %.1f/%.1f (%.1f%%)",
                    semText, list.size(), totalMarksObtained, totalMaxMarks, avgPct));
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
        int classId = currentStudent != null ? currentStudent.getClassId() : 1;
        return new StudentTimetablePanel(classId);
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

    private JPanel createSettingsPanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        p.add(UIComponents.createHeader("⚙️ Account & Profile Settings",
                "View profile details and change your account authentication password."), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 20));
        grid.setOpaque(false);

        // Panel A: Profile Details (Read-Only)
        JPanel profileCard = UIComponents.createCardPanel();
        profileCard.setLayout(new BorderLayout(14, 14));

        JLabel lblProfileTitle = new JLabel("👤 Student Information (Read-Only)");
        lblProfileTitle.setFont(UITheme.FONT_SECTION);
        lblProfileTitle.setForeground(UITheme.PRIMARY);

        JPanel profileForm = new JPanel(new GridBagLayout());
        profileForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        User currentUser = SessionManager.getInstance().getCurrentUser();

        String sName = currentStudent != null ? currentStudent.getFullName() : (currentUser != null ? currentUser.getFullName() : "N/A");
        String sDept = currentStudent != null ? currentStudent.getDepartmentName() : "Computer Science & Engineering";
        String sUsername = currentUser != null ? currentUser.getUsername() : "N/A";

        JTextField txtName = createReadOnlyTextField(sName);
        JTextField txtDept = createReadOnlyTextField(sDept);
        JTextField txtUser = createReadOnlyTextField(sUsername);

        addFormField(profileForm, gbc, 0, "Full Name:", txtName);
        addFormField(profileForm, gbc, 1, "Department:", txtDept);
        addFormField(profileForm, gbc, 2, "Username:", txtUser);

        JLabel lblProfileNote = UIComponents.createBadge("🔒 Institutional records (Name, Department, Username) are read-only.",
                new Color(30, 41, 59), UITheme.TEXT_MUTED);

        profileCard.add(lblProfileTitle, BorderLayout.NORTH);
        profileCard.add(profileForm, BorderLayout.CENTER);
        profileCard.add(lblProfileNote, BorderLayout.SOUTH);

        // Panel B: Change Password (Editable)
        JPanel passwordCard = UIComponents.createCardPanel();
        passwordCard.setLayout(new BorderLayout(14, 14));

        JLabel lblPassTitle = new JLabel("🔑 Security & Password Change");
        lblPassTitle.setFont(UITheme.FONT_SECTION);
        lblPassTitle.setForeground(UITheme.PRIMARY);

        JPanel passForm = new JPanel(new GridBagLayout());
        passForm.setOpaque(false);

        JPasswordField txtCurrentPass = new JPasswordField();
        JPasswordField txtNewPass = new JPasswordField();
        JPasswordField txtConfirmPass = new JPasswordField();

        addFormField(passForm, gbc, 0, "Current Password:", txtCurrentPass);
        addFormField(passForm, gbc, 1, "New Password:", txtNewPass);
        addFormField(passForm, gbc, 2, "Confirm Password:", txtConfirmPass);

        JButton btnChangePass = UIComponents.createPrimaryButton("💾 Save New Password");
        btnChangePass.addActionListener(e -> {
            String currentPass = new String(txtCurrentPass.getPassword());
            String newPass = new String(txtNewPass.getPassword());
            String confirmPass = new String(txtConfirmPass.getPassword());

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all password fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User activeUser = SessionManager.getInstance().getCurrentUser();
            if (activeUser == null) {
                JOptionPane.showMessageDialog(this, "Session error. User not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!SecurityUtils.checkPassword(currentPass, activeUser.getPasswordHash())) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "New password and confirm password do not match.", "Password Mismatch", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newPass.length() < 4) {
                JOptionPane.showMessageDialog(this, "New password must be at least 4 characters long.", "Weak Password", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean updated = userDAO.updatePassword(activeUser.getId(), newPass);
            if (updated) {
                activeUser.setPasswordHash(SecurityUtils.hashPassword(newPass));
                AuditLogDAO.log(activeUser.getId(), activeUser.getUsername(), activeUser.getRole().name(),
                        "PASSWORD_CHANGE", "AUTH", String.valueOf(activeUser.getId()),
                        "Student updated account password successfully.", AuditLog.Severity.INFO);

                txtCurrentPass.setText("");
                txtNewPass.setText("");
                txtConfirmPass.setText("");
                JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update password. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        passwordCard.add(lblPassTitle, BorderLayout.NORTH);
        passwordCard.add(passForm, BorderLayout.CENTER);
        passwordCard.add(btnChangePass, BorderLayout.SOUTH);

        grid.add(profileCard);
        grid.add(passwordCard);

        p.add(grid, BorderLayout.CENTER);
        return p;
    }

    private JTextField createReadOnlyTextField(String value) {
        JTextField tf = new JTextField(value);
        tf.setEditable(false);
        tf.setFocusable(false);
        tf.setFont(UITheme.FONT_BOLD);
        tf.setBackground(new Color(25, 25, 38));
        tf.setForeground(UITheme.TEXT_MUTED);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 70), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private void loadAllData() {
        loadMarks();
        loadAttendance();
        loadDutyLeaves();
        loadAssignments();
        loadGrievances();
    }
}
