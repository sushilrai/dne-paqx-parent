/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service;

public interface IBaseService {
    WorkflowService getWorkflowService();
    void setWorkflowService (WorkflowService workflowService);
}
