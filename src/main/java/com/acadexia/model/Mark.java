package com.acadexia.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Mark {
    public enum ExamType {
        SERIES_1("Series Test 1"),
        SERIES_2("Series Test 2"),
        ASSIGNMENT("Assignment Evaluation"),
        INTERNAL("Consolidated Internal"),
        SEMESTER_EXAM("End Semester Examination");

        private final String label;
        ExamType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private int id;
    private int studentId;
    private String studentName;
    private String registerNumber;
    private String rollNumber;
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private ExamType examType;
    private double marksObtained;
    private double maxMarks;
    private Date examDate;
    private int recordedByFacultyId;
    private Timestamp createdAt;

    public Mark() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public ExamType getExamType() { return examType; }
    public void setExamType(ExamType examType) { this.examType = examType; }

    public double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(double marksObtained) { this.marksObtained = marksObtained; }

    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }

    public Date getExamDate() { return examDate; }
    public void setExamDate(Date examDate) { this.examDate = examDate; }

    public int getRecordedByFacultyId() { return recordedByFacultyId; }
    public void setRecordedByFacultyId(int recordedByFacultyId) { this.recordedByFacultyId = recordedByFacultyId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public double getPercentage() {
        return maxMarks > 0 ? (marksObtained / maxMarks) * 100.0 : 0.0;
    }

    public String getGrade() {
        double pct = getPercentage();
        if (pct >= 90) return "S";
        if (pct >= 85) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 75) return "B+";
        if (pct >= 70) return "B";
        if (pct >= 65) return "C+";
        if (pct >= 60) return "C";
        if (pct >= 50) return "D";
        if (pct >= 40) return "P";
        return "F";
    }

    public double getGradePoint() {
        String grade = getGrade();
        return switch (grade) {
            case "S" -> 10.0;
            case "A+" -> 9.0;
            case "A" -> 8.5;
            case "B+" -> 8.0;
            case "B" -> 7.5;
            case "C+" -> 7.0;
            case "C" -> 6.5;
            case "D" -> 6.0;
            case "P" -> 5.5;
            default -> 0.0;
        };
    }
}
