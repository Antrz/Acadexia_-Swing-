package com.acadexia.model;

import java.sql.Date;
import java.sql.Timestamp;

public class DutyLeave {
    public enum Status {
        PENDING_ADVISOR("Pending Advisor Review"),
        RECOMMENDED_BY_ADVISOR("Recommended by CFA"),
        REJECTED_BY_ADVISOR("Rejected by CFA"),
        ENDORSED_BY_HOD("Endorsed by HOD"),
        REJECTED_BY_HOD("Rejected by HOD"),
        APPROVED("Approved by Principal"),
        REJECTED("Rejected by Principal");

        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private int id;
    private int studentId;
    private String studentName;
    private String registerNumber;
    private String className;
    private String departmentName;
    private String reason;
    private Date startDate;
    private Date endDate;
    private int totalDays;
    private String proofDetails;
    private Status status = Status.PENDING_ADVISOR;
    private String advisorRemarks;
    private String hodRemarks;
    private String principalRemarks;
    private Timestamp appliedAt;
    private Timestamp updatedAt;

    public DutyLeave() {}

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

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public String getProofDetails() { return proofDetails; }
    public void setProofDetails(String proofDetails) { this.proofDetails = proofDetails; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getAdvisorRemarks() { return advisorRemarks; }
    public void setAdvisorRemarks(String advisorRemarks) { this.advisorRemarks = advisorRemarks; }

    public String getHodRemarks() { return hodRemarks; }
    public void setHodRemarks(String hodRemarks) { this.hodRemarks = hodRemarks; }

    public String getPrincipalRemarks() { return principalRemarks; }
    public void setPrincipalRemarks(String principalRemarks) { this.principalRemarks = principalRemarks; }

    public Timestamp getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Timestamp appliedAt) { this.appliedAt = appliedAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
