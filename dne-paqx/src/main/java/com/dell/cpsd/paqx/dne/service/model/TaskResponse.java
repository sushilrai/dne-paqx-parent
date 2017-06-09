/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.model;

import java.util.ArrayList;
import java.util.List;

public class TaskResponse {

    private String workFlowTaskName;
    private Status workFlowTaskStatus;
    private List<String> errors;
    private List<String> warnings;

    public TaskResponse() {
        errors = new ArrayList<>();
        warnings = new ArrayList<>();
    }

    public void addError(String errorMsg){
        errors.add(errorMsg);
    }
    public List<String> getErrors() {
        return errors;
    }

    public void addWarning(String warningMsg){
        warnings.add(warningMsg);
    }
    public List<String> getWarnings() {
        return warnings;
    }

    public String getWorkFlowTaskName() {

        return workFlowTaskName;
    }

    public void setWorkFlowTaskName(String workFlowTaskName) {
        this.workFlowTaskName = workFlowTaskName;
    }

    public Status getWorkFlowTaskStatus() {
        return workFlowTaskStatus;
    }

    public void setWorkFlowTaskStatus(Status workFlowTaskStatus) {
        this.workFlowTaskStatus = workFlowTaskStatus;
    }


}
