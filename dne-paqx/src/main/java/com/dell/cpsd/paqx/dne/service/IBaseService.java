package com.dell.cpsd.paqx.dne.service;

import com.dell.cpsd.paqx.dne.domain.Job;

/**
 * Created by madenb on 4/27/2017.
 */
public interface IBaseService {
    WorkflowService getWorkflowService();
    void setWorkflowService (WorkflowService workflowService);
}
