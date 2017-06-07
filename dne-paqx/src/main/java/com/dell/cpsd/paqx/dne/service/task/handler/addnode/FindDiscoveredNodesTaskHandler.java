package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.IWorkflowTaskHandler;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.*;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */
public class FindDiscoveredNodesTaskHandler extends BaseTaskHandler implements IWorkflowTaskHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FindDiscoveredNodesTaskHandler.class);

    private NodeService nodeService;

    public FindDiscoveredNodesTaskHandler(NodeService nodeService){
        this.nodeService = nodeService;
    }

    @Override
    public boolean executeTask(Job job)
    {
        LOGGER.info("Execute Find Discovered Nodes");
        DiscoveredNodesResponse response = initializeResponse(job);
        try {
            List<DiscoveredNode> discoveredNodesResponse = nodeService.listDiscoveredNodes();

            if (discoveredNodesResponse != null) {
                final List<NodeInfo> nodeInfo = discoveredNodesResponse.stream().
                        map(n -> new NodeInfo(n.getConvergedUuid(), n.getNodeId(), NodeStatus.valueOf(n.getNodeStatus().toString()))).
                        collect(Collectors.toList());

                LOGGER.info("Found available nodes : " + nodeInfo);
                response.setNodesInfo(nodeInfo);
                response.setWorkFlowTaskStatus(Status.SUCCEEDED);
                return true;
            }
        }
        catch(Exception e){
                LOGGER.info(" ", e);
                response.setWorkFlowTaskStatus(Status.FAILED);
                response.addError(e.toString());
            }

        return false;
    }
    @Override
    public DiscoveredNodesResponse initializeResponse(Job job){
        DiscoveredNodesResponse response = new DiscoveredNodesResponse();
        response.setWorkFlowTaskName(job.getCurrentTask().getTaskName());
        response.setWorkFlowTaskStatus(Status.IN_PROGRESS);
        job.addTaskResponse(job.getStep(), response);

        return response;
    }

}
