package com.acadexia.model;

public class Subject {
    private int id;
    private String code;
    private String name;
    private int departmentId;
    private String departmentName;
    private int semester;
    private int credits;
    private int maxInternalMarks;
    private int maxExternalMarks;

    public Subject() {}

    public Subject(int id, String code, String name, int departmentId, int semester, int credits) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.departmentId = departmentId;
        this.semester = semester;
        this.credits = credits;
        this.maxInternalMarks = 50;
        this.maxExternalMarks = 100;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getMaxInternalMarks() { return maxInternalMarks; }
    public void setMaxInternalMarks(int maxInternalMarks) { this.maxInternalMarks = maxInternalMarks; }

    public int getMaxExternalMarks() { return maxExternalMarks; }
    public void setMaxExternalMarks(int maxExternalMarks) { this.maxExternalMarks = maxExternalMarks; }

    @Override
    public String toString() {
        return code + " - " + name + " (Credits: " + credits + ")";
    }
}
