package com.acadexia.model;

import java.sql.Timestamp;

public class AuditLog {
    public enum Severity {
        INFO,
        WARNING,
        SECURITY_ALERT,
        ERROR
    }

    private int id;
    private Integer userId;
    private String username;
    private String role;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private String ipAddress;
    private Severity severity = Severity.INFO;
    private Timestamp timestamp;

    public AuditLog() {}

    public AuditLog(Integer userId, String username, String role, String action, String entityType, String entityId, String details, Severity severity) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.severity = severity;
        this.ipAddress = "127.0.0.1";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
