package com.acadexia.ui.principal;

import com.acadexia.dao.DepartmentDAO;
import com.acadexia.dao.DutyLeaveDAO;
import com.acadexia.dao.FacultyDAO;
import com.acadexia.dao.GrievanceDAO;
import com.acadexia.dao.StudentDAO;
import com.acadexia.model.Department;
import com.acadexia.model.DutyLeave;
import com.acadexia.model.Faculty;
import com.acadexia.model.Grievance;
import com.acadexia.model.Student;
import com.acadexia.model.User;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.auth.LandingFrame;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PrincipalDashboardFrame extends JFrame {

    private final DutyLeaveDAO dutyLeaveDAO = new DutyLeaveDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final GrievanceDAO grievanceDAO = new GrievanceDAO();

    private DefaultTableModel leaveTableModel;
    private DefaultTableModel deptTableModel;
    private DefaultTableModel analyticsTableModel;
    private DefaultTableModel grievanceTableModel;

    private List<DutyLeave> pendingLeaves;
    private List<Grievance> allGrievances;

    public PrincipalDashboardFrame() {
        setTitle("Acadexia - Office of the Principal (Executive Academic Governance)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1140, 740);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        initUI();
        loadAllData();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Top Navigation Bar
        add(createTopBar(), BorderLayout.NORTH);

        // Center Tabbed Views
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);

        tabs.addTab("👑 Executive Overview", createOverviewPanel());
        tabs.addTab("📋 Tier-3 Duty Leave Sanctions", createDutyLeaveSanctionsPanel());
        tabs.addTab("🏛️ Department & HOD Governance", createDepartmentGovernancePanel());
        tabs.addTab("📊 College-wide Academic Analytics", createAcademicAnalyticsPanel());
        tabs.addTab("⚖️ Grievance Redressal Oversight", createGrievanceOversightPanel());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(18, 18, 28));
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel lblTitle = new JLabel("ACADEXIA PRINCIPAL PORTAL");
        lblTitle.setFont(UITheme.FONT_SECTION);
        lblTitle.setForeground(new Color(245, 158, 11)); // Amber/Gold accent

        JLabel lblBadge = UIComponents.createBadge("Executive Academic Sanctions", new Color(120, 53, 15), Color.WHITE);
        left.add(lblTitle);
        left.add(lblBadge);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        JLabel lblUser = new JLabel("Signed in: " + (currentUser != null ? currentUser.getFullName() : "Principal"));
        lblUser.setFont(UITheme.FONT_REGULAR);
        lblUser.setForeground(UITheme.TEXT_MUTED);

        JButton btnLogout = UIComponents.createDangerButton("Log Out");
        btnLogout.addActionListener(e -> {
            SessionManager.getInstance().logout();
            new LandingFrame().setVisible(true);
            dispose();
        });

        right.add(lblUser);
        right.add(btnLogout);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 16, 16));
        kpiGrid.setOpaque(false);

        List<Student> students = studentDAO.getAllStudents();
        List<Faculty> facultyList = facultyDAO.getAllFaculty();
        List<Department> departments = departmentDAO.getAllDepartments();
        List<DutyLeave> leaves = dutyLeaveDAO.getPendingLeavesForPrincipal();

        kpiGrid.add(UIComponents.createStatCard("Total Enrollment", String.valueOf(students.size()), "Registered Students", UITheme.PRIMARY));
        kpiGrid.add(UIComponents.createStatCard("Faculty Strength", String.valueOf(facultyList.size()), "Teaching Staff", UITheme.INFO));
        kpiGrid.add(UIComponents.createStatCard("Academic Depts", String.valueOf(departments.size()), "Active Departments", UITheme.SUCCESS));
        kpiGrid.add(UIComponents.createStatCard("Pending Sanctions", String.valueOf(leaves.size()), "Tier-3 Duty Leaves", UITheme.WARNING));

        panel.add(kpiGrid, BorderLayout.NORTH);

        // Center Welcome Card
        JPanel centerCard = UIComponents.createCardPanel();
        centerCard.setLayout(new BoxLayout(centerCard, BoxLayout.Y_AXIS));

        JLabel lblH = new JLabel("Principal Executive Dashboard");
        lblH.setFont(UITheme.FONT_TITLE);
        lblH.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblDesc = new JLabel("<html>As Principal, you have supreme authority over institutional decisions:<br>" +
                "• <b>Tier-3 Duty Leave Sanctions:</b> Review applications verified by CFAs and endorsed by HODs.<br>" +
                "• <b>HOD Appointments:</b> Assign or reappoint Department Heads across academic departments.<br>" +
                "• <b>Performance Analytics:</b> Monitor institution-wide GPA distribution and attendance warnings.<br>" +
                "• <b>Grievance Redressal:</b> Adjudicate student grievances and record institutional resolutions.</html>");
        lblDesc.setFont(UITheme.FONT_REGULAR);
        lblDesc.setForeground(UITheme.TEXT_MUTED);

        centerCard.add(lblH);
        centerCard.add(Box.createVerticalStrut(12));
        centerCard.add(lblDesc);

        panel.add(centerCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDutyLeaveSanctionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnApprove = UIComponents.createSuccessButton("✅ Final Sanction (Approve)");
        btnApprove.addActionListener(e -> processDutyLeave(true));

        JButton btnReject = UIComponents.createDangerButton("❌ Deny / Reject");
        btnReject.addActionListener(e -> processDutyLeave(false));

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadDutyLeaves());

        toolbar.add(btnApprove);
        toolbar.add(btnReject);
        toolbar.add(btnRefresh);

        String[] cols = {"App ID", "Student", "Register No", "Class", "Department", "Reason", "Days", "Dates", "Proof", "CFA Remarks", "HOD Remarks"};
        leaveTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(leaveTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadDutyLeaves() {
        leaveTableModel.setRowCount(0);
        pendingLeaves = dutyLeaveDAO.getPendingLeavesForPrincipal();
        for (DutyLeave dl : pendingLeaves) {
            leaveTableModel.addRow(new Object[]{
                    dl.getId(),
                    dl.getStudentName(),
                    dl.getRegisterNumber(),
                    dl.getClassName(),
                    dl.getDepartmentName(),
                    dl.getReason(),
                    dl.getTotalDays(),
                    dl.getStartDate() + " to " + dl.getEndDate(),
                    dl.getProofDetails(),
                    dl.getAdvisorRemarks() != null ? dl.getAdvisorRemarks() : "-",
                    dl.getHodRemarks() != null ? dl.getHodRemarks() : "-"
            });
        }
    }

    private void processDutyLeave(boolean approve) {
        if (pendingLeaves == null || pendingLeaves.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending duty leaves to sanction.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String input = JOptionPane.showInputDialog(this,
                "Enter Application ID to " + (approve ? "APPROVE" : "REJECT") + ":",
                "Sanction Duty Leave", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int appId = Integer.parseInt(input.trim());
            DutyLeave target = pendingLeaves.stream().filter(l -> l.getId() == appId).findFirst().orElse(null);
            if (target == null) {
                JOptionPane.showMessageDialog(this, "Application ID not found in pending list.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String remarks = JOptionPane.showInputDialog(this, "Enter Principal Sanction Remarks:",
                    approve ? "Duty Leave Approved" : "Duty Leave Rejected");
            if (remarks == null) remarks = approve ? "Sanctioned by Principal" : "Rejected by Principal";

            String principalUser = SessionManager.getInstance().getCurrentUser().getUsername();
            boolean success = dutyLeaveDAO.updatePrincipalDecision(appId, approve, remarks, principalUser);

            if (success) {
                loadDutyLeaves();
                JOptionPane.showMessageDialog(this, "Duty leave application " + appId + " successfully " + (approve ? "APPROVED" : "REJECTED") + "!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Application ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createDepartmentGovernancePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnAssignHod = UIComponents.createPrimaryButton("👔 Appoint / Change HOD");
        btnAssignHod.addActionListener(e -> showAssignHodDialog());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadDepartments());

        toolbar.add(btnAssignHod);
        toolbar.add(btnRefresh);

        String[] cols = {"Dept ID", "Code", "Department Name", "Current HOD Name"};
        deptTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(deptTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadDepartments() {
        deptTableModel.setRowCount(0);
        List<Department> list = departmentDAO.getAllDepartments();
        for (Department d : list) {
            deptTableModel.addRow(new Object[]{
                    d.getId(),
                    d.getCode(),
                    d.getName(),
                    d.getHodName() != null ? d.getHodName() : "⚠️ Unassigned"
            });
        }
    }

    private void showAssignHodDialog() {
        List<Department> depts = departmentDAO.getAllDepartments();
        List<Faculty> facultyList = facultyDAO.getAllFaculty();

        if (depts.isEmpty() || facultyList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No departments or faculty available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<Department> cmbDept = new JComboBox<>(depts.toArray(new Department[0]));
        JComboBox<Faculty> cmbFac = new JComboBox<>(facultyList.toArray(new Faculty[0]));

        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Department:"));
        p.add(cmbDept);
        p.add(new JLabel("Appoint Faculty as HOD:"));
        p.add(cmbFac);

        int result = JOptionPane.showConfirmDialog(this, p, "Appoint Head of Department (HOD)", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Department selDept = (Department) cmbDept.getSelectedItem();
            Faculty selFac = (Faculty) cmbFac.getSelectedItem();
            if (selDept != null && selFac != null) {
                departmentDAO.updateHod(selDept.getId(), selFac.getId());
                loadDepartments();
                JOptionPane.showMessageDialog(this, "Successfully appointed " + selFac.getFullName() + " as HOD of " + selDept.getName());
            }
        }
    }

    private JPanel createAcademicAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh Analytics");
        btnRefresh.addActionListener(e -> loadAcademicAnalytics());
        toolbar.add(btnRefresh);

        String[] cols = {"Register No", "Roll", "Student Name", "Class", "Department", "Semester", "Attendance %", "SGPA", "CGPA"};
        analyticsTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(analyticsTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadAcademicAnalytics() {
        analyticsTableModel.setRowCount(0);
        List<Student> students = studentDAO.getAllStudents();
        for (Student s : students) {
            analyticsTableModel.addRow(new Object[]{
                    s.getRegisterNumber(),
                    s.getRollNumber(),
                    s.getFullName(),
                    s.getClassName(),
                    s.getDepartmentName(),
                    s.getSemester(),
                    s.getAttendancePercentage() + "%",
                    s.getCurrentSgpa(),
                    s.getCurrentCgpa()
            });
        }
    }

    private JPanel createGrievanceOversightPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnResolve = UIComponents.createPrimaryButton("⚖️ Adjudicate / Resolve Grievance");
        btnResolve.addActionListener(e -> showResolveGrievanceDialog());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadGrievances());

        toolbar.add(btnResolve);
        toolbar.add(btnRefresh);

        String[] cols = {"ID", "Student", "Register No", "Category", "Title", "Status", "Resolution", "Resolved By"};
        grievanceTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(grievanceTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadGrievances() {
        grievanceTableModel.setRowCount(0);
        allGrievances = grievanceDAO.getAllGrievances();
        for (Grievance g : allGrievances) {
            grievanceTableModel.addRow(new Object[]{
                    g.getId(),
                    g.getStudentName(),
                    g.getRegisterNumber(),
                    g.getCategory().getLabel(),
                    g.getTitle(),
                    g.getStatus().getLabel(),
                    g.getResolution() != null ? g.getResolution() : "Pending",
                    g.getResolvedByUserName() != null ? g.getResolvedByUserName() : "-"
            });
        }
    }

    private void showResolveGrievanceDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter Grievance ID to Resolve / Dismiss:", "Adjudicate Grievance", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int gid = Integer.parseInt(input.trim());
            JComboBox<Grievance.Status> cmbStatus = new JComboBox<>(new Grievance.Status[]{Grievance.Status.RESOLVED, Grievance.Status.UNDER_REVIEW, Grievance.Status.DISMISSED});
            JTextArea txtRes = new JTextArea(4, 25);
            txtRes.setLineWrap(true);

            JPanel p = new JPanel(new BorderLayout(8, 8));
            p.add(cmbStatus, BorderLayout.NORTH);
            p.add(new JScrollPane(txtRes), BorderLayout.CENTER);

            int opt = JOptionPane.showConfirmDialog(this, p, "Action & Resolution Notes", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                User u = SessionManager.getInstance().getCurrentUser();
                grievanceDAO.updateResolution(gid, (Grievance.Status) cmbStatus.getSelectedItem(), txtRes.getText().trim(), u.getId(), u.getUsername());
                loadGrievances();
                JOptionPane.showMessageDialog(this, "Grievance status successfully updated.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Grievance ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllData() {
        loadDutyLeaves();
        loadDepartments();
        loadAcademicAnalytics();
        loadGrievances();
    }
}
