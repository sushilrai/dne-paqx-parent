/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.addNode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.IBaseService;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionResponse;

import java.util.UUID;

public interface IAddNodeService extends IBaseService {

    Job createWorkflow(final String workflowType, final String startingStep,
                                                        final String currentStatus);

    Job findJob(UUID jobId);

    NodeExpansionResponse makeNodeExpansionResponse(final Job job);

}
