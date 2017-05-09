package com.dell.cpsd.paqx.dne.domain;

import java.util.UUID;

/**
 * Created by madenb on 4/28/2017.
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
