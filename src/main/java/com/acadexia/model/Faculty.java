package com.acadexia.model;

import java.sql.Date;

public class Faculty {
    private int id;
    private int userId;
    private String facultyId; // e.g. FAC-CS-101
    private String designation;
    private String qualification;
    private int departmentId;
    private String departmentName;
    private String fullName;
    private String email;
    private String phone;
    private Date joiningDate;

    // Computed contextual roles
    private boolean isHod = false;
    private boolean isCfa = false;
    private Integer advisedClassId;
    private String advisedClassName;

    public Faculty() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Date getJoiningDate() { return joiningDate; }
    public void setJoiningDate(Date joiningDate) { this.joiningDate = joiningDate; }

    public boolean isHod() { return isHod; }
    public void setHod(boolean hod) { isHod = hod; }

    public boolean isCfa() { return isCfa; }
    public void setCfa(boolean cfa) { isCfa = cfa; }

    public Integer getAdvisedClassId() { return advisedClassId; }
    public void setAdvisedClassId(Integer advisedClassId) { this.advisedClassId = advisedClassId; }

    public String getAdvisedClassName() { return advisedClassName; }
    public void setAdvisedClassName(String advisedClassName) { this.advisedClassName = advisedClassName; }

    @Override
    public String toString() {
        return fullName + " (" + facultyId + ") - " + designation;
    }
}
