package com.acadexia.model;

public class Department {
    private int id;
    private String code;
    private String name;
    private Integer hodFacultyId;
    private String hodName;

    public Department() {}

    public Department(int id, String code, String name, Integer hodFacultyId) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.hodFacultyId = hodFacultyId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getHodFacultyId() { return hodFacultyId; }
    public void setHodFacultyId(Integer hodFacultyId) { this.hodFacultyId = hodFacultyId; }

    public String getHodName() { return hodName; }
    public void setHodName(String hodName) { this.hodName = hodName; }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
