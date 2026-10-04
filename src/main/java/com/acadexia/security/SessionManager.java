package com.acadexia.security;

import com.acadexia.dao.AuditLogDAO;
import com.acadexia.dao.FacultyDAO;
import com.acadexia.dao.StudentDAO;
import com.acadexia.model.AuditLog;
import com.acadexia.model.Faculty;
import com.acadexia.model.Role;
import com.acadexia.model.Student;
import com.acadexia.model.User;

/**
 * Singleton managing user session state, active profiles, and logout flow.
 */
public final class SessionManager {

    private static SessionManager instance;

    private User currentUser;
    private Faculty currentFaculty;
    private Student currentStudent;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        this.currentFaculty = null;
        this.currentStudent = null;

        if (user.getRole() == Role.FACULTY) {
            FacultyDAO facDAO = new FacultyDAO();
            this.currentFaculty = facDAO.getByUserId(user.getId());
        } else if (user.getRole() == Role.STUDENT) {
            StudentDAO stuDAO = new StudentDAO();
            this.currentStudent = stuDAO.getByUserId(user.getId());
        }
    }

    public void logout() {
        if (currentUser != null) {
            AuditLogDAO.log(currentUser.getId(), currentUser.getUsername(), currentUser.getRole().name(),
                    "LOGOUT", "AUTH", String.valueOf(currentUser.getId()),
                    "User logged out successfully.", AuditLog.Severity.INFO);
        }
        this.currentUser = null;
        this.currentFaculty = null;
        this.currentStudent = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Faculty getCurrentFaculty() {
        return currentFaculty;
    }

    public Student getCurrentStudent() {
        return currentStudent;
    }

    public Role getRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
}
