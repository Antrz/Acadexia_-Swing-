package com.acadexia.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Attendance {
    public enum Status {
        PRESENT("Present"),
        ABSENT("Absent"),
        DUTY_LEAVE("Duty Leave");

        private final String label;
        Status(String label) { this.label = label; }
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
    private int classId;
    private Date date;
    private int hour;
    private Status status = Status.PRESENT;
    private int markedByFacultyId;
    private String remarks;
    private Timestamp createdAt;

    public Attendance() {}

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

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getMarkedByFacultyId() { return markedByFacultyId; }
    public void setMarkedByFacultyId(int markedByFacultyId) { this.markedByFacultyId = markedByFacultyId; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
