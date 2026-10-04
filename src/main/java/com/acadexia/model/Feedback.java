package com.acadexia.model;

import java.sql.Timestamp;

public class Feedback {
    private int id;
    private int studentId;
    private int facultyId;
    private String facultyName;
    private int subjectId;
    private String subjectName;
    private String subjectCode;
    private int ratingTeaching;
    private int ratingPunctuality;
    private int ratingClarity;
    private String comments;
    private Timestamp createdAt;

    public Feedback() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public int getRatingTeaching() { return ratingTeaching; }
    public void setRatingTeaching(int ratingTeaching) { this.ratingTeaching = ratingTeaching; }

    public int getRatingPunctuality() { return ratingPunctuality; }
    public void setRatingPunctuality(int ratingPunctuality) { this.ratingPunctuality = ratingPunctuality; }

    public int getRatingClarity() { return ratingClarity; }
    public void setRatingClarity(int ratingClarity) { this.ratingClarity = ratingClarity; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public double getAverageRating() {
        return (ratingTeaching + ratingPunctuality + ratingClarity) / 3.0;
    }
}
