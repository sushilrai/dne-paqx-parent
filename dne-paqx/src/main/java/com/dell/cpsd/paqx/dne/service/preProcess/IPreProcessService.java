/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.preProcess;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.IBaseService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionResponse;

import java.util.UUID;

public interface IPreProcessService extends IBaseService {
    Job createWorkflow(final String workflowType, final String startingStep,
                              final String currentStatus);
    NodeExpansionResponse makeNodeExpansionResponse(final Job job);

    Job findJob(UUID jobId);

    WorkflowService getWorkflowService();
}
