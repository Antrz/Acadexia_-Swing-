package com.acadexia.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Assignment {
    private int id;
    private String title;
    private String description;
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private int classId;
    private String className;
    private int facultyId;
    private String facultyName;
    private Date dueDate;
    private double maxMarks;
    private Timestamp createdAt;

    // Computed for student/advisor view
    private int totalSubmissions;
    private int totalStudents;
    private boolean submittedByCurrentStudent;
    private Double marksAwarded;

    public Assignment() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getTotalSubmissions() { return totalSubmissions; }
    public void setTotalSubmissions(int totalSubmissions) { this.totalSubmissions = totalSubmissions; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public boolean isSubmittedByCurrentStudent() { return submittedByCurrentStudent; }
    public void setSubmittedByCurrentStudent(boolean submittedByCurrentStudent) { this.submittedByCurrentStudent = submittedByCurrentStudent; }

    public Double getMarksAwarded() { return marksAwarded; }
    public void setMarksAwarded(Double marksAwarded) { this.marksAwarded = marksAwarded; }

    @Override
    public String toString() {
        return title + " (" + subjectCode + " - Due: " + dueDate + ")";
    }
}
