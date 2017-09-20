/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.workflow.addnode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.IBaseService;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionResponse;

import java.util.UUID;

public interface IAddNodeService extends IBaseService {

    Job createWorkflow(String workflowType, String startingStep, String currentStatus);

    Job findJob(UUID jobId);

    NodeExpansionResponse makeNodeExpansionResponse(Job job);

}
