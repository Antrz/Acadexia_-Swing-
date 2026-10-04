package com.acadexia.ui.auth;

import com.acadexia.dao.UserDAO;
import com.acadexia.model.Role;
import com.acadexia.model.User;
import com.acadexia.security.InputFilterFactory;
import com.acadexia.security.SessionManager;
import com.acadexia.ui.admin.AdminDashboardFrame;
import com.acadexia.ui.common.UIComponents;
import com.acadexia.ui.common.UITheme;
import com.acadexia.ui.faculty.FacultyDashboardFrame;
import com.acadexia.ui.principal.PrincipalDashboardFrame;
import com.acadexia.ui.student.StudentDashboardFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern Swing Landing and Login Portal with real-time SQL injection filtering,
 * multi-identifier resolution, and quick-fill evaluation shortcuts.
 */
public class LandingFrame extends JFrame {

    private final JTextField txtIdentifier = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JLabel lblError = new JLabel(" ");
    private final UserDAO userDAO = new UserDAO();

    public LandingFrame() {
        setTitle("Acadexia - College Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Split panel: Left Hero, Right Login Card
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        mainPanel.add(createHeroPanel());
        mainPanel.add(createLoginPanel());

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeroPanel() {
        JPanel hero = new JPanel(new BorderLayout(15, 15));
        hero.setOpaque(false);

        // Header
        JPanel top = new JPanel(new GridLayout(2, 1, 4, 4));
        top.setOpaque(false);
        JLabel lblBrand = new JLabel("ACADEXIA");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblBrand.setForeground(UITheme.PRIMARY);

        JLabel lblTagline = new JLabel("Next-Generation Academic Administration & Governance");
        lblTagline.setFont(UITheme.FONT_SUBTITLE);
        lblTagline.setForeground(UITheme.TEXT_MUTED);

        top.add(lblBrand);
        top.add(lblTagline);

        // Feature list card
        JPanel featuresCard = UIComponents.createCardPanel();
        featuresCard.setLayout(new BoxLayout(featuresCard, BoxLayout.Y_AXIS));

        addFeatureItem(featuresCard, "🎓 Student Life Cycle", "Results, internals, SGPA/CGPA, duty leaves, attendance tracking & feedback.");
        featuresCard.add(Box.createVerticalStrut(14));
        addFeatureItem(featuresCard, "👨‍🏫 Unified Multi-Role Faculty", "Subject attendance register, assignments, CFA advised class monitor & HOD governance.");
        featuresCard.add(Box.createVerticalStrut(14));
        addFeatureItem(featuresCard, "👑 Principal Executive Sanctions", "Tier-3 duty leave approvals, HOD appointments & college-wide academic analytics.");
        featuresCard.add(Box.createVerticalStrut(14));
        addFeatureItem(featuresCard, "🛡️ System Admin & Security Logs", "User provisioning, database monitor & real-time SQL injection audit trail.");

        hero.add(top, BorderLayout.NORTH);
        hero.add(featuresCard, BorderLayout.CENTER);

        return hero;
    }

    private void addFeatureItem(JPanel container, String title, String desc) {
        JPanel item = new JPanel(new BorderLayout(2, 2));
        item.setOpaque(false);

        JLabel lblT = new JLabel(title);
        lblT.setFont(UITheme.FONT_BOLD);
        lblT.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblD = new JLabel("<html>" + desc + "</html>");
        lblD.setFont(UITheme.FONT_SMALL);
        lblD.setForeground(UITheme.TEXT_MUTED);

        item.add(lblT, BorderLayout.NORTH);
        item.add(lblD, BorderLayout.CENTER);
        container.add(item);
    }

    private JPanel createLoginPanel() {
        JPanel loginCard = UIComponents.createCardPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Sign In to Portal");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Enter your Register No., Faculty ID, or Username");
        lblSubtitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Apply strict Swing DocumentFilters against SQL injection & exploits
        InputFilterFactory.applyIdentifierFilter(txtIdentifier, 50);
        InputFilterFactory.applyPasswordFilter(txtPassword, 50);

        txtIdentifier.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel lblUser = new JLabel("Institutional ID / Username");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.TEXT_PRIMARY);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_BOLD);
        lblPass.setForeground(UITheme.TEXT_PRIMARY);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnLogin = UIComponents.createPrimaryButton("Authenticate & Log In");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> performLogin());

        // Also login on Enter press in password field
        txtPassword.addActionListener(e -> performLogin());

        // Demo Quick-Fill Buttons Panel
        JPanel demoPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        demoPanel.setOpaque(false);
        demoPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(UITheme.CARD_BORDER), "Quick-Fill Demo Credentials",
                0, 0, UITheme.FONT_SMALL, UITheme.TEXT_MUTED
        ));
        demoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        demoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        demoPanel.add(createQuickButton("Student (Rohan)", "REG2024CS001", "student123"));
        demoPanel.add(createQuickButton("CFA (Prof. Arun)", "FAC-CS-002", "faculty123"));
        demoPanel.add(createQuickButton("HOD CSE (Rajesh)", "FAC-CS-001", "faculty123"));
        demoPanel.add(createQuickButton("HOD Math (Sarah)", "FAC-MA-001", "faculty123"));
        demoPanel.add(createQuickButton("Principal (Menon)", "principal", "principal123"));
        demoPanel.add(createQuickButton("System Admin", "admin", "admin123"));

        loginCard.add(lblTitle);
        loginCard.add(lblSubtitle);
        loginCard.add(Box.createVerticalStrut(18));
        loginCard.add(lblUser);
        loginCard.add(Box.createVerticalStrut(4));
        loginCard.add(txtIdentifier);
        loginCard.add(Box.createVerticalStrut(12));
        loginCard.add(lblPass);
        loginCard.add(Box.createVerticalStrut(4));
        loginCard.add(txtPassword);
        loginCard.add(Box.createVerticalStrut(6));
        loginCard.add(lblError);
        loginCard.add(Box.createVerticalStrut(12));
        loginCard.add(btnLogin);
        loginCard.add(Box.createVerticalStrut(16));
        loginCard.add(demoPanel);

        return loginCard;
    }

    private JButton createQuickButton(String label, String id, String pass) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(new Color(45, 45, 65));
        btn.setForeground(UITheme.TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            txtIdentifier.setText(id);
            txtPassword.setText(pass);
            lblError.setText(" ");
        });
        return btn;
    }

    private void performLogin() {
        String identifier = txtIdentifier.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both ID and password.");
            return;
        }

        lblError.setText("Authenticating...");

        SwingUtilities.invokeLater(() -> {
            User user = userDAO.authenticate(identifier, password);
            if (user != null) {
                lblError.setText(" ");
                SessionManager.getInstance().login(user);
                openRoleDashboard(user);
                dispose(); // Close landing window
            } else {
                lblError.setText("Invalid credentials or account inactive.");
            }
        });
    }

    private void openRoleDashboard(User user) {
        Role role = user.getRole();
        switch (role) {
            case STUDENT -> {
                StudentDashboardFrame frame = new StudentDashboardFrame();
                frame.setVisible(true);
            }
            case FACULTY -> {
                FacultyDashboardFrame frame = new FacultyDashboardFrame();
                frame.setVisible(true);
            }
            case PRINCIPAL -> {
                PrincipalDashboardFrame frame = new PrincipalDashboardFrame();
                frame.setVisible(true);
            }
            case ADMIN -> {
                AdminDashboardFrame frame = new AdminDashboardFrame();
                frame.setVisible(true);
            }
        }
    }
}
