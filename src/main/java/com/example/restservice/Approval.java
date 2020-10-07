package com.example.restservice;

public class Approval {
    String approvalDecision;

    public Approval(){}
    public Approval(String approvalDecision) {
        this.approvalDecision = approvalDecision;
    }

    public String getApprovalDecision() {
        return approvalDecision;
    }

    public void setApprovalDecision(String approvalDecision) {
        this.approvalDecision = approvalDecision;
    }
}
