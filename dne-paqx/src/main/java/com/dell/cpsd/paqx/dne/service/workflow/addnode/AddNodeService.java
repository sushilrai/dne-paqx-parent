/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.workflow.addnode;

import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.domain.WorkflowTask;
import com.dell.cpsd.paqx.dne.service.BaseService;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.WorkflowService;
import com.dell.cpsd.paqx.dne.service.model.NodeExpansionResponse;
import com.dell.cpsd.paqx.dne.service.task.handler.addnode.AddNodeToSystemDefinitionTaskHandler;
import com.dell.cpsd.paqx.dne.service.task.handler.addnode.ConfigIdracTaskHandler;
import com.dell.cpsd.paqx.dne.service.task.handler.addnode.FindDiscoveredNodesTaskHandler;
import com.dell.cpsd.paqx.dne.service.task.handler.addnode.NotifyNodeDiscoveryNodeAllocationCompletedTaskHandler;
import com.dell.cpsd.sdk.AMQPClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AddNodeService extends BaseService implements IAddNodeService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AddNodeService.class);

//    @Value("#{PropertySplitter.map('${addnode.map.step.to.map}')}")
//    private Map<String, String> propertyAsMap;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private AMQPClient sdkAMQPClient;

    @Autowired
    @Qualifier("addNodeWorkflowService")
    private WorkflowService workflowService;

    @Override
    public Job createWorkflow(final String workflowType, final String startingStep, final String currentStatus)
    {
        Job job = workflowService.createWorkflow(workflowType, startingStep, currentStatus, addNodeWorkflowTasks());
        return job;
    }

    @Bean("addNodesWorkflowTasks")
    public Map<String, WorkflowTask> addNodeWorkflowTasks()
    {
        final Map<String, WorkflowTask> workflowTasks = new HashMap<>();

        workflowTasks.put("findAvailableNodes", findDiscoveredNodesTask());
        workflowTasks.put("configIdrac", configIdracTask());
        workflowTasks.put("updateSystemDefinition", updateSystemDefinitionTask());
        workflowTasks.put("notifyNodeDiscovery", notifyNodeDiscoveryNodeAllocationCompletedTask());
        return workflowTasks;
    }

    @Bean("findDiscoveredNodesTask")
    private WorkflowTask findDiscoveredNodesTask()
    {
        return createTask("findDiscoveredNodesTaskHandler", new FindDiscoveredNodesTaskHandler(this.nodeService));
    }

    @Bean("configIdracTask")
    private WorkflowTask configIdracTask()
    {
        return createTask("configIdracTask", new ConfigIdracTaskHandler(this.nodeService));
    }

    @Bean("updateSystemDefinitionTask")
    private WorkflowTask updateSystemDefinitionTask()
    {
        return createTask("updateSystemDefinitionTaskHandler", new AddNodeToSystemDefinitionTaskHandler(this.sdkAMQPClient));
    }

    @Bean("notifyNodeDiscoveryNodeAllocationCompletedTask")
    private WorkflowTask notifyNodeDiscoveryNodeAllocationCompletedTask()
    {
        return createTask("notifyNodeDiscoveryNodeAllocationCompletedTaskHandler",
                new NotifyNodeDiscoveryNodeAllocationCompletedTaskHandler(this.nodeService));
    }

    public Job findJob(UUID jobId)
    {
        final Job job = workflowService.findJob(jobId);

        return job;
    }

    public NodeExpansionResponse makeNodeExpansionResponse(final Job job)
    {
        return makeNodeExpansionResponse(job, workflowService);
    }

//    public String findPathFromStep(final String step)
//    {
//        return propertyAsMap.get(step);
//    }

    public WorkflowService getWorkflowService()
    {
        return workflowService;
    }

    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
}
