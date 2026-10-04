package com.acadexia.model;

import java.sql.Timestamp;

public class Grievance {
    public enum Category {
        ACADEMIC("Academic Issue"),
        EXAMINATION("Examination & Evaluation"),
        ATTENDANCE("Attendance Discrepancy"),
        FACILITY("Campus & Hostel Facility"),
        HARASSMENT("Ragging / Harassment"),
        OTHER("Other Concern");

        private final String label;
        Category(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum Status {
        SUBMITTED("Submitted"),
        UNDER_REVIEW("Under Review"),
        RESOLVED("Resolved"),
        DISMISSED("Dismissed");

        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private int id;
    private int studentId;
    private String studentName;
    private String registerNumber;
    private String className;
    private String title;
    private Category category;
    private String description;
    private Status status = Status.SUBMITTED;
    private String resolution;
    private Integer resolvedByUserId;
    private String resolvedByUserName;
    private Timestamp createdAt;
    private Timestamp resolvedAt;

    public Grievance() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public Integer getResolvedByUserId() { return resolvedByUserId; }
    public void setResolvedByUserId(Integer resolvedByUserId) { this.resolvedByUserId = resolvedByUserId; }

    public String getResolvedByUserName() { return resolvedByUserName; }
    public void setResolvedByUserName(String resolvedByUserName) { this.resolvedByUserName = resolvedByUserName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }
}
