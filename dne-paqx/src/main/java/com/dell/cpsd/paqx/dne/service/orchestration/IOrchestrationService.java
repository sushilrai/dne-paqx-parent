package com.dell.cpsd.paqx.dne.service.orchestration;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.IBaseService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;

/**
 * Created by madenb on 4/27/2017.
 */
public interface IOrchestrationService {
    void orchestrateWorkflow(Job job, IBaseService jobService);
}
