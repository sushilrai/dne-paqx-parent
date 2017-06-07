package com.dell.cpsd.paqx.dne.domain;

import java.util.UUID;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */
public class WorkflowTask {
    IWorkflowTaskHandler taskHandler;
    String taskName;
    UUID jobCorrelationId;

    public IWorkflowTaskHandler getTaskHandler() {
        return taskHandler;
    }

    public void setTaskHandler(IWorkflowTaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public UUID getJobCorrelationId() {
        return jobCorrelationId;
    }

    public void setJobCorrelationId(UUID jobCorrelationId) {
        this.jobCorrelationId = jobCorrelationId;
    }
}
