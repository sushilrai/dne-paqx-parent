/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.workflow.preprocess;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.IBaseService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionResponse;

import java.util.UUID;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
 */

public interface IPreProcessService extends IBaseService {
    Job createWorkflow(String workflowType, String startingStep, String currentStatus);

    NodeExpansionResponse makeNodeExpansionResponse(Job job);

    Job findJob(UUID jobId);

    WorkflowService getWorkflowService();
}
