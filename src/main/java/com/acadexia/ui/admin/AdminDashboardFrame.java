package com.acadexia.ui.admin;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.dao.AuditLogDAO;
import com.acadexia.dao.UserDAO;
import com.acadexia.model.AuditLog;
import com.acadexia.model.Role;
import com.acadexia.model.User;
import com.acadexia.security.InputFilterFactory;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.auth.LandingFrame;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboardFrame extends JFrame {

    private final AuditLogDAO auditDAO = new AuditLogDAO();
    private final UserDAO userDAO = new UserDAO();

    private DefaultTableModel auditTableModel;
    private DefaultTableModel userTableModel;
    private JComboBox<String> cmbSeverity;

    public AdminDashboardFrame() {
        setTitle("Acadexia - System Administration & Security Governance");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        initUI();
        loadAuditLogs();
        loadUsers();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Top Navigation Header
        add(createTopBar(), BorderLayout.NORTH);

        // Center Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);

        tabs.addTab("🛡️ Security & Audit Logs", createAuditLogsPanel());
        tabs.addTab("👥 User Account Governance", createUserManagementPanel());
        tabs.addTab("⚙️ System & Database Status", createSystemStatusPanel());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(18, 18, 28));
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel lblTitle = new JLabel("ACADEXIA SYSTEM ADMIN");
        lblTitle.setFont(UITheme.FONT_SECTION);
        lblTitle.setForeground(UITheme.PRIMARY);

        JLabel lblBadge = UIComponents.createBadge("IT Governance & Auditing", new Color(49, 46, 129), UITheme.TEXT_PRIMARY);
        left.add(lblTitle);
        left.add(lblBadge);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        JLabel lblUser = new JLabel("Signed in as: " + (currentUser != null ? currentUser.getFullName() : "Admin"));
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

    private JPanel createAuditLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter Severity:");
        lblFilter.setFont(UITheme.FONT_BOLD);
        lblFilter.setForeground(UITheme.TEXT_PRIMARY);

        cmbSeverity = new JComboBox<>(new String[]{"ALL", "SECURITY_ALERT", "WARNING", "INFO", "ERROR"});
        cmbSeverity.addActionListener(e -> loadAuditLogs());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh Logs");
        btnRefresh.addActionListener(e -> loadAuditLogs());

        JButton btnTestExploit = UIComponents.createDangerButton("⚡ Test SQL Exploit Detection");
        btnTestExploit.setToolTipText("Simulate a blocked SQL injection attack to verify real-time security alerting");
        btnTestExploit.addActionListener(e -> {
            AuditLogDAO.logSecurityAlert("TEST_ATTACKER", "SQL_INJECTION_INTERCEPTED",
                    "Blocked input: ' OR '1'='1'; DROP TABLE users; -- on authentication gateway.");
            loadAuditLogs();
            JOptionPane.showMessageDialog(this,
                    "Simulated malicious payload intercepted and logged to security audit trail!",
                    "Security Event Logged", JOptionPane.WARNING_MESSAGE);
        });

        toolbar.add(lblFilter);
        toolbar.add(cmbSeverity);
        toolbar.add(btnRefresh);
        toolbar.add(btnTestExploit);

        // Audit Table
        String[] cols = {"ID", "Timestamp", "User", "Role", "Action", "Target", "Severity", "Details"};
        auditTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(auditTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(320);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadAuditLogs() {
        auditTableModel.setRowCount(0);
        String severity = (String) cmbSeverity.getSelectedItem();
        List<AuditLog> logs = auditDAO.getRecentLogs(100, severity);
        for (AuditLog l : logs) {
            auditTableModel.addRow(new Object[]{
                    l.getId(),
                    l.getTimestamp(),
                    l.getUsername(),
                    l.getRole(),
                    l.getAction(),
                    l.getEntityType() != null ? l.getEntityType() : "-",
                    l.getSeverity().name(),
                    l.getDetails()
            });
        }
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnNewUser = UIComponents.createPrimaryButton("➕ Provision New User");
        btnNewUser.addActionListener(e -> showCreateUserDialog());

        JButton btnToggleStatus = UIComponents.createSecondaryButton("Toggle Active / Deactivate");
        btnToggleStatus.addActionListener(e -> toggleSelectedUser());

        JButton btnRefresh = UIComponents.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadUsers());

        toolbar.add(btnNewUser);
        toolbar.add(btnToggleStatus);
        toolbar.add(btnRefresh);

        // User Table
        String[] cols = {"User ID", "Username", "Full Name", "Role", "Email", "Phone", "Status", "Created At"};
        userTableModel = new DefaultTableModel(cols, 0);
        JTable table = UIComponents.createStyledTable(userTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void loadUsers() {
        userTableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            userTableModel.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole().name(),
                    u.getEmail() != null ? u.getEmail() : "-",
                    u.getPhone() != null ? u.getPhone() : "-",
                    u.isActive() ? "ACTIVE" : "INACTIVE",
                    u.getCreatedAt()
            });
        }
    }

    private void toggleSelectedUser() {
        // Simple toggle implementation
        String input = JOptionPane.showInputDialog(this, "Enter User ID to toggle status:", "Toggle User Status", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int uid = Integer.parseInt(input.trim());
                User u = userDAO.getById(uid);
                if (u != null) {
                    boolean newStatus = !u.isActive();
                    userDAO.updateUserStatus(uid, newStatus);
                    AuditLogDAO.log(uid, u.getUsername(), "ADMIN", "USER_STATUS_TOGGLED", "USER",
                            String.valueOf(uid), "Status changed to: " + (newStatus ? "ACTIVE" : "INACTIVE"), AuditLog.Severity.WARNING);
                    loadUsers();
                    JOptionPane.showMessageDialog(this, "User status updated to: " + (newStatus ? "ACTIVE" : "INACTIVE"));
                } else {
                    JOptionPane.showMessageDialog(this, "User ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid User ID format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCreateUserDialog() {
        JDialog dlg = new JDialog(this, "Provision New User Account", true);
        dlg.setSize(440, 480);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(7, 2, 10, 12));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField txtU = new JTextField();
        JPasswordField txtP = new JPasswordField();
        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();
        JComboBox<Role> cmbRole = new JComboBox<>(Role.values());

        // Apply strict filters
        InputFilterFactory.applyIdentifierFilter(txtU, 30);
        InputFilterFactory.applyPasswordFilter(txtP, 30);
        InputFilterFactory.applySqlSanitizer(txtName, 60, true);
        InputFilterFactory.applySqlSanitizer(txtEmail, 60, false);
        InputFilterFactory.applyNumericFilter(txtPhone, 15, false);

        p.add(new JLabel("Username:"));
        p.add(txtU);
        p.add(new JLabel("Password:"));
        p.add(txtP);
        p.add(new JLabel("Full Name:"));
        p.add(txtName);
        p.add(new JLabel("Role:"));
        p.add(cmbRole);
        p.add(new JLabel("Email:"));
        p.add(txtEmail);
        p.add(new JLabel("Phone:"));
        p.add(txtPhone);

        JButton btnSubmit = UIComponents.createPrimaryButton("Create Account");
        btnSubmit.addActionListener(e -> {
            if (txtU.getText().trim().isEmpty() || txtP.getPassword().length == 0 || txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Username, password, and name are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            User u = new User();
            u.setUsername(txtU.getText().trim());
            u.setFullName(txtName.getText().trim());
            u.setRole((Role) cmbRole.getSelectedItem());
            u.setEmail(txtEmail.getText().trim());
            u.setPhone(txtPhone.getText().trim());
            u.setActive(true);

            try {
                int id = userDAO.createUser(u, new String(txtP.getPassword()));
                if (id > 0) {
                    AuditLogDAO.log(id, u.getUsername(), "ADMIN", "USER_PROVISIONED", "USER",
                            String.valueOf(id), "Created new " + u.getRole() + " account", AuditLog.Severity.INFO);
                    loadUsers();
                    dlg.dispose();
                    JOptionPane.showMessageDialog(this, "User created successfully with ID: " + id);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error creating user: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        dlg.add(btnSubmit, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JPanel createSystemStatusPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 16, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        boolean dbOk = DatabaseConnection.testConnection();

        panel.add(UIComponents.createStatCard("Database Engine", "MySQL 8 / 9", "Port 3306 | utf8mb4", UITheme.PRIMARY));
        panel.add(UIComponents.createStatCard("Connection Status", dbOk ? "ONLINE" : "OFFLINE", dbOk ? "Active and healthy" : "Check credentials", dbOk ? UITheme.SUCCESS : UITheme.DANGER));
        panel.add(UIComponents.createStatCard("Security Filter", "ACTIVE", "Swing DocumentFilter + Parameterized Queries", UITheme.SUCCESS));
        panel.add(UIComponents.createStatCard("Architecture", "Java Swing + FlatLaf", "Strictly Native Desktop UI", UITheme.INFO));

        return panel;
    }
}
