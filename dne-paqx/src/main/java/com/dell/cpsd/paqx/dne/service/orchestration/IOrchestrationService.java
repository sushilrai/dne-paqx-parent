package com.dell.cpsd.paqx.dne.service.orchestration;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.IBaseService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */
public interface IOrchestrationService {
    void orchestrateWorkflow(Job job, IBaseService jobService);
}
