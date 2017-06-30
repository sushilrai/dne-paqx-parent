/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.task.handler.addnode;

import com.dell.cpsd.paqx.dne.domain.IWorkflowTaskHandler;
import com.dell.cpsd.paqx.dne.domain.Job;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.model.*;
import com.dell.cpsd.paqx.dne.service.task.handler.BaseTaskHandler;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @since 1.0
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
        FirstAvailableDiscoveredNodeResponse response = initializeResponse(job);
        try {
            List<DiscoveredNode> discoveredNodesResponse = nodeService.listDiscoveredNodes();

            if (discoveredNodesResponse != null) {
                final List<NodeInfo> nodeInfoList = discoveredNodesResponse.stream().
                        map(n -> new NodeInfo(n.getConvergedUuid(), n.getNodeId(), NodeStatus.valueOf(n.getNodeStatus().toString()))).
                        collect(Collectors.toList());

                if(!CollectionUtils.isEmpty(nodeInfoList)) {
                    NodeInfo nodeInfo = (NodeInfo) nodeInfoList.get(0);
                    LOGGER.info("Found first available node : " + nodeInfo);
                    response.setNodeInfo(nodeInfo);
                    response.setWorkFlowTaskStatus(Status.SUCCEEDED);
                    return true;
                }
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
    public FirstAvailableDiscoveredNodeResponse initializeResponse(Job job){
        FirstAvailableDiscoveredNodeResponse response = new FirstAvailableDiscoveredNodeResponse();
        response.setWorkFlowTaskName(job.getCurrentTask().getTaskName());
        response.setWorkFlowTaskStatus(Status.IN_PROGRESS);
        job.addTaskResponse(job.getStep(), response);

        return response;
    }

}
