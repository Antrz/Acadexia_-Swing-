package com.acadexia.model;

import java.sql.Timestamp;

public class AssignmentSubmission {
    public enum Status {
        SUBMITTED("Submitted"),
        PENDING("Pending"),
        GRADED("Graded"),
        LATE("Submitted Late");

        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private int id;
    private int assignmentId;
    private String assignmentTitle;
    private int studentId;
    private String studentName;
    private String registerNumber;
    private String rollNumber;
    private Timestamp submissionDate;
    private String submissionText;
    private String fileName;
    private Status status = Status.SUBMITTED;
    private Double marksObtained;
    private double maxMarks;
    private String facultyFeedback;

    public AssignmentSubmission() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public Timestamp getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(Timestamp submissionDate) { this.submissionDate = submissionDate; }

    public String getSubmissionText() { return submissionText; }
    public void setSubmissionText(String submissionText) { this.submissionText = submissionText; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }

    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }

    public String getFacultyFeedback() { return facultyFeedback; }
    public void setFacultyFeedback(String facultyFeedback) { this.facultyFeedback = facultyFeedback; }
}
