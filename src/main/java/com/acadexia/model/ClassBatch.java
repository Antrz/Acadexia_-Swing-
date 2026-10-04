package com.acadexia.model;

public class ClassBatch {
    private int id;
    private String name;
    private int departmentId;
    private String departmentName;
    private int semester;
    private String academicYear;
    private String section;
    private Integer advisorFacultyId;
    private String advisorName;

    public ClassBatch() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getAdvisorFacultyId() { return advisorFacultyId; }
    public void setAdvisorFacultyId(Integer advisorFacultyId) { this.advisorFacultyId = advisorFacultyId; }

    public String getAdvisorName() { return advisorName; }
    public void setAdvisorName(String advisorName) { this.advisorName = advisorName; }

    @Override
    public String toString() {
        return name + " (S" + semester + " " + section + " - " + academicYear + ")";
    }
}
