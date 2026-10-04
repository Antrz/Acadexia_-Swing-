package com.acadexia.model;

/**
 * High-level system roles.
 */
public enum Role {
    STUDENT("Student"),
    FACULTY("Faculty"),
    PRINCIPAL("Principal"),
    ADMIN("System Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
